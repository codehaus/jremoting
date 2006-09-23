/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
 * Portions copyright 2001 - 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.codehaus.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.jremoting.client.factories;

import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.client.HostContext;
import org.codehaus.jremoting.client.NotPublishedException;
import org.codehaus.jremoting.requests.RetrieveStub;
import org.codehaus.jremoting.responses.StubRetrievalFailed;
import org.codehaus.jremoting.responses.StubClass;
import org.codehaus.jremoting.responses.*;

import java.util.HashMap;

/**
 * Class ServerSideStubFactory
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class ServerSideStubFactory extends AbstractStubFactory {

    private final HashMap publishedServiceClassLoaders = new HashMap();

    public ServerSideStubFactory(HostContext hostContext) throws ConnectionException {
        super(hostContext);
    }

    protected Class getStubClass(String publishedServiceName, String objectName) throws ConnectionException, ClassNotFoundException {

        TransportedStubClassLoader tcl = null;
        String stubClassName = "JRemotingGenerated" + publishedServiceName + "_" + objectName;

        if (publishedServiceClassLoaders.containsKey(stubClassName)) {
            tcl = (TransportedStubClassLoader) publishedServiceClassLoaders.get(stubClassName);
        } else {
            StubClass cr = null;

            try {
                AbstractResponse ar = clientInvocationHandler.handleInvocation(new RetrieveStub(publishedServiceName, objectName));

                if (ar.getResponseCode() >= ResponseConstants.PROBLEMRESPONSE) {
                    if (ar instanceof RequestFailed) {
                        throw new ConnectionException(((RequestFailed) ar).getFailureReason());
                    } else if (ar instanceof StubRetrievalFailed) {
                        StubRetrievalFailed srf = (StubRetrievalFailed) ar;

                        throw new ConnectionException("Class Retrieval Failed - " + srf.getReason());
                    }    //TODO others.
                }

                cr = (StubClass) ar;
            } catch (NotPublishedException npe) {
                throw new ConnectionException("Service " + publishedServiceName + " not published on Server");
            }

            tcl = new TransportedStubClassLoader(clientInvocationHandler.getInterfacesClassLoader());

            tcl.add(stubClassName, cr.getStubClassBytes());

            publishedServiceClassLoaders.put(stubClassName, tcl);
        }

        return tcl.loadClass(stubClassName);
    }

}
