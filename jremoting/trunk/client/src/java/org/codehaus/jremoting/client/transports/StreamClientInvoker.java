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
package org.codehaus.jremoting.client.transports;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.concurrent.ScheduledExecutorService;

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ClientStreamDriver;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.client.NoSuchReferenceException;
import org.codehaus.jremoting.client.NoSuchSessionException;
import org.codehaus.jremoting.client.NotPublishedException;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.requests.Servicable;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.requests.RequestConstants;
import org.codehaus.jremoting.responses.*;
import com.sun.corba.se.pept.transport.Connection;

/**
 * Class StreamClientInvoker
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public abstract class StreamClientInvoker extends StatefulClientInvoker {

    private ClientStreamDriver objectDriver;
    private boolean methodLogging = false;
    private long lastRealRequest = System.currentTimeMillis();
    protected final ClientStreamDriverFactory streamDriverFactory;
    private Long session;

    public StreamClientInvoker(ClientMonitor clientMonitor, ScheduledExecutorService executorService,
                                                 ConnectionPinger connectionPinger, ClassLoader facadesClassLoader,
                                                 ClientStreamDriverFactory streamDriverFactory) {
        super(clientMonitor, executorService, connectionPinger, facadesClassLoader);
        this.streamDriverFactory = streamDriverFactory;
    }


    protected void setObjectDriver(ClientStreamDriver objectDriver) {
        this.objectDriver = objectDriver;
    }

    protected void requestWritten() {
    }

    public synchronized Response invoke(Request request) {
        if (request.getRequestCode() != RequestConstants.PINGREQUEST) {
            lastRealRequest = System.currentTimeMillis();

        } else {
            ((org.codehaus.jremoting.requests.Ping) request).setSession(session);
        }

        try {
            while (true) {
                boolean again = true;
                Response response = null;
                int tries = 0;
                long start = 0;

                if (methodLogging) {
                    start = System.currentTimeMillis();
                }

                while (again) {
                    tries++;

                    again = false;

                    try {
                        response = objectDriver.postRequest(request);

                        if (response instanceof ProblemResponse) {

                            if (response instanceof TryLater) {
                                int millis = ((TryLater) response).getSuggestedDelayMillis();

                                clientMonitor.serviceSuspended(this.getClass(), request, tries, millis);

                                again = true;
                            } else if (response instanceof NoSuchReference) {
                                throw new NoSuchReferenceException(((NoSuchReference) response).getReferenceID());
                            } else if (response instanceof NoSuchSession) {
                                throw new NoSuchSessionException(((NoSuchSession) response).getSessionID());
                            } else if (response instanceof NotPublished) {
                                Servicable pnr = (Servicable) request;

                                throw new NotPublishedException(pnr.getService(), pnr.getObjectName());
                            }
                        }
                        if (response instanceof ConnectionOpened) {
                            session = ((ConnectionOpened) response).getSessionID();
                        }
                    } catch (IOException ioe) {
                        if (isSafeEnd(ioe)) {
                            int retryConnectTries = 0;

                            again = true;

                            while (!tryReconnect()) {
                                clientMonitor.serviceAbend(this.getClass(), retryConnectTries, ioe);

                                retryConnectTries++;
                            }
                        } else {
                            throw clientMonitor.unexpectedIOException(StatefulClientInvoker.class, "invoke()", ioe);
                        }
                    }
                }
                if (methodLogging) {
                    if (request instanceof InvokeMethod) {
                        clientMonitor.methodCalled(this.getClass(), ((InvokeMethod) request).getMethodSignature(), System.currentTimeMillis() - start, "");
                    }
                }

                return response;
            }
        } catch (ClassNotFoundException e) {
            throw new InvocationException("Class definition missing on Deserialization: " + e.getMessage(), e);
        }
    }

    private boolean isSafeEnd(IOException ioe) {
        if (ioe instanceof SocketException | ioe instanceof EOFException | ioe instanceof InterruptedIOException) {
            return true;
        }
        if (ioe.getMessage() != null) {
            String msg = ioe.getMessage();
            if (msg.equals("Read end dead") | msg.equals("Pipe closed")) {
                return true;
            }
        }
        return false;
    }

    public long getLastRealRequestTime() {
        return lastRealRequest;
    }
}
