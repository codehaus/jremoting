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
package org.codehaus.jremoting.client.transports;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.Stream;
import org.codehaus.jremoting.client.*;
import org.codehaus.jremoting.client.pingers.TimingOutPinger;
import org.codehaus.jremoting.client.transports.StreamTransport;

import java.io.IOException;
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

    private Stream Stream;
    private final SocketDetails addr;

    public SocketTransport(ClientMonitor clientMonitor, ScheduledExecutorService executorService,
                           ConnectionPinger connectionPinger, ClassLoader facadesClassLoader,
                           Stream Stream,
                           SocketDetails addr) throws ConnectionException {
        this(clientMonitor, executorService, connectionPinger, facadesClassLoader, Stream, addr, defaultSocketTimeout());
    }

    public SocketTransport(ClientMonitor clientMonitor, ScheduledExecutorService executorService,
                           ConnectionPinger connectionPinger, ClassLoader facadesClassLoader,
                           Stream Stream,
                           SocketDetails addr, int socketTimeout) throws ConnectionException {
        super(clientMonitor, executorService, connectionPinger, facadesClassLoader);
        this.Stream = Stream;
        this.addr = addr;

        try {
            for (int x = 0; x < addr.getConcurrentConnections(); x++) {
                Socket socket = makeSocket(addr);
                socket.setSoTimeout(socketTimeout);
                addStreamEncoder(Stream.makeStreamConnection(socket.getInputStream(), socket.getOutputStream(), getFacadesClassLoader()));
            }
        } catch (IOException ioe) {
            if (ioe.getMessage().startsWith("Connection refused")) {
                throw new ConnectionRefusedException("Connection to port " + addr.getPort() + " on host " + addr.getHostName() + " refused.");
            }
            throw new ConnectionException("Cannot open Stream(s) for socket: " + ioe.getMessage());
        }
    }


    public SocketTransport(ClientMonitor clientMonitor, Stream Stream, SocketDetails addr) throws ConnectionException {
        this(clientMonitor, Executors.newScheduledThreadPool(10), new TimingOutPinger(),
                Thread.currentThread().getContextClassLoader(), Stream, addr);
    }


    public SocketTransport(ClientMonitor clientMonitor, Stream Stream, SocketDetails addr, int socketTimeout) throws ConnectionException {
        this(clientMonitor, Executors.newScheduledThreadPool(10), new TimingOutPinger(),
                Thread.currentThread().getContextClassLoader(), Stream, addr, socketTimeout);
    }

    private static int defaultSocketTimeout() {
        return 45*1000;
    }


    protected boolean tryReconnect() {

        try {
            Socket socket = makeSocket(addr);
            socket.setSoTimeout(60 * 1000);
            addStreamEncoder(Stream.makeStreamConnection(socket.getInputStream(), socket.getOutputStream(), getFacadesClassLoader()));
            return true;
        } catch (IOException ce) {
            return false;
        }
    }

    protected Socket makeSocket(SocketDetails addr) throws IOException {
        return new Socket(addr.getHostName(), addr.getPort());
    }

}
