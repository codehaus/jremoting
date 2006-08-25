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
package org.codehaus.jremoting.test.tools.generator;

import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.transports.AbstractClientInvocationHandler;
import org.codehaus.jremoting.responses.ExceptionResponse;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.responses.MethodResponse;
import org.codehaus.jremoting.requests.OpenConnection;
import org.codehaus.jremoting.responses.ConnectionOpened;
import org.codehaus.jremoting.requests.AbstractRequest;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.ServerInvocationHandler;

import java.lang.reflect.Method;

/**
 * TstInvocationHandler
 *
 * @author <a href="mailto:vinayc@apache">Vinay Chandrasekharan</a>
 * @version 1.0
 */
public class TstInvocationHandler extends AbstractClientInvocationHandler implements ServerInvocationHandler {


    public TstInvocationHandler(ThreadPool threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger) {
        super(threadPool, clientMonitor, connectionPinger);
    }

    public Response handleInvocation(AbstractRequest request) {
        return handleInvocation(request, "test");
    }

    public Response handleInvocation(AbstractRequest request, Object connectionDetails) {
        if (request instanceof OpenConnection) {
            return new ConnectionOpened();
        } else if (request instanceof InvokeMethod) {
            InvokeMethod invokeMethod = (InvokeMethod) request;
            //System.out.println("invokeMethod[" + invokeMethod.getMethodSignature() + "]");
            Method[] methods = TstRemoteInterface.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                try {
                    if (invokeMethod.getMethodSignature().indexOf(methods[i].getName()) != -1) {
                        Object[] _arguments = invokeMethod.getArgs();
                        for (int j = 0; j < _arguments.length; j++) {

                            if (!TstRemoteInterface.class.getField(methods[i].getName() + "_arg" + j).get(null).equals(_arguments[j])) {
                                return new ExceptionResponse(new Exception(invokeMethod.getMethodSignature() + ": arguments not marshalled correctly \n expected[" + TstRemoteInterface.class.getField(methods[i].getName() + "_arg" + j).get(null) + "] received[" + _arguments[j] + "]"));
                            }
                        }
                        MethodResponse methodReply = null;
                        if (methods[i].getReturnType() != Void.TYPE) {
                            methodReply = new MethodResponse(TstRemoteInterface.class.getField(methods[i].getName() + "_retValue").get(null));
                        } else {
                            methodReply = new MethodResponse();
                        }
                        return methodReply;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ExceptionResponse(e);
                }

            }
        }
        return null;
    }

    /*
     * @see AbstractClientInvocationHandler#tryReconnect()
     */
    protected boolean tryReconnect() {
        return true;
    }

    /*
     * @see ClientInvocationHandler#getLastRealRequest()
     */
    public long getLastRealRequest() {
        return 0;
    }

}