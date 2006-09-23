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

import org.codehaus.jremoting.api.JRemotingRuntimeException;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.server.*;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.monitors.NullServerMonitor;
import org.codehaus.jremoting.server.authenticators.DefaultAuthenticator;
import org.codehaus.jremoting.server.classretrievers.BcelDynamicGeneratorStubRetriever;
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
public class SelfContainedSocketStreamServer extends AbstractServer implements Runnable {

    /**
     * The server socket.
     */
    private ServerSocket serverSocket;

    /**
     * The thread handling the listening
     */
    private Future future;
    private final ServerStreamDriver serverStreamDriver;
    private int port;
    public static final String OBJECTSTREAM = "objectstream";
    public static final String CUSTOMSTREAM = "customstream";
    public static final String XSTREAM = "xstream";

    /**
     * Construct a SelfContainedSocketStreamServer
     *
     * @param invocationHandlerAdapter The invocation handler adapter to use.
     * @param port                     The port to use
     * @param serverMonitor
     */
    public SelfContainedSocketStreamServer(InvocationHandlerAdapter invocationHandlerAdapter, ServerMonitor serverMonitor,
                                           ServerStreamDriver serverStreamDriver, ExecutorService executor, int port) {

        super(invocationHandlerAdapter, serverMonitor, executor);
        this.serverStreamDriver = serverStreamDriver;
        this.port = port;
    }

    public SelfContainedSocketStreamServer(StubRetriever stubRetriever, Authenticator authenticator, ServerMonitor serverMonitor,
                                           ServerStreamDriver serverStreamDriver, ExecutorService executor,
                                           ServerSideClientContextFactory contextFactory, int port) {
        this(new InvocationHandlerAdapter(stubRetriever, authenticator, serverMonitor, contextFactory), serverMonitor, serverStreamDriver, executor, port);
    }

    public SelfContainedSocketStreamServer(int port) {
        this(port, new NullServerMonitor(), Executors.newCachedThreadPool());
    }

    public SelfContainedSocketStreamServer(int port, ServerMonitor serverMonitor, ExecutorService executorService) {
        this(port, serverMonitor, executorService, new ServerCustomStreamDriver(serverMonitor, executorService));
    }

    public SelfContainedSocketStreamServer(int port, ServerMonitor serverMonitor, ExecutorService executorService, ServerStreamDriver serverStreamDriver) {
        this(new NoStubRetriever(), new DefaultAuthenticator(), serverMonitor, serverStreamDriver, executorService, new DefaultServerSideClientContextFactory(), port);
    }

    public SelfContainedSocketStreamServer(int port, String streamType) {
        this(port, new NullServerMonitor(), Executors.newCachedThreadPool(), streamType);
    }

    public SelfContainedSocketStreamServer(int port, ServerMonitor serverMonitor, ExecutorService executorService, String streamType) {
        this(port, serverMonitor, executorService, createServerStreamDriver(serverMonitor, executorService, streamType));
    }

    private static ServerStreamDriver createServerStreamDriver(ServerMonitor serverMonitor, ExecutorService executorService, String streamType) {
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

                //ServerStreamDriver ssd = createServerStreamDriver();

                serverStreamDriver.setStreams(sock.getInputStream(), sock.getOutputStream(), sock);

                SocketStreamServerConnection sssc = new SocketStreamServerConnection(this, sock, serverStreamDriver, serverMonitor);

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
    public void start() throws ServerException {

        setState(STARTING);
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ioe) {
            throw new ServerException("Could not bind to a socket when setting up the server", ioe);
        }
        setState(STARTED);
        future = getExecutor().submit(this);
    }

    /**
     * Method stop
     */
    public void stop() {

        if (getState() != STARTED) {
            throw new JRemotingRuntimeException("Server Not Started at time of stop");
        }

        setState(SHUTTINGDOWN);
        try {
            serverSocket.close();
        } catch (IOException ioe) {
            throw new JRemotingRuntimeException("Error stopping Complete Socket server", ioe);
        }
        killAllConnections();
        if (future != null) {
            future.cancel(true);
        }

        setState(STOPPED);
    }

    /**
     * Create a Server Stream Driver.
     *
     * @return The Server Stream Driver.
     */
   // protected abstract ServerStreamDriver createServerStreamDriver();
}
