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
package org.codehaus.jremoting.client;

import org.codehaus.jremoting.ConnectionException;

import java.util.concurrent.ExecutorService;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;

/**
 * Interface ClientInvocationHandler
 *
 * @author Paul Hammant
 * @version * $Revision: 1.3 $
 */
public interface ClientInvocationHandler {

    Response handleInvocation(Request request);

    void initialize() throws ConnectionException;

    void close();

    long getLastRealRequest();

    void ping();

    ClassLoader getFacadesClassLoader();

    /**
     * morphObject handles any changes  to the arguments being
     * marshalled to the server.
     *
     * @param remoteObjName         String remote objecct name
     * @param inputArgumentClass    Class of the input argument
     * @param inputArgumentInstance instance of the object being marshalled to the server
     * @return Object new object that replaces the input argument.
     */

    Object resolveArgument(String remoteObjName, String methodSignature, Class inputArgumentClass, Object inputArgumentInstance);

    ExecutorService getExecutorService();

    ClientMonitor getClientMonitor();

}
