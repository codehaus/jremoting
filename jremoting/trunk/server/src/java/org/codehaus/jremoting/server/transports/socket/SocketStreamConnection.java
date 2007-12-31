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
import org.codehaus.jremoting.server.encoders.StreamEncoder;
import org.codehaus.jremoting.server.transports.ConnectingServer;
import org.codehaus.jremoting.server.transports.StreamConnection;

import java.io.IOException;
import java.net.Socket;

/**
 * Class SocketStreamConnection
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class SocketStreamConnection extends StreamConnection {

    /**
     * The socket for the connection
     */
    private Socket socket;

    /**
     * Construct a Socket Stream Server Connection
     *
     * @param connectingServer The Abstract Server that will process invocations and requests
     * @param socket         The Socket
     * @param encoder         The encoder for the transport type
     */
    public SocketStreamConnection(final ConnectingServer connectingServer, final Socket socket, StreamEncoder encoder, ServerMonitor serverMonitor) {

        super(connectingServer, encoder, serverMonitor);
        this.socket = socket;
    }

    /**
     * Kill connections
     */
    protected void killConnection() {

        try {
            socket.close();
        } catch (IOException e) {
            serverMonitor.closeError(this.getClass(), "SocketStreamConnection.killConnection(): Error closing Connection", e);
        }
    }
}
