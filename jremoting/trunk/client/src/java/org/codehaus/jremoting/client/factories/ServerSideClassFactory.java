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
import org.codehaus.jremoting.commands.ClassRequest;
import org.codehaus.jremoting.commands.ClassResponse;
import org.codehaus.jremoting.commands.ClassRetrievalFailedResponse;
import org.codehaus.jremoting.commands.ResponseConstants;
import org.codehaus.jremoting.commands.RequestFailedResponse;
import org.codehaus.jremoting.commands.Response;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Class ServerSideClassFactory
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class ServerSideClassFactory extends AbstractFactory {

    private HashMap publishedServiceClassLoaders = new HashMap();

    public ServerSideClassFactory(HostContext hostContext, boolean allowOptimize) throws ConnectionException {
        super(hostContext, allowOptimize);
    }

    protected Class getFacadeClass(String publishedServiceName, String objectName) throws ConnectionException, ClassNotFoundException {

        TransportedClassLoader tcl = null;
        String proxyClassName = "JRemotingGenerated" + publishedServiceName + "_" + objectName;

        if (publishedServiceClassLoaders.containsKey(proxyClassName)) {
            tcl = (TransportedClassLoader) publishedServiceClassLoaders.get(proxyClassName);
        } else {
            ClassResponse cr = null;

            try {
                Response ar = hostContext.getInvocationHandler().handleInvocation(new ClassRequest(publishedServiceName, objectName));

                if (ar.getResponseCode() >= ResponseConstants.PROBLEMRESPONSE) {
                    if (ar instanceof RequestFailedResponse) {
                        throw new ConnectionException(((RequestFailedResponse) ar).getFailureReason());
                    } else if (ar instanceof ClassRetrievalFailedResponse) {
                        ClassRetrievalFailedResponse crfr = (ClassRetrievalFailedResponse) ar;

                        throw new ConnectionException("Class Retrieval Failed - " + crfr.getReason());
                    }    //TODO others.
                }

                cr = (ClassResponse) ar;
            } catch (NotPublishedException npe) {
                throw new ConnectionException("Service " + publishedServiceName + " not published on Server");
            }

            tcl = new TransportedClassLoader(hostContext.getInvocationHandler().getInterfacesClassLoader());

            tcl.add(proxyClassName, cr.getProxyClassBytes());

            publishedServiceClassLoaders.put(proxyClassName, tcl);
        }

        return tcl.loadClass(proxyClassName);
    }

    protected Object getInstance(String publishedServiceName, String objectName, DefaultProxyHelper proxyHelper) throws ConnectionException {

        try {
            Class clazz = getFacadeClass(publishedServiceName, objectName);
            Constructor[] constructors = clazz.getConstructors();
            Object retVal = constructors[0].newInstance(new Object[]{proxyHelper});

            return retVal;
        } catch (InvocationTargetException ite) {
            throw new ConnectionException("Generated class not instantiated.", ite.getTargetException());
        } catch (ClassNotFoundException cnfe) {
            throw new ConnectionException("Generated class not found during lookup.", cnfe);
        } catch (InstantiationException ie) {
            throw new ConnectionException("Generated class not instantiable during lookup.", ie);
        } catch (IllegalAccessException iae) {
            throw new ConnectionException("Illegal access to generated class during lookup.", iae);
        }
    }

    /**
     * Method close
     */
    public void close() {
        hostContext.getInvocationHandler().close();
    }
}
