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

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.ConnectionRefusedException;
import org.codehaus.jremoting.client.StreamEncoding;
import org.codehaus.jremoting.client.pingers.NeverConnectionPinger;
import org.codehaus.jremoting.client.transports.StreamTransport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class SocketTransport
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class SocketTransport extends StreamTransport {

    private final InetSocketAddress addr;

    public SocketTransport(ClientMonitor clientMonitor, ScheduledExecutorService executorService,
                           ConnectionPinger connectionPinger, ClassLoader facadesClassLoader,
                           StreamEncoding streamEncoding,
                           InetSocketAddress addr) throws ConnectionException {
        this(clientMonitor, executorService, connectionPinger, facadesClassLoader, streamEncoding, addr, defaultSocketTimeout());
    }

    public SocketTransport(ClientMonitor clientMonitor, ScheduledExecutorService executorService,
                           ConnectionPinger connectionPinger, ClassLoader facadesClassLoader,
                           StreamEncoding streamEncoding,
                           InetSocketAddress addr, int socketTimeout) throws ConnectionRefusedException, ConnectionException {
        super(clientMonitor, executorService, connectionPinger, facadesClassLoader, streamEncoding);
        this.addr = addr;

        try {
            Socket socket = new Socket(addr.getHostName(), addr.getPort());
            socket.setSoTimeout(socketTimeout);
            setStreamEncoder(streamEncoding.makeStreamEncoder(socket.getInputStream(), socket.getOutputStream(), getFacadesClassLoader()));
        } catch (IOException ioe) {
            if (ioe.getMessage().startsWith("Connection refused")) {
                throw new ConnectionRefusedException("Connection to port " + addr.getPort() + " on host " + addr.getHostName() + " refused.");
            }
            throw new ConnectionException("Cannot open Stream(s) for socket: " + ioe.getMessage());
        }
    }


    public SocketTransport(ClientMonitor clientMonitor, StreamEncoding streamEncoding, InetSocketAddress addr) throws ConnectionRefusedException, ConnectionException {
        this(clientMonitor, Executors.newScheduledThreadPool(10), new NeverConnectionPinger(),
                Thread.currentThread().getContextClassLoader(), streamEncoding, addr);
    }


    public SocketTransport(ClientMonitor clientMonitor, StreamEncoding streamEncoding, InetSocketAddress addr, int socketTimeout) throws ConnectionRefusedException, ConnectionException {
        this(clientMonitor, Executors.newScheduledThreadPool(10), new NeverConnectionPinger(),
                Thread.currentThread().getContextClassLoader(), streamEncoding, addr, socketTimeout);
    }

    private static int defaultSocketTimeout() {
        return 45*1000;
    }


    protected boolean tryReconnect() {

        try {
            Socket socket = makeSocket(addr);
            socket.setSoTimeout(60 * 1000);
            setStreamEncoder(streamEncoding.makeStreamEncoder(socket.getInputStream(), socket.getOutputStream(), getFacadesClassLoader()));
            return true;
        } catch (IOException ce) {
            return false;
        }
    }

    protected Socket makeSocket(InetSocketAddress addr) throws IOException {
        return new Socket(addr.getHostName(), addr.getPort());
    }

}
