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

import java.util.concurrent.ExecutorService;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.transports.StatefulClientInvocationHandler;
import org.codehaus.jremoting.responses.ExceptionThrown;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.responses.SimpleMethodInvoked;
import org.codehaus.jremoting.requests.OpenConnection;
import org.codehaus.jremoting.responses.ConnectionOpened;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.ServerInvocationHandler;

import java.lang.reflect.Method;

/**
 * TstClientInvocationHandler
 *
 * @author <a href="mailto:vinayc@apache">Vinay Chandrasekharan</a>
 * @version 1.0
 */
public class TstClientInvocationHandler extends StatefulClientInvocationHandler implements ServerInvocationHandler {


    public TstClientInvocationHandler(ExecutorService executorService, ClientMonitor clientMonitor, ConnectionPinger connectionPinger) {
        super(clientMonitor, executorService, connectionPinger, TstClientInvocationHandler.class.getClassLoader());
    }

    public Response handleInvocation(Request request) {
        return handleInvocation(request, "test");
    }

    public Response handleInvocation(Request request, Object connectionDetails) {
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
                                return new ExceptionThrown(new Exception(invokeMethod.getMethodSignature() + ": arguments not marshalled correctly \n expected[" + TstRemoteInterface.class.getField(methods[i].getName() + "_arg" + j).get(null) + "] received[" + _arguments[j] + "]"));
                            }
                        }
                        SimpleMethodInvoked methodResponse = null;
                        if (methods[i].getReturnType() != Void.TYPE) {
                            methodResponse = new SimpleMethodInvoked(TstRemoteInterface.class.getField(methods[i].getName() + "_retValue").get(null));
                        } else {
                            methodResponse = new SimpleMethodInvoked();
                        }
                        return methodResponse;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ExceptionThrown(e);
                }

            }
        }
        return null;
    }

    /*
     * @see StatefulClientInvocationHandler#tryReconnect()
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