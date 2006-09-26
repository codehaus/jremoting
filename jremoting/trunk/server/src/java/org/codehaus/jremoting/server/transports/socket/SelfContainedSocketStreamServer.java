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

import org.codehaus.jremoting.api.JRemotingException;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.server.*;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.classretrievers.NoStubRetriever;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;
import org.codehaus.jremoting.server.transports.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
    private final ServerStreamDriver serverStreamDriver;
    private int port;
    public static final String OBJECTSTREAM = "objectstream";
    public static final String CUSTOMSTREAM = "customstream";
    public static final String XSTREAM = "xstream";

    /**
     * Construct a SelfContainedSocketStreamServer
     *
     * @param serverMonitor
     * @param invocationHandlerAdapter The invocation handler adapter to use.
     * @param port                     The port to use
     */
    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, InvocationHandlerAdapter invocationHandlerAdapter,
                                           ServerStreamDriver serverStreamDriver, ExecutorService executor, int port) {

        super(serverMonitor, invocationHandlerAdapter, executor);
        this.serverStreamDriver = serverStreamDriver;
        this.port = port;
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator,
                                           ServerStreamDriver serverStreamDriver, ExecutorService executor,
                                           ServerSideClientContextFactory contextFactory, int port) {
        this(serverMonitor, new InvocationHandlerAdapter(serverMonitor, stubRetriever, authenticator, contextFactory), serverStreamDriver, executor, port);
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, int port) {
        this(serverMonitor, port, Executors.newCachedThreadPool());
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, int port, ExecutorService executorService) {
        this(serverMonitor, port, executorService, new ServerCustomStreamDriver(serverMonitor, executorService));
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, int port, ExecutorService executorService, ServerStreamDriver serverStreamDriver) {
        this(serverMonitor, new NoStubRetriever(), new NullAuthenticator(), serverStreamDriver, executorService, new DefaultServerSideClientContextFactory(), port);
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, int port, String streamType) {
        this(serverMonitor, port, Executors.newCachedThreadPool(), streamType);
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, int port, ExecutorService executorService, String streamType) {
        this(serverMonitor, port, executorService, createZerverStreamDriver(serverMonitor, executorService, streamType));
    }

    private static ServerStreamDriver createZerverStreamDriver(ServerMonitor serverMonitor, ExecutorService executorService, String streamType) {
        if (streamType.equals(CUSTOMSTREAM)) {
            return new ServerCustomStreamDriver(serverMonitor, executorService);
        } else if (streamType.equals(OBJECTSTREAM)) {
            return new ServerObjectStreamDriver(serverMonitor, executorService);
        } else if (streamType.equals(XSTREAM)) {
            return new ServerXStreamDriver(serverMonitor, executorService);
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

                serverStreamDriver.setStreams(sock.getInputStream(), sock.getOutputStream(), sock);

                SocketStreamConnection sssc = new SocketStreamConnection(this, sock, serverStreamDriver, serverMonitor);

                //TODO ? Two of these getExecutors? PH
                getExecutor().execute(sssc);

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
        future = getExecutor().submit(this);
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
