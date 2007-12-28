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

import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.adapters.InvokerDelegate;
import org.codehaus.jremoting.server.transports.ConnectingServer;
import org.codehaus.jremoting.server.transports.StreamEncoder;
import org.codehaus.jremoting.server.transports.StreamEncoding;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Peter Royal
 * @version $Revision: 1.2 $
 */
public abstract class SocketStreamServer extends ConnectingServer {
    private final StreamEncoding streamEncoding;
    private final ClassLoader facadesClassLoader;

    protected boolean accepting = true;
    /**
     * Construct a SocketStreamServer
     *
     * @param serverMonitor            The server Monitor
     * @param invokerDelegate Use this invocation handler adapter.
     */
    public SocketStreamServer(ServerMonitor serverMonitor, InvokerDelegate invokerDelegate, ScheduledExecutorService executorService,
                              StreamEncoding streamEncoding, ClassLoader facadesClassLoader) {
        super(serverMonitor, invokerDelegate, executorService);
        this.streamEncoding = streamEncoding;
        this.facadesClassLoader = facadesClassLoader;
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
            if (getState().equals(STARTED)) {
                StreamEncoder streamEncoder = streamEncoding.createEncoder(serverMonitor, facadesClassLoader,
                        socket.getInputStream(), socket.getOutputStream(), socket);
                SocketStreamConnection ssc = new SocketStreamConnection(this, socket, streamEncoder, serverMonitor);
                ssc.run();
            }
        } catch (IOException ioe) {
            handleIOE(accepting, ioe);
        }
    }

    protected void handleIOE(boolean accepting, IOException ioe) {
        // some JVM revisions report 'socket closed' , some 'Soclet closed'
        if (accepting & ioe.getMessage().equalsIgnoreCase("socket closed")) {
            // do nothing, server shut down during accept();
        } else {
            serverMonitor.unexpectedException(this.getClass(), "SocketStreamServer: Some problem connecting client via sockets: " + ioe.getMessage(), ioe);
        }
    }
}
