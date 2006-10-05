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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.codehaus.jremoting.server.Connection;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;

/**
 * Class ConnectingServer
 *
 * @author Paul Hammant
 * @author Vinay Chandrasekharan <a href="mailto:vinayc77@yahoo.com">vinayc77@yahoo.com</a>
 * @version $Revision: 1.2 $
 */
public abstract class ConnectingServer extends StatefulServer {

    private List<Connection> connections = new ArrayList<Connection>();

    public ConnectingServer(ServerMonitor serverMonitor, InvocationHandlerAdapter invocationHandlerAdapter,
                            ScheduledExecutorService executor) {
        super(serverMonitor, invocationHandlerAdapter, executor);
    }


    public void stop() {
        setState(SHUTTINGDOWN);
        killAllConnections();
        super.stop();
    }


    protected void connectionStart(Connection connection) {
        connections.add(connection);
    }

    protected void connectionCompleted(Connection connection) {
        connections.remove(connection);
    }

    protected void killAllConnections() {
        // Copy the connections into an array to avoid ConcurrentModificationExceptions
        //  as the connections are closed.
        Connection[] connections = this.connections.toArray(new Connection[0]);
        for (Connection connection : connections) {
            connection.endConnection();
        }
    }



}
