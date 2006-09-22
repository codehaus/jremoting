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

package org.codehaus.jremoting.server.transports.socket;

import java.util.concurrent.ExecutorService;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;
import org.codehaus.jremoting.server.transports.AbstractServer;
import org.codehaus.jremoting.server.transports.AbstractServerStreamDriver;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author Peter Royal
 * @version $Revision: 1.2 $
 */
public abstract class AbstractPartialSocketStreamServer extends AbstractServer {

    /**
     * Construct a AbstractPartialSocketStreamServer
     *
     * @param invocationHandlerAdapter Use this invocation handler adapter.
     * @param serverMonitor            The server Monitor
     */
    public AbstractPartialSocketStreamServer(InvocationHandlerAdapter invocationHandlerAdapter, ServerMonitor serverMonitor, ExecutorService executor, ServerSideClientContextFactory contextFactory) {
        super(invocationHandlerAdapter, serverMonitor, executor, contextFactory);
    }

    /**
     * Handle a connection.
     *
     * @param socket The socket for the connection
     */
    public void handleConnection(final Socket socket) {

        // see http://developer.java.sun.com/developer/bugParade/bugs/4508149.html
        try {
            socket.setSoTimeout(60 * 1000);
        } catch (SocketException se) {
            serverMonitor.unexpectedException(this.getClass(), "AbstractPartialSocketStreamServer.handleConnection(): Some error during " + "socket handling", se);
        }

        try {
            if (getState() == STARTED) {
                AbstractServerStreamDriver ssd = createServerStreamDriver();

                ssd.setStreams(socket.getInputStream(), socket.getOutputStream(), socket);

                SocketStreamServerConnection sssc = new SocketStreamServerConnection(this, socket, ssd, serverMonitor);

                sssc.run();
            }
        } catch (IOException ioe) {

            serverMonitor.unexpectedException(this.getClass(), "AbstractPartialSocketStreamServer.handleConnection(): Some problem connecting " + "client via sockets: ", ioe);
        }
    }

    /**
     * Create a Server Stream Driver.
     *
     * @return The Server Stream Driver.
     */
    protected abstract AbstractServerStreamDriver createServerStreamDriver();

    /**
     * Method start
     */
    public void start() {
        setState(STARTED);
    }

    /**
     * Method stop
     */
    public void stop() {

        setState(SHUTTINGDOWN);

        killAllConnections();

        setState(STOPPED);
    }
}
