/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
 * Portions copyright 2001 - 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.jremoting.server.adapters;

import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.ServerInvoker;
import org.codehaus.jremoting.server.ServerMarshalledInvocationHandler;
import org.codehaus.jremoting.util.SerializationHelper;

/**
 * Class MarshalledInvocationHandlerAdapter
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class MarshalledInvocationHandlerAdapter implements ServerMarshalledInvocationHandler {

    /**
     * The invocation hamdeler
     */
    private ServerInvoker invoker;
    /**
     * The class loader.
     */
    private ClassLoader facadesClassLoader;

    /**
     * Constructor MarshalledInvocationHandlerAdapter
     *
     * @param invoker The invocation handler
     */
    public MarshalledInvocationHandlerAdapter(ServerInvoker invoker) {
        this.invoker = invoker;
        facadesClassLoader = getClass().getClassLoader();
    }

    /**
     * Constructor MarshalledInvocationHandlerAdapter
     *
     * @param invoker The invocation handler
     * @param facadesClassLoader       The classloader
     */
    public MarshalledInvocationHandlerAdapter(ServerInvoker invoker, ClassLoader facadesClassLoader) {
        this.invoker = invoker;
        this.facadesClassLoader = facadesClassLoader;
    }

    /**
     * Handle an Invocation
     *
     * @param request The request
     * @return The reply
     */
    public byte[] handleInvocation(byte[] request, Object connectionDetails) {

        try {
            Request ar = (Request) SerializationHelper.getInstanceFromBytes(request, facadesClassLoader);
            Response response = invoker.invoke(ar, connectionDetails);

            return SerializationHelper.getBytesFromInstance(response);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();

            return null;
        }
    }
}
