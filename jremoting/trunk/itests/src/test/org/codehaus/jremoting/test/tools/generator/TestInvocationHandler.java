/* ====================================================================
 * Copyright 2005 JRemoting Committers
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
import org.codehaus.jremoting.commands.ExceptionResponse;
import org.codehaus.jremoting.commands.MethodRequest;
import org.codehaus.jremoting.commands.MethodResponse;
import org.codehaus.jremoting.commands.OpenConnectionRequest;
import org.codehaus.jremoting.commands.OpenConnectionResponse;
import org.codehaus.jremoting.commands.Request;
import org.codehaus.jremoting.commands.Response;
import org.codehaus.jremoting.server.ServerInvocationHandler;

import java.lang.reflect.Method;

/**
 * TestInvocationHandler
 *
 * @author <a href="mailto:vinayc@apache">Vinay Chandrasekharan</a>
 * @version 1.0
 */
public class TestInvocationHandler extends AbstractClientInvocationHandler implements ServerInvocationHandler {


    public TestInvocationHandler(ThreadPool threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger) {
        super(threadPool, clientMonitor, connectionPinger);
    }

    public Response handleInvocation(Request request) {
        return handleInvocation(request, "test");
    }

    public Response handleInvocation(Request request, Object connectionDetails) {
        if (request instanceof OpenConnectionRequest) {
            return new OpenConnectionResponse();
        } else if (request instanceof MethodRequest) {
            MethodRequest methodRequest = (MethodRequest) request;
            //System.out.println("methodRequest[" + methodRequest.getMethodSignature() + "]");
            Method[] methods = TestRemoteInterface.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                try {
                    if (methodRequest.getMethodSignature().indexOf(methods[i].getName()) != -1) {
                        Object[] _arguments = methodRequest.getArgs();
                        for (int j = 0; j < _arguments.length; j++) {

                            if (!TestRemoteInterface.class.getField(methods[i].getName() + "_arg" + j).get(null).equals(_arguments[j])) {
                                return new ExceptionResponse(new Exception(methodRequest.getMethodSignature() + ": arguments not marshalled correctly \n expected[" + TestRemoteInterface.class.getField(methods[i].getName() + "_arg" + j).get(null) + "] received[" + _arguments[j] + "]"));
                            }
                        }
                        MethodResponse methodReply = null;
                        if (methods[i].getReturnType() != Void.TYPE) {
                            methodReply = new MethodResponse(TestRemoteInterface.class.getField(methods[i].getName() + "_retValue").get(null));
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