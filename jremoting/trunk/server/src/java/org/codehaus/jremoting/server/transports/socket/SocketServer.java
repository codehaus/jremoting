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

import org.codehaus.jremoting.JRemotingException;
import org.codehaus.jremoting.server.Authenticator;
import org.codehaus.jremoting.server.ServerContextFactory;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.StreamEncoder;
import org.codehaus.jremoting.server.StreamEncoding;
import org.codehaus.jremoting.server.StubRetriever;
import org.codehaus.jremoting.server.adapters.DefaultServerDelegate;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.encoders.ByteStreamEncoding;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.transports.ConnectingServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class SocketServer
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class SocketServer extends ConnectingServer {

    private ServerSocket serverSocket;
    private Future daemon;
    private final InetSocketAddress addr;
    private final StreamEncoding streamEncoding;
    private final ClassLoader facadesClassLoader;
    private int socketTimeout = 60 * 1000;

    public void setSocketTimeout(int millis) {
        this.socketTimeout = millis;
    }

    protected boolean accepting = true;

    public SocketServer(ServerMonitor serverMonitor, InetSocketAddress addr) {
        this(serverMonitor, defaultExecutor(), addr);
    }

    public SocketServer(ServerMonitor serverMonitor, Authenticator authenticator, InetSocketAddress addr) {
        this(serverMonitor, addr, defaultExecutor(), authenticator);
    }


    public SocketServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, InetSocketAddress addr) {
        this(serverMonitor, stubRetriever, defaultAuthenticator(), defaultStreamEncoding(), defaultExecutor(),
                defaultContextFactory(), addr);
    }

    public SocketServer(ServerMonitor serverMonitor, StreamEncoding streamEncoding, InetSocketAddress port) {
        this(serverMonitor, defaultExecutor(), streamEncoding, port);
    }

    public SocketServer(ServerMonitor serverMonitor, ScheduledExecutorService executorService, InetSocketAddress addr) {
        this(serverMonitor, executorService, defaultStreamEncoding(), addr);
    }

    public SocketServer(ServerMonitor serverMonitor, ScheduledExecutorService executorService, StreamEncoding streamEncoding, InetSocketAddress addr) {
        this(serverMonitor, defaultStubRetriever(), defaultAuthenticator(), streamEncoding, executorService, defaultContextFactory(), defaultClassLoader(), addr);
    }

    public SocketServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator, StreamEncoding streamEncoding, ScheduledExecutorService executorService, ServerContextFactory serverContextFactory, InetSocketAddress addr) {
        this(serverMonitor, stubRetriever, authenticator, streamEncoding, executorService, serverContextFactory, defaultClassLoader(), addr);
    }

    public SocketServer(ServerMonitor serverMonitor, InetSocketAddress addr, ScheduledExecutorService executorService, Authenticator authenticator) {
        this(serverMonitor, defaultStubRetriever(), authenticator, defaultStreamEncoding(), executorService, defaultContextFactory(), addr);
    }

    public SocketServer(ServerMonitor serverMonitor, StubRetriever stubRetriever,
                        Authenticator authenticator,
                        StreamEncoding streamEncoding,
                        ScheduledExecutorService executorService,
                        ServerContextFactory contextFactory,
                        ClassLoader facadesClassLoader, InetSocketAddress addr) {
        this(serverMonitor, new DefaultServerDelegate(serverMonitor, stubRetriever, authenticator, contextFactory),
                streamEncoding, executorService, facadesClassLoader, addr);
    }    

    public SocketServer(ServerMonitor serverMonitor, DefaultServerDelegate serverDelegate,
                                           StreamEncoding streamEncoding, ScheduledExecutorService executorService,
                                           ClassLoader facadesClassLoader, InetSocketAddress addr) {

        super(serverMonitor, serverDelegate, executorService);
        this.streamEncoding = streamEncoding;
        this.facadesClassLoader = facadesClassLoader;
        this.addr = addr;
    }


    public static ScheduledExecutorService defaultExecutor() {
        return Executors.newScheduledThreadPool(10);
    }

    public static StreamEncoding defaultStreamEncoding() {
        return new ByteStreamEncoding();
    }

    public static ClassLoader defaultClassLoader() {
        return SocketServer.class.getClassLoader();
    }

    public static StubRetriever defaultStubRetriever() {
        return new RefusingStubRetriever();
    }

    public static Authenticator defaultAuthenticator() {
        return new NullAuthenticator();
    }

    public static ServerContextFactory defaultContextFactory() {
        return new ThreadLocalServerContextFactory();
    }

    public void starting() {
        try {
            serverSocket = makeServerSocket(addr);
        } catch (IOException ioe) {
            throw new JRemotingException("Could not bind to port '"+addr.getPort()+"', address '"+addr.getAddress()+"'when setting up the server", ioe);
        }
        super.starting();
    }

    protected ServerSocket makeServerSocket(InetSocketAddress addr) throws IOException {
        return new ServerSocket(addr.getPort(),50, addr.getAddress());
    }

    public void started() {
        super.started();
        daemon = executorService.submit(new Runnable() {
            public void run() {
                boolean accepting = false;
                try {
                    while (getState().equals(STARTED)) {

                        accepting = true;
                        Socket sock = serverSocket.accept();
                        executorService.submit(new SocketConnection(sock));
                    }
                } catch (IOException ioe) {
                    handleIOE(accepting, ioe);
                }
            }
        });

    }

    public void stopping() {
        try {
            serverSocket.close();
        } catch (IOException ioe) {
            throw new JRemotingException("Error stopping Complete Socket server", ioe);
        }
        killAllConnections();
        if (daemon != null) {
            daemon.cancel(true);
        }
        super.stopping();
    }

    public void stopped() {
        super.stopped();
    }

    public void redirect(String serviceName, String host, int port) {
        super.redirect(serviceName, host + ":" + port);
    }

    private class SocketConnection implements Runnable {
        Socket socket;

        private SocketConnection(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            // see http://developer.java.sun.com/developer/bugParade/bugs/4508149.html
            try {
                socket.setSoTimeout(socketTimeout);
                if (getState().equals(STARTED)) {
                    StreamEncoder streamEncoder = streamEncoding.createEncoder(serverMonitor, facadesClassLoader,
                            socket.getInputStream(), socket.getOutputStream(), socket.getInetAddress().toString());
                    SocketStreamConnection ssc = new SocketStreamConnection(SocketServer.this, socket, streamEncoder, serverMonitor);
                    ssc.run();
                }
            } catch (IOException ioe) {
                handleIOE(accepting, ioe);
            }
        }
    }


    protected void handleIOE(boolean accepting, IOException ioe) {
        // some JVM revisions report 'socket closed' , some 'Socket closed'
        if (accepting & ioe.getMessage().equalsIgnoreCase("socket closed")) {
            // do nothing, server shut down during accept();
        } else {
            serverMonitor.unexpectedException(this.getClass(), "SocketStreamServer: Some problem connecting client via sockets: " + ioe.getMessage(), ioe);
        }
    }

    


}
