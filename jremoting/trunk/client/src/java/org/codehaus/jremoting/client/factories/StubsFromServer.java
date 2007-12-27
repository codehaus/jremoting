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

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.NotPublishedException;
import org.codehaus.jremoting.client.Transport;
import org.codehaus.jremoting.requests.RetrieveStub;
import org.codehaus.jremoting.responses.ProblemResponse;
import org.codehaus.jremoting.responses.RequestFailed;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.responses.StubClass;
import org.codehaus.jremoting.responses.StubRetrievalFailed;
import org.codehaus.jremoting.util.StubHelper;

import java.util.HashMap;

/**
 * Class StubsFromServer
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class StubsFromServer implements StubClassLoader {

    private final HashMap publishedServiceClassLoaders = new HashMap();

    public Class getStubClass(String publishedServiceName, String objectName, Transport transport) throws ConnectionException, ClassNotFoundException {

        TransportedStubClassLoader tcl = null;
        String stubClassName = StubHelper.formatStubClassName(publishedServiceName, objectName);

        if (publishedServiceClassLoaders.containsKey(stubClassName)) {
            tcl = (TransportedStubClassLoader) publishedServiceClassLoaders.get(stubClassName);
        } else {
            StubClass cr = null;

            try {
                Response ar = transport.invoke(new RetrieveStub(publishedServiceName, objectName));

                if (ar instanceof ProblemResponse) {
                    if (ar instanceof RequestFailed) {
                        RequestFailed requestFailed = (RequestFailed) ar;
                        throw new ConnectionException(requestFailed.getFailureReason());
                    } else if (ar instanceof StubRetrievalFailed) {
                        StubRetrievalFailed srf = (StubRetrievalFailed) ar;

                        throw new ConnectionException("Class Retrieval Failed - " + srf.getReason());
                    }
                }

                cr = (StubClass) ar;
            } catch (NotPublishedException npe) {
                throw new ConnectionException("Service " + publishedServiceName + " not published on Server");
            }

            tcl = new TransportedStubClassLoader(transport.getFacadesClassLoader());

            tcl.add(stubClassName, cr.getStubClassBytes());

            publishedServiceClassLoaders.put(stubClassName, tcl);
        }

        return tcl.loadClass(stubClassName);
    }



}
