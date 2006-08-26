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

import org.codehaus.jremoting.api.SerializationHelper;
import org.codehaus.jremoting.requests.AbstractRequest;
import org.codehaus.jremoting.responses.AbstractResponse;
import org.codehaus.jremoting.server.ServerInvocationHandler;
import org.codehaus.jremoting.server.ServerMarshalledInvocationHandler;

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
    private ServerInvocationHandler invocationHandler;
    /**
     * The class loader.
     */
    private ClassLoader classLoader;

    /**
     * Constructor MarshalledInvocationHandlerAdapter
     *
     * @param invocationHandler The invocation handler
     */
    public MarshalledInvocationHandlerAdapter(ServerInvocationHandler invocationHandler) {
        this.invocationHandler = invocationHandler;
        classLoader = getClass().getClassLoader();
    }

    /**
     * Constructor MarshalledInvocationHandlerAdapter
     *
     * @param invocationHandler The invocation handler
     * @param classLoader       The classloader
     */
    public MarshalledInvocationHandlerAdapter(ServerInvocationHandler invocationHandler, ClassLoader classLoader) {
        this.invocationHandler = invocationHandler;
        this.classLoader = classLoader;
    }

    /**
     * Handle an Invocation
     *
     * @param request The request
     * @return The reply
     */
    public byte[] handleInvocation(byte[] request, Object connectionDetails) {

        try {
            AbstractRequest ar = (AbstractRequest) SerializationHelper.getInstanceFromBytes(request, classLoader);
            AbstractResponse response = invocationHandler.handleInvocation(ar, connectionDetails);

            return SerializationHelper.getBytesFromInstance(response);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();

            return null;
        }
    }
}
