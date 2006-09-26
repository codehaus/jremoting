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
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;
import org.codehaus.jremoting.server.transports.ConnectingServer;
import org.codehaus.jremoting.server.transports.AbstractServerStreamDriver;
import org.codehaus.jremoting.server.transports.ServerStreamDriverFactory;
import org.codehaus.jremoting.server.transports.ServerStreamDriver;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author Peter Royal
 * @version $Revision: 1.2 $
 */
public class AbstractPartialSocketStreamServer extends ConnectingServer {
    private final ServerStreamDriverFactory serverStreamDriverFactory;

    /**
     * Construct a AbstractPartialSocketStreamServer
     *
     * @param invocationHandlerAdapter Use this invocation handler adapter.
     * @param serverMonitor            The server Monitor
     */
    public AbstractPartialSocketStreamServer(InvocationHandlerAdapter invocationHandlerAdapter, ServerMonitor serverMonitor, ExecutorService executorService,
                                             ServerStreamDriverFactory serverStreamDriverFactory) {
        super(serverMonitor, invocationHandlerAdapter, executorService);
        this.serverStreamDriverFactory = serverStreamDriverFactory;
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
                ServerStreamDriver ssd = serverStreamDriverFactory.createDriver(serverMonitor, executorService,
                        socket.getInputStream(), socket.getOutputStream(), socket);
                SocketStreamConnection sssc = new SocketStreamConnection(this, socket, ssd, serverMonitor);
                sssc.run();
            }
        } catch (IOException ioe) {

            serverMonitor.unexpectedException(this.getClass(), "AbstractPartialSocketStreamServer.handleConnection(): Some problem connecting " + "client via sockets: ", ioe);
        }
    }

}
