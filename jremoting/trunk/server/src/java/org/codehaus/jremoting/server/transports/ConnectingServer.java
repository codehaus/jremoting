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
package org.codehaus.jremoting.server.transports;

import org.codehaus.jremoting.server.Connection;
import org.codehaus.jremoting.server.ServerDelegate;
import org.codehaus.jremoting.server.ServerMonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class ConnectingServer
 *
 * @author Paul Hammant
 * @author Vinay Chandrasekharan
 *
 */
public abstract class ConnectingServer extends StatefulServer {

    private final List<Connection> connections = new ArrayList<Connection>();
    private ScheduledFuture pruner;
    private int pruneStaleLongerThan = 5 * 60 * 1000;
    private int pruneSessionInterval = 100;
    protected final ScheduledExecutorService executorService;

    public void setPruneStaleLongerThan(int millis) {
        this.pruneStaleLongerThan = millis;
    }

    public void setPruneSessionsInterval(int seconds) {
        this.pruneSessionInterval = seconds;
    }

    public ConnectingServer(ServerMonitor serverMonitor, ServerDelegate serverDelegate,
                            ScheduledExecutorService executorService) {
        super(serverMonitor, serverDelegate);
        this.executorService = executorService;
    }

    /**
     * {@inheritDoc}
     */
    public void started() {
        pruner =  executorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                serverDelegate.pruneSessionsStaleForLongerThan(pruneStaleLongerThan);
            }
        }, pruneSessionInterval, pruneSessionInterval, TimeUnit.SECONDS);
        super.started();
    }

    /**
     * {@inheritDoc}
     */
    public void stopping() {
        pruner.cancel(true);
        closeConnections();
        super.stopping();
    }

    protected void connectionStarting(Connection connection) {
        if (connection == null) {
            throw new NullPointerException("whoaa!");
        }
        synchronized (connections) {
            connections.add(connection);
        }
    }

    protected void connectionCompleted(Connection connection) {
        synchronized (connections) {
            connections.remove(connection);
        }
    }

    protected void closeConnections() {
        // Copy the connections into an array to avoid ConcurrentModificationExceptions
        //  as the connections are closed.
        Connection[] connections;
        synchronized (this.connections) {
            connections = this.connections.toArray(new Connection[0]);
        }
        for (Connection connection : connections) {
            connection.closeConnection();
        }
    }



}
