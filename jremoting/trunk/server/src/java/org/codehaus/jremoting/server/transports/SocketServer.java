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

import org.codehaus.jremoting.JRemotingException;
import org.codehaus.jremoting.server.*;
import org.codehaus.jremoting.server.adapters.DefaultServerDelegate;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.streams.ByteStream;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.transports.ConnectingServer;
import org.codehaus.jremoting.server.transports.RunningConnection;

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
 *
 */
public class SocketServer extends ConnectingServer {

    private ServerSocket serverSocket;
    private Future daemon;
    private final InetSocketAddress addr;
    private final Stream Stream;
    private final ClassLoader facadesClassLoader;
    private int socketTimeout = 60 * 1000;
    protected boolean accepting = true;

    public SocketServer(ServerMonitor serverMonitor, InetSocketAddress addr) {
        this(serverMonitor, defaultExecutor(), addr);
    }

    public SocketServer(ServerMonitor serverMonitor, Authenticator authenticator, InetSocketAddress addr) {
        this(serverMonitor, addr, defaultExecutor(), authenticator);
    }


    public SocketServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, InetSocketAddress addr) {
        this(serverMonitor, stubRetriever, defaultAuthenticator(), defaultStream(), defaultExecutor(),
                defaultContextFactory(), addr);
    }

    public SocketServer(ServerMonitor serverMonitor, Stream Stream, InetSocketAddress port) {
        this(serverMonitor, defaultExecutor(), Stream, port);
    }

    public SocketServer(ServerMonitor serverMonitor, ScheduledExecutorService executorService, InetSocketAddress addr) {
        this(serverMonitor, executorService, defaultStream(), addr);
    }

    public SocketServer(ServerMonitor serverMonitor, ScheduledExecutorService executorService, Stream Stream, InetSocketAddress addr) {
        this(serverMonitor, defaultStubRetriever(), defaultAuthenticator(), Stream, executorService, defaultContextFactory(), defaultClassLoader(), addr);
    }

    public SocketServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator, Stream Stream, ScheduledExecutorService executorService, ServerContextFactory serverContextFactory, InetSocketAddress addr) {
        this(serverMonitor, stubRetriever, authenticator, Stream, executorService, serverContextFactory, defaultClassLoader(), addr);
    }

    public SocketServer(ServerMonitor serverMonitor, InetSocketAddress addr, ScheduledExecutorService executorService, Authenticator authenticator) {
        this(serverMonitor, defaultStubRetriever(), authenticator, defaultStream(), executorService, defaultContextFactory(), addr);
    }

    public SocketServer(ServerMonitor serverMonitor, StubRetriever stubRetriever,
                        Authenticator authenticator,
                        Stream Stream,
                        ScheduledExecutorService executorService,
                        ServerContextFactory contextFactory,
                        ClassLoader facadesClassLoader, InetSocketAddress addr) {
        this(serverMonitor, defaultServerDelegate(serverMonitor, stubRetriever, authenticator, contextFactory),
                Stream, executorService, facadesClassLoader, addr);
    }

    public SocketServer(ServerMonitor serverMonitor, ServerDelegate serverDelegate,
                                           Stream Stream, ScheduledExecutorService executorService,
                                           ClassLoader facadesClassLoader, InetSocketAddress addr) {

        super(serverMonitor, serverDelegate, executorService);
        this.Stream = Stream;
        this.facadesClassLoader = facadesClassLoader;
        this.addr = addr;
    }

    private static ServerDelegate defaultServerDelegate(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator, ServerContextFactory contextFactory) {
        return new DefaultServerDelegate(serverMonitor, stubRetriever, authenticator, contextFactory);
    }

    public static ScheduledExecutorService defaultExecutor() {
        return Executors.newScheduledThreadPool(10);
    }

    public static Stream defaultStream() {
        return new ByteStream();
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public void stopping() {
        try {
            serverSocket.close();
        } catch (IOException ioe) {
            throw new JRemotingException("Error stopping Complete Socket server", ioe);
        }
        closeConnections();
        if (daemon != null) {
            daemon.cancel(true);
        }
        super.stopping();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
        public void run() {
            // see http://developer.java.sun.com/developer/bugParade/bugs/4508149.html
            try {
                socket.setSoTimeout(socketTimeout);
                if (getState().equals(STARTED)) {
                    RunningConnection src = new RunningConnection(SocketServer.this,
                            Stream.makeStreamConnection(serverMonitor, facadesClassLoader,
                                socket.getInputStream(), socket.getOutputStream(), socket.getInetAddress().toString()),
                            serverMonitor) {
                        public void closeConnection() {
                            super.closeConnection();
                            try {
                                socket.close();
                            } catch (IOException e) {
                                serverMonitor.closeError(this.getClass(), "SocketServer.closeConnection(): Error closing socket", e);
                            }
                        }

                    };
                    connectionStarting(src);
                    src.run();
                    connectionCompleted(src);
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

    public void setSocketTimeout(int millis) {
        this.socketTimeout = millis;
    }

}
