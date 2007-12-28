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

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.JRemotingException;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionClosedException;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.client.NoSuchReferenceException;
import org.codehaus.jremoting.client.NoSuchSessionException;
import org.codehaus.jremoting.client.NotPublishedException;
import org.codehaus.jremoting.client.Transport;
import org.codehaus.jremoting.requests.CloseConnection;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.requests.OpenConnection;
import org.codehaus.jremoting.requests.Ping;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.requests.Servicable;
import org.codehaus.jremoting.responses.ConnectionClosed;
import org.codehaus.jremoting.responses.ConnectionOpened;
import org.codehaus.jremoting.responses.NoSuchReference;
import org.codehaus.jremoting.responses.NoSuchSession;
import org.codehaus.jremoting.responses.NotPublished;
import org.codehaus.jremoting.responses.ProblemResponse;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.responses.TryLater;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class StatefulTransport
 *
 * @author Paul Hammant
 * @version $Revision: 1.3 $
 */
public abstract class StatefulTransport implements Transport {

    protected final ConnectionPinger connectionPinger;
    protected final ClientMonitor clientMonitor;
    private final ClassLoader facadesClassLoader;
    protected boolean stopped = false;
    protected final ScheduledExecutorService executorService;
    protected final boolean methodLogging;
    private Long session;
    private long lastRealRequest = System.currentTimeMillis();

    public StatefulTransport(ClientMonitor clientMonitor, ScheduledExecutorService executorService,
                                           ConnectionPinger connectionPinger, ClassLoader facadesClassLoader) {
        this.executorService = executorService;
        this.clientMonitor = clientMonitor;
        this.facadesClassLoader = facadesClassLoader;
        methodLogging = clientMonitor.methodLogging();
        this.connectionPinger = connectionPinger;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return executorService;
    }

    public ClientMonitor getClientMonitor() {
        return clientMonitor;
    }

    public ConnectionOpened openConnection() throws ConnectionException {
        Response resp = invoke(new OpenConnection(), true);
        connectionPinger.start(this);
        if (!(resp instanceof ConnectionOpened)) {
            throw new ConnectionException("Setting of host context blocked for reasons of unknown, server-side response: (" + resp.getClass().getName() + ")");
        }
        return (ConnectionOpened) resp;
    }

    public void closeConnection(Long session) {
        ConnectionClosed closed = (ConnectionClosed) invoke(new CloseConnection(session), true);
        connectionPinger.stop();
        // TODO check closed ?
        stopped = true;
    }

    public void ping() {

        if (stopped) {
            throw new ConnectionClosedException("Connection closed");
        }

        try {
            invoke(new Ping(), true);
        } catch (JRemotingException e) {
            clientMonitor.pingFailure(this.getClass(), e);
        }
    }

    protected abstract boolean tryReconnect();

    public ClassLoader getFacadesClassLoader() {
        return facadesClassLoader;
    }


    /**
     * resolveArgument can handle any changes that one has to  do to the arguments being
     * marshalled to the server.
     * Noop Default behaviour.
     *
     * @param remoteObjName
     * @param objClass
     * @param obj
     * @return Object
     */

    public Object resolveArgument(String remoteObjName, String methodSignature, Class objClass, Object obj) {
        return obj;
    }

    public synchronized Response invoke(Request request, boolean retry) {
        if (request instanceof Ping) {
            ((Ping) request).setSession(session);
        } else {
            lastRealRequest = System.currentTimeMillis();
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
                        response = performInvocation(request);

                        if (response instanceof ProblemResponse) {

                            if (response instanceof TryLater) {
                                int millis = ((TryLater) response).getSuggestedDelayMillis();

                                clientMonitor.serviceSuspended(this.getClass(), request, tries, millis);

                                again = true;
                            } else if (response instanceof NoSuchReference) {
                                throw new NoSuchReferenceException(((NoSuchReference) response).getReference());
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

                            while (retry && !tryReconnect()) {
                                clientMonitor.serviceAbend(this.getClass(), retryConnectTries, ioe);

                                retryConnectTries++;
                            }
                        } else {
                            throw clientMonitor.unexpectedIOException(StatefulTransport.class, "invoke()", ioe);
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

    protected abstract Response performInvocation(Request request) throws IOException, ClassNotFoundException;

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
