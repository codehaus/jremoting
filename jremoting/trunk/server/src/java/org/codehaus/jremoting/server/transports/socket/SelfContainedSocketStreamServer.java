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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class SelfContainedSocketObjectStreamServer
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class SelfContainedSocketStreamServer extends SocketStreamServer {

    /**
     * The server socket.
     */
    private ServerSocket serverSocket;

    /**
     * The thread handling the listening
     */
    private Future future;
    private int port;
    public static final String OBJECTSTREAM = "objectstream";
    public static final String CUSTOMSTREAM = "customstream";
    public static final String XSTREAM = "xstream";

    /**
     * Construct a SelfContainedSocketStreamServer
     *
     * @param serverMonitor
     * @param invocationHandlerDelegate The invocation handler adapter to use.
     * @param port                     The port to use
     */
    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, InvokerDelegate invocationHandlerDelegate,
                                           StreamEncoding streamEncoding, ScheduledExecutorService executorService,
                                           ClassLoader facadesClassLoader, int port) {

        super(serverMonitor, invocationHandlerDelegate, executorService, streamEncoding, facadesClassLoader);
        this.port = port;
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator,
                                           StreamEncoding streamEncoding, ScheduledExecutorService executorService,
                                           ServerContextFactory contextFactory,
                                           ClassLoader facadesClassLoader, int port) {
        this(serverMonitor, new InvokerDelegate(serverMonitor, stubRetriever, authenticator, contextFactory),
                streamEncoding, executorService, facadesClassLoader, port);
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, int port) {
        this(serverMonitor, port, dftExecutor());
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, int port, StubRetriever stubRetriever) {
        this(serverMonitor, stubRetriever, dftAuthenticator(), dftDriverFactory(), dftExecutor(),
                dftContextFactory(), port);
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, int port, ScheduledExecutorService executorService) {
        this(serverMonitor, port, executorService, dftDriverFactory());
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, int port, ScheduledExecutorService executorService,
                                           StreamEncoding streamEncoding) {
        this(serverMonitor, dftStubRetriever(), dftAuthenticator(), streamEncoding, executorService, dftContextFactory(), thisClassLoader(), port);
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, int port, StreamEncoding streamEncoding) {
        this(serverMonitor, port, dftExecutor(), streamEncoding);
    }

    public SelfContainedSocketStreamServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator, StreamEncoding streamEncoding, ScheduledExecutorService executorService, ServerContextFactory serverContextFactory, int port) {
        this(serverMonitor, stubRetriever, authenticator, streamEncoding, executorService, serverContextFactory, thisClassLoader(), port);
    }


    private static ScheduledExecutorService dftExecutor() {
        return Executors.newScheduledThreadPool(10);
    }

    private static StreamEncoding dftDriverFactory() {
        return new ByteStreamEncoding();
    }

    private static ClassLoader thisClassLoader() {
        return SelfContainedSocketStreamServer.class.getClassLoader();
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
}
