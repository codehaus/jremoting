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
package org.codehaus.jremoting.client.transports.rmi;

import org.codehaus.jremoting.api.BadConnectionException;
import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.api.RmiInvocationHandler;
import java.util.concurrent.ExecutorService;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.client.NoSuchReferenceException;
import org.codehaus.jremoting.client.NotPublishedException;
import org.codehaus.jremoting.client.transports.AbstractClientInvocationHandler;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.responses.NoSuchReference;
import org.codehaus.jremoting.responses.NotPublished;
import org.codehaus.jremoting.requests.RequestConstants;
import org.codehaus.jremoting.responses.AbstractResponse;
import org.codehaus.jremoting.responses.TryLater;
import org.codehaus.jremoting.requests.AbstractServiceRequest;
import org.codehaus.jremoting.requests.AbstractRequest;

import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Class RmiClientInvocationHandler
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public final class RmiClientInvocationHandler extends AbstractClientInvocationHandler {

    private RmiInvocationHandler rmiInvocationHandler;
    private String url;
    private long lastRealRequest = System.currentTimeMillis();

    /**
     * Constructor RmiClientInvocationHandler
     *
     * @param host
     * @param port
     * @throws ConnectionException
     */
    public RmiClientInvocationHandler(ExecutorService executor, ClientMonitor clientMonitor, ConnectionPinger connectionPinger, String host, int port) throws ConnectionException {

        super(executor, clientMonitor, connectionPinger);

        url = "rmi://" + host + ":" + port + "/" + RmiInvocationHandler.class.getName();

        try {
            rmiInvocationHandler = (RmiInvocationHandler) Naming.lookup(url);
        } catch (NotBoundException nbe) {
            throw new ConnectionException("Cannot bind to the remote RMI service.  Either an IP or RMI issue.");
        } catch (MalformedURLException mfue) {
            throw new ConnectionException("Malformed URL, host/port (" + host + "/" + port + ") must be wrong: " + mfue.getMessage());
        } catch (ConnectIOException cioe) {
            throw new BadConnectionException("Cannot connect to remote RMI server. " + "It is possible that transport mismatch");
        } catch (RemoteException re) {
            throw new ConnectionException("Unknown Remote Exception : " + re.getMessage());
        }
    }

    /**
     * Method tryReconnect
     *
     * @return
     */
    protected boolean tryReconnect() {

        try {
            rmiInvocationHandler = (RmiInvocationHandler) Naming.lookup(url);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Method handleInvocation
     *
     * @param request
     * @return
     */
    public synchronized AbstractResponse handleInvocation(AbstractRequest request) {

        if (request.getRequestCode() != RequestConstants.PINGREQUEST) {
            lastRealRequest = System.currentTimeMillis();
        }

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
                response = rmiInvocationHandler.handleInvocation(request);

                if (response.getResponseCode() >= 100) {
                    if (response instanceof TryLater) {
                        int millis = ((TryLater) response).getSuggestedDelayMillis();

                        clientMonitor.serviceSuspended(this.getClass(), request, tries, millis);

                        again = true;
                    } else if (response instanceof NoSuchReference) {
                        throw new NoSuchReferenceException(((NoSuchReference) response).getReferenceID());
                    } else if (response instanceof NotPublished) {
                        AbstractServiceRequest pnr = (AbstractServiceRequest) request;

                        throw new NotPublishedException(pnr.getService(), pnr.getObjectName());
                    }
                }
            } catch (RemoteException re) {
                if (re instanceof ConnectException | re instanceof ConnectIOException) {
                    int retryConnectTries = 0;

                    rmiInvocationHandler = null;

                    while (!tryReconnect()) {
                        clientMonitor.serviceAbend(this.getClass(), retryConnectTries, re);

                        retryConnectTries++;
                    }
                } else {
                    throw new InvocationException("Unknown RMI problem : " + re.getMessage(), re);
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

    /**
     * Method getLastRealRequest
     *
     * @return
     */
    public long getLastRealRequest() {
        return lastRealRequest;
    }
}
