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
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.StubRetriever;
import org.codehaus.jremoting.server.adapters.InvokerDelegate;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.context.ServerContextFactory;
import org.codehaus.jremoting.server.factories.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.transports.ByteStreamEncoding;
import org.codehaus.jremoting.server.transports.StreamEncoding;
import org.codehaus.jremoting.server.transports.StreamEncoder;
import org.codehaus.jremoting.server.transports.ConnectingServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class SocketStreamServer
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class SocketStreamServer extends ConnectingServer {

    private ServerSocket serverSocket;
    private Future future;
    private final int port;
    private final StreamEncoding streamEncoding;
    private final ClassLoader facadesClassLoader;

    protected boolean accepting = true;


    /**
     * Construct a SocketStreamServer
     *
     * @param serverMonitor
     * @param invokerDelegate The invocation handler adapter to use.
     * @param port                     The port to use
     */
    public SocketStreamServer(ServerMonitor serverMonitor, InvokerDelegate invokerDelegate,
                                           StreamEncoding streamEncoding, ScheduledExecutorService executorService,
                                           ClassLoader facadesClassLoader, int port) {

        super(serverMonitor, invokerDelegate, executorService);
        this.streamEncoding = streamEncoding;
        this.facadesClassLoader = facadesClassLoader;
        this.port = port;
    }

    public SocketStreamServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator,
                                           StreamEncoding streamEncoding, ScheduledExecutorService executorService,
                                           ServerContextFactory contextFactory,
                                           ClassLoader facadesClassLoader, int port) {
        this(serverMonitor, new InvokerDelegate(serverMonitor, stubRetriever, authenticator, contextFactory),
                streamEncoding, executorService, facadesClassLoader, port);
    }

    public SocketStreamServer(ServerMonitor serverMonitor, int port) {
        this(serverMonitor, port, dftExecutor());
    }

    public SocketStreamServer(ServerMonitor serverMonitor, int port, StubRetriever stubRetriever) {
        this(serverMonitor, stubRetriever, dftAuthenticator(), dftDriverFactory(), dftExecutor(),
                dftContextFactory(), port);
    }

    public SocketStreamServer(ServerMonitor serverMonitor, int port, ScheduledExecutorService executorService) {
        this(serverMonitor, port, executorService, dftDriverFactory());
    }

    public SocketStreamServer(ServerMonitor serverMonitor, int port, ScheduledExecutorService executorService,
                                           StreamEncoding streamEncoding) {
        this(serverMonitor, dftStubRetriever(), dftAuthenticator(), streamEncoding, executorService, dftContextFactory(), thisClassLoader(), port);
    }

    public SocketStreamServer(ServerMonitor serverMonitor, int port, StreamEncoding streamEncoding) {
        this(serverMonitor, port, dftExecutor(), streamEncoding);
    }

    public SocketStreamServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator, StreamEncoding streamEncoding, ScheduledExecutorService executorService, ServerContextFactory serverContextFactory, int port) {
        this(serverMonitor, stubRetriever, authenticator, streamEncoding, executorService, serverContextFactory, thisClassLoader(), port);
    }


    private static ScheduledExecutorService dftExecutor() {
        return Executors.newScheduledThreadPool(10);
    }

    private static StreamEncoding dftDriverFactory() {
        return new ByteStreamEncoding();
    }

    private static ClassLoader thisClassLoader() {
        return SocketStreamServer.class.getClassLoader();
    }

    private static StubRetriever dftStubRetriever() {
        return new RefusingStubRetriever();
    }

    private static Authenticator dftAuthenticator() {
        return new NullAuthenticator();
    }

    private static ServerContextFactory dftContextFactory() {
        return new ThreadLocalServerContextFactory();
    }

    public void starting() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ioe) {
            throw new JRemotingException("Could not bind to port '"+port+"'when setting up the server", ioe);
        }
        super.starting();
    }

    public void started() {
        super.started();
        future = executorService.submit(new Runnable() {
            public void run() {

                boolean accepting = false;
                try {
                    while (getState().equals(STARTED)) {

                        accepting = true;
                        Socket sock = serverSocket.accept();
                        handleConnection(sock);

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
        if (future != null) {
            future.cancel(true);
        }
        super.stopping();
    }

    public void stopped() {
        super.stopped();
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
