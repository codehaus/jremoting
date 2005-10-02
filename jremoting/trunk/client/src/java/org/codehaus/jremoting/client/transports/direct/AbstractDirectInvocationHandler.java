/* ====================================================================
 * Copyright 2005 JRemoting Committers
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
import org.codehaus.jremoting.commands.MethodRequest;
import org.codehaus.jremoting.commands.NoSuchReferenceResponse;
import org.codehaus.jremoting.commands.NotPublishedResponse;
import org.codehaus.jremoting.commands.PublishedNameRequest;
import org.codehaus.jremoting.commands.Request;
import org.codehaus.jremoting.commands.RequestConstants;
import org.codehaus.jremoting.commands.Response;
import org.codehaus.jremoting.commands.TryLaterResponse;

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
    public Response handleInvocation(Request request) {

        if (request.getRequestCode() != RequestConstants.PINGREQUEST) {
            lastRealRequest = System.currentTimeMillis();
        }

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
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            //if ((response instanceof ProblemReply))  // slower by 11%
            if (response.getResponseCode() >= 100) {
                if (response instanceof TryLaterResponse) {
                    int millis = ((TryLaterResponse) response).getSuggestedDelayMillis();

                    clientMonitor.serviceSuspended(this.getClass(), request, tries, millis);

                    again = true;
                } else if (response instanceof NoSuchReferenceResponse) {
                    throw new NoSuchReferenceException(((NoSuchReferenceResponse) response).getReferenceID());
                } else if (response instanceof NotPublishedResponse) {
                    PublishedNameRequest pnr = (PublishedNameRequest) request;

                    throw new NotPublishedException(pnr.getPublishedServiceName(), pnr.getObjectName());
                }
            }
        }

        if (methodLogging) {
            if (request instanceof MethodRequest) {
                clientMonitor.methodCalled(this.getClass(), ((MethodRequest) request).getMethodSignature(), System.currentTimeMillis() - start, "");
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

    protected abstract Response performInvocation(Request request) throws IOException;
}
