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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.codehaus.jremoting.api.JRemotingException;
import org.codehaus.jremoting.server.Authenticator;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;
import org.codehaus.jremoting.server.StubRetriever;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.classretrievers.NoStubRetriever;
import org.codehaus.jremoting.server.transports.ConnectingServer;
import org.codehaus.jremoting.server.transports.DefaultServerSideClientContextFactory;
import org.codehaus.jremoting.server.transports.ServerCustomStreamDriverFactory;
import org.codehaus.jremoting.server.transports.ServerObjectStreamDriverFactory;
import org.codehaus.jremoting.server.transports.ServerStreamDriver;
import org.codehaus.jremoting.server.transports.ServerStreamDriverFactory;
import org.codehaus.jremoting.server.transports.ServerXStreamDriverFactory;

/**
 * Class SelfContainedSocketObjectStreamServer
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class SelfContainedSocketStreamServer extends ConnectingServer implements Runnable {

    /**
     * The server socket.
     */
    private ServerSocket serverSocket;

    /**
     * The thread handling the listening
     */
    private Future future;
    //TODO cannot be instance variable because of setStreams()
//    private final ServerStreamDriver serverStreamDriver;
    private int port;
    public static final String OBJECTSTREAM = "objectstream";
    public static final String CUSTOMSTREAM = "customstream";
    public static final String XSTREAM = "xstream";
    private final ServerStreamDriverFactory serverStreamDriverFactory;

    /**
     * Construct a SelfContainedSocketStreamServer
     *
     * @param serverMonitor
     * @param invocationHandlerAdapter The invocation handler adapter to use.
     * @param port                     The port to use
     */
    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, InvocationHandlerAdapter invocationHandlerAdapter,
                                           ServerStreamDriverFactory serverStreamDriverFactory, ExecutorService executor, int port) {

        super(serverMonitor, invocationHandlerAdapter, executor);
        this.serverStreamDriverFactory = serverStreamDriverFactory;
        this.port = port;
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator,
                                           ServerStreamDriverFactory serverStreamDriverFactory, ExecutorService executor,
                                           ServerSideClientContextFactory contextFactory, int port) {
        this(serverMonitor, new InvocationHandlerAdapter(serverMonitor, stubRetriever, authenticator, contextFactory), serverStreamDriverFactory, executor, port);
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, int port) {
        this(serverMonitor, port, Executors.newCachedThreadPool());
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, int port, ExecutorService executorService) {
        this(serverMonitor, port, executorService, new ServerCustomStreamDriverFactory());
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, int port, ExecutorService executorService, ServerStreamDriverFactory serverStreamDriverFactory) {
        this(serverMonitor, new NoStubRetriever(), new NullAuthenticator(), serverStreamDriverFactory, executorService, new DefaultServerSideClientContextFactory(), port);
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, int port, String streamType) {
        this(serverMonitor, port, Executors.newCachedThreadPool(), streamType);
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, int port, ExecutorService executorService, String streamType) {
        this(serverMonitor, port, executorService, createZerverStreamDriverFactory(streamType));
    }

    private static ServerStreamDriverFactory createZerverStreamDriverFactory(String streamType) {
        if (streamType.equals(CUSTOMSTREAM)) {
            return new ServerCustomStreamDriverFactory();
        } else if (streamType.equals(OBJECTSTREAM)) {
            return new ServerObjectStreamDriverFactory();
        } else if (streamType.equals(XSTREAM)) {
            return new ServerXStreamDriverFactory();
        }
        throw new IllegalArgumentException("streamType can only be '"+CUSTOMSTREAM+"', '"+OBJECTSTREAM+"' or '"+XSTREAM+"' ");
    }


    /**
     * Method run
     */
    public void run() {

        boolean accepting = false;
        try {
            while (getState() == STARTED) {

                accepting = true;
                Socket sock = serverSocket.accept();
                accepting = false;

                // see http://developer.java.sun.com/developer/bugParade/bugs/4508149.html
                sock.setSoTimeout(60 * 1000);

                ServerStreamDriver serverStreamDriver = serverStreamDriverFactory.createDriver(serverMonitor,
                        executorService, sock.getInputStream(), sock.getOutputStream(), sock);

                SocketStreamConnection sssc = new SocketStreamConnection(this, sock, serverStreamDriver, serverMonitor);

                //TODO ? Two of these getExecutors? PH
                getExecutorService().execute(sssc);

            }
        } catch (IOException ioe) {
            // some JVM revisions report 'socket closed' , some 'Soclet closed'
            if (accepting & ioe.getMessage().equalsIgnoreCase("socket closed")) {
                // do nothing, server shut down during accept();
            } else {
                serverMonitor.unexpectedException(this.getClass(), "SelfContainedSocketStreamServer.run(): Some problem connecting client via sockets: " + ioe.getMessage(), ioe);
            }
        }
    }

    /**
     * Method start
     */
    public void start() {

        setState(STARTING);
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ioe) {
            throw new JRemotingException("Could not bind to port '"+port+"'when setting up the server", ioe);
        }
        setState(STARTED);
        future = getExecutorService().submit(this);
    }

    /**
     * Method stop
     */
    public void stop() {

        if (getState() != STARTED) {
            throw new JRemotingException("Server Not Started at time of stop");
        }

        setState(SHUTTINGDOWN);
        try {
            serverSocket.close();
        } catch (IOException ioe) {
            throw new JRemotingException("Error stopping Complete Socket server", ioe);
        }
        killAllConnections();
        if (future != null) {
            future.cancel(true);
        }

        setState(STOPPED);
    }
}
