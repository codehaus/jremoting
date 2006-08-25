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

import org.codehaus.jremoting.ThreadContext;
import org.codehaus.jremoting.api.JRemotingRuntimeException;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.server.ServerException;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;
import org.codehaus.jremoting.server.transports.AbstractServer;
import org.codehaus.jremoting.server.transports.AbstractServerStreamDriver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class CompleteSocketObjectStreamServer
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public abstract class AbstractCompleteSocketStreamServer extends AbstractServer implements Runnable {

    /**
     * The server socket.
     */
    private ServerSocket serverSocket;

    /**
     * The thread handling the listening
     */
    private ThreadContext threadContext;
    private int port;

    /**
     * Construct a AbstractCompleteSocketStreamServer
     *
     * @param invocationHandlerAdapter The invocation handler adapter to use.
     * @param port                     The port to use
     * @param serverMonitor
     */
    public AbstractCompleteSocketStreamServer(InvocationHandlerAdapter invocationHandlerAdapter, ServerMonitor serverMonitor, ThreadPool threadPool, ServerSideClientContextFactory contextFactory, int port) {

        super(invocationHandlerAdapter, serverMonitor, threadPool, contextFactory);
        this.port = port;
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
                sock.setSoTimeout(36000);

                AbstractServerStreamDriver ssrw = createServerStreamReadWriter();

                ssrw.setStreams(sock.getInputStream(), sock.getOutputStream(), sock);

                SocketStreamServerConnection sssc = new SocketStreamServerConnection(this, sock, ssrw, serverMonitor);

                //TODO ? Two of these getThreadContexts? PH
                ThreadContext threadContext = getThreadPool().getThreadContext(sssc);

                threadContext.start();

            }
        } catch (IOException ioe) {
            // some JVM revisions report 'socket closed' , some 'Soclet closed'
            if (accepting & ioe.getMessage().equalsIgnoreCase("socket closed")) {
                // do nothing, server shut down during accept();
            } else {
                serverMonitor.unexpectedException(this.getClass(), "AbstractCompleteSocketStreamServer.run(): Some problem connecting client via sockets: " + ioe.getMessage(), ioe);
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
        getThreadContext().start();
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
            throw new org.codehaus.jremoting.api.JRemotingRuntimeException("Error stopping Complete Socket server", ioe);
        }
        killAllConnections();
        getThreadContext().interrupt();

        setState(STOPPED);
    }

    /**
     * Get the thread used for connection processing
     *
     * @return
     */
    private ThreadContext getThreadContext() {

        if (threadContext == null) {
            threadContext = getThreadPool().getThreadContext(this);

        }

        return threadContext;
    }

    /**
     * Create a Server Stream Read Writer.
     *
     * @return The Server Stream Read Writer.
     */
    protected abstract AbstractServerStreamDriver createServerStreamReadWriter();
}
