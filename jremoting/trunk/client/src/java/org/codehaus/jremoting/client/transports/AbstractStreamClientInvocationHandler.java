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

import java.util.concurrent.ExecutorService;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ClientStreamDriver;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.client.NoSuchReferenceException;
import org.codehaus.jremoting.client.NoSuchSessionException;
import org.codehaus.jremoting.client.NotPublishedException;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.responses.NotPublished;
import org.codehaus.jremoting.requests.AbstractServiceRequest;
import org.codehaus.jremoting.requests.AbstractRequest;
import org.codehaus.jremoting.requests.RequestConstants;
import org.codehaus.jremoting.responses.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;

/**
 * Class AbstractStreamClientInvocationHandler
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public abstract class AbstractStreamClientInvocationHandler extends AbstractClientInvocationHandler {

    private ClientStreamDriver objectDriver;
    private boolean methodLogging = false;
    private long lastRealRequest = System.currentTimeMillis();
    protected final ClassLoader interfacesClassLoader;

    /**
     * Constructor AbstractStreamClientInvocationHandler
     *
     * @param executor
     * @param clientMonitor
     * @param connectionPinger
     * @param interfacesClassLoader
     */
    public AbstractStreamClientInvocationHandler(ExecutorService executor, ClientMonitor clientMonitor, ConnectionPinger connectionPinger, ClassLoader interfacesClassLoader) {
        super(executor, clientMonitor, connectionPinger);
        this.interfacesClassLoader = interfacesClassLoader;
    }

    public ClassLoader getInterfacesClassLoader() {
        return interfacesClassLoader;
    }

    protected void setObjectDriver(ClientStreamDriver objectDriver) {
        this.objectDriver = objectDriver;
    }

    protected void requestWritten() {
    }

    public synchronized AbstractResponse handleInvocation(AbstractRequest request) {
        if (request.getRequestCode() != RequestConstants.PINGREQUEST) {
            lastRealRequest = System.currentTimeMillis();
        }

        try {
            while (true) {
                boolean again = true;
                AbstractResponse response = null;
                int tries = 0;
                long start = 0;

                if (methodLogging) {
                    start = System.currentTimeMillis();
                }

                while (again) {
                    tries++;

                    again = false;

                    try {
                        long t1 = System.currentTimeMillis();

                        response = (AbstractResponse) objectDriver.postRequest(request);

                        long t2 = System.currentTimeMillis();

                        if (response.getResponseCode() >= 100) {

                            if (response instanceof TryLater) {
                                int millis = ((TryLater) response).getSuggestedDelayMillis();

                                clientMonitor.serviceSuspended(this.getClass(), request, tries, millis);

                                again = true;
                            } else if (response instanceof NoSuchReference) {
                                throw new NoSuchReferenceException(((NoSuchReference) response).getReferenceID());
                            } else if (response instanceof NoSuchSession) {
                                throw new NoSuchSessionException(((NoSuchSession) response).getSessionID());
                            } else if (response instanceof NotPublished) {
                                AbstractServiceRequest pnr = (AbstractServiceRequest) request;

                                throw new NotPublishedException(pnr.getService(), pnr.getObjectName());
                            }
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
                            throw clientMonitor.unexpectedIOException(AbstractClientInvocationHandler.class, "handleInvocation()", ioe);
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

    public long getLastRealRequest() {
        return lastRealRequest;
    }
}
