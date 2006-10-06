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
package org.codehaus.jremoting.client.pingers;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.codehaus.jremoting.client.ClientInvocationHandler;
import org.codehaus.jremoting.client.ConnectionClosedException;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.InvocationException;

/**
 * Interface IntervalConnectionPinger
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public abstract class IntervalConnectionPinger implements ConnectionPinger {

    private ClientInvocationHandler clientInvocationHandler;
    private Future future;
    private final long pingInterval;

    public IntervalConnectionPinger(int pingIntervalSeconds) {
        pingInterval = pingIntervalSeconds;
    }

    public void setInvocationHandler(ClientInvocationHandler invocationHandler) {
        clientInvocationHandler = invocationHandler;
    }

    protected ClientInvocationHandler getInvocationHandler() {
        return clientInvocationHandler;
    }

    public void start() {

        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    ping();
                } catch (InvocationException ie) {
                    clientInvocationHandler.getClientMonitor().invocationFailure(this.getClass(), "n/a", "n/a", "n/a", ie);
                    // no need to ping anymore?
                } catch (ConnectionClosedException cce) {
                    clientInvocationHandler.getClientMonitor().unexpectedConnectionClosed(this.getClass(), this.getClass().getName(), cce);
                    // no need to ping anymore?
                }
            }
        };

        future = clientInvocationHandler.getScheduledExecutorService().scheduleAtFixedRate(runnable, pingInterval, pingInterval, TimeUnit.SECONDS);

    }

    public void stop() {
        future.cancel(true);
        future = null;
    }

    protected abstract void ping();

}
