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
package org.codehaus.jremoting.client.transports.socket;

import org.codehaus.jremoting.api.BadConnectionException;
import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ClientStreamDriver;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.ConnectionRefusedException;
import org.codehaus.jremoting.client.transports.AbstractStreamClientInvocationHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Class SocketCustomStreamInvocationHandler
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public abstract class AbstractSocketStreamInvocationHandler extends AbstractStreamClientInvocationHandler {

    private final String host;
    private final int port;


    /**
     * AbstractSocketStreamInvocationHandler
     *
     * @param threadPool
     * @param clientMonitor
     * @param connectionPinger
     * @param interfacesClassLoader The class loader
     * @param host                  The host to connect to
     * @param port                  The port to conenct to
     */
    public AbstractSocketStreamInvocationHandler(ThreadPool threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger, ClassLoader interfacesClassLoader, String host, int port) throws ConnectionRefusedException, BadConnectionException {
        super(threadPool, clientMonitor, connectionPinger, interfacesClassLoader);
        this.host = host;
        this.port = port;

        try {
            Socket socket = makeSocket();

            setObjectReadWriter(createClientStreamReadWriter(socket.getInputStream(), socket.getOutputStream()));
        } catch (IOException ioe) {
            if (ioe.getMessage().startsWith("Connection refused")) {
                throw new ConnectionRefusedException("Connection to port " + port + " on host " + host + " refused.");
            }
            throw new BadConnectionException("Cannot open Stream(s) for socket: " + ioe.getMessage());
        }
    }

    /**
     * Method tryReconnect
     *
     * @return connected or not.
     */
    protected boolean tryReconnect() {

        try {
            Socket socket = makeSocket();

            setObjectReadWriter(createClientStreamReadWriter(socket.getInputStream(), socket.getOutputStream()));

            return true;
        } catch (ConnectionException ce) {
            // TODO log ?
            return false;
        } catch (IOException ce) {

            // TODO log ?
            return false;
        }
    }

    private Socket makeSocket() throws IOException {

        Socket socket = new Socket(host, port);

        // The 1 second value was causing unwanted timeouts when the remote
        //  method took more than a second to run or if either system was heavily
        //  loaded.
        //socket.setSoTimeout(1000);
        socket.setSoTimeout(60 * 1000);

        return socket;
    }

    protected abstract ClientStreamDriver createClientStreamReadWriter(InputStream in, OutputStream out) throws ConnectionException;
}
