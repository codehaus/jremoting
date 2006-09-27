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
    private boolean keepGoing = true;
    private Runnable runnable;
    private Future future;
    private final long pingInterval;
    private final long giveupInterval;

    /**
     * Construct a IntervalConnectionPinger with seconds for interval.
     *
     * @param pingIntervalSeconds   the interval to wait
     * @param giveupIntervalSeconds when to give up
     */
    public IntervalConnectionPinger(int pingIntervalSeconds, int giveupIntervalSeconds) {
        pingInterval = pingIntervalSeconds * 1000;
        giveupInterval = giveupIntervalSeconds * 1000;
    }

    /**
     * Construct a IntervalConnectionPinger with millisecond intervals
     *
     * @param pingIntervalMilliSeconds   the interval to wait
     * @param giveupIntervalMilliSeconds when to give up
     */
    public IntervalConnectionPinger(long pingIntervalMilliSeconds, long giveupIntervalMilliSeconds) {
        pingInterval = pingIntervalMilliSeconds;
        giveupInterval = giveupIntervalMilliSeconds;
    }


    /**
     * Constructor IntervalConnectionPinger
     */
    public IntervalConnectionPinger() {
        pingInterval = 10 * 1000;       // ten seconds
        giveupInterval = 100 * 1000;    // one hundred seconds.
    }

    /**
     * Method setInvocationHandler
     */
    public void setInvocationHandler(ClientInvocationHandler invocationHandler) {
        clientInvocationHandler = invocationHandler;
    }

    /**
     * Get the Invocation handler.
     *
     * @return
     */
    protected ClientInvocationHandler getInvocationHandler() {
        return clientInvocationHandler;
    }

    /**
     * Start the pinger
     */
    public void start() {

        runnable = new Runnable() {
            public void run() {

                try {
                    while (keepGoing) {
                        Thread.sleep(pingInterval);
                        if (keepGoing) {
                            ping();
                        }
                    }
                } catch (InvocationException ie) {
                    clientInvocationHandler.getClientMonitor().invocationFailure(this.getClass(), this.getClass().getName(), ie);
                    // no need to ping anymore.
                } catch (ConnectionClosedException cce) {
                    clientInvocationHandler.getClientMonitor().unexpectedConnectionClosed(this.getClass(), this.getClass().getName(), cce);
                    // no need to ping anymore.
                } catch (InterruptedException e) {
                    if (keepGoing) {
                        clientInvocationHandler.getClientMonitor().unexpectedInterruption(this.getClass(), this.getClass().getName(), e);
                    }
                }
            }
        };

        future = clientInvocationHandler.getExecutorService().submit(runnable);
    }

    /**
     * Stop the pinger
     */
    public void stop() {
        keepGoing = false;
        future.cancel(true);
        future = null;
    }

    protected abstract void ping();

    public long getGiveupInterval() {
        return giveupInterval;
    }
}