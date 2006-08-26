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
package org.codehaus.jremoting.client.transports.direct;

import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.client.NoSuchReferenceException;
import org.codehaus.jremoting.client.NotPublishedException;
import org.codehaus.jremoting.client.transports.AbstractClientInvocationHandler;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.responses.NoSuchReference;
import org.codehaus.jremoting.responses.NotPublished;
import org.codehaus.jremoting.requests.AbstractPublishedNameRequest;
import org.codehaus.jremoting.requests.AbstractRequest;
import org.codehaus.jremoting.requests.RequestConstants;
import org.codehaus.jremoting.responses.AbstractResponse;
import org.codehaus.jremoting.responses.TryLater;

import java.io.IOException;

/**
 * Class DirectInvocationHandler
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public abstract class AbstractDirectInvocationHandler extends AbstractClientInvocationHandler {

    protected boolean methodLogging = false;
    protected long lastRealRequest = System.currentTimeMillis();


    public AbstractDirectInvocationHandler(ThreadPool threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger) {
        super(threadPool, clientMonitor, connectionPinger);
    }

    /**
     * Method handleInvocation
     *
     * @param request
     * @return
     */
    public AbstractResponse handleInvocation(AbstractRequest request) {

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
                response = performInvocation(request);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            //if ((response instanceof ProblemReply))  // slower by 11%
            if (response.getResponseCode() >= 100) {
                if (response instanceof TryLater) {
                    int millis = ((TryLater) response).getSuggestedDelayMillis();

                    clientMonitor.serviceSuspended(this.getClass(), request, tries, millis);

                    again = true;
                } else if (response instanceof NoSuchReference) {
                    throw new NoSuchReferenceException(((NoSuchReference) response).getReferenceID());
                } else if (response instanceof NotPublished) {
                    AbstractPublishedNameRequest pnr = (AbstractPublishedNameRequest) request;

                    throw new NotPublishedException(pnr.getPublishedServiceName(), pnr.getObjectName());
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

    protected boolean tryReconnect() {

        // blimey how do we reconnect this?
        throw new InvocationException("Direct connection broken, unable to reconnect.");
    }

    /**
     * Method getLastRealRequest
     *
     * @return
     */
    public long getLastRealRequest() {
        return lastRealRequest;
    }

    protected abstract AbstractResponse performInvocation(AbstractRequest request) throws IOException;
}
