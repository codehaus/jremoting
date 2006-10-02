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
package org.codehaus.jremoting.server.transports.rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.JRemotingException;
import org.codehaus.jremoting.RmiInvocationHandler;
import org.codehaus.jremoting.server.Authenticator;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;
import org.codehaus.jremoting.server.StubRetriever;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.stubretrievers.PlainStubRetriever;
import org.codehaus.jremoting.server.transports.ConnectingServer;
import org.codehaus.jremoting.server.transports.DefaultServerSideClientContextFactory;

/**
 * Class RmiServer for serving of 'over RMI'
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class RmiServer extends ConnectingServer {

    /**
     * The invocation adapter
     */
    private RmiInvocationAdapter rmiInvocationAdapter;

    /**
     * The port
     */
    private int port;
    /**
     * The registry
     */
    private Registry registry;

    /**
     * Constructor a RmiServer with a preexiting invocation handler.
     *
     * @param serverMonitor
     * @param invocationHandlerAdapter
     * @param executorService
     * @param port
     */
    public RmiServer(ServerMonitor serverMonitor, InvocationHandlerAdapter invocationHandlerAdapter, ExecutorService executorService, int port) {
        super(serverMonitor, invocationHandlerAdapter, executorService);
        this.port = port;
    }

    public RmiServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator, ExecutorService executorService, ServerSideClientContextFactory contextFactory, int port) {
        this(serverMonitor, new InvocationHandlerAdapter(serverMonitor, stubRetriever, authenticator, contextFactory), executorService, port);
    }

    public RmiServer(ServerMonitor serverMonitor, int port) {
        this(serverMonitor, new PlainStubRetriever(), new NullAuthenticator(), Executors.newCachedThreadPool(), new DefaultServerSideClientContextFactory(), port);
    }

    /**
     * Start the server.
     *
     */
    public void start() {
        setState(STARTING);
        try {
            rmiInvocationAdapter = new RmiInvocationAdapter(this);

            UnicastRemoteObject.exportObject(rmiInvocationAdapter);

            registry = LocateRegistry.createRegistry(port);

            registry.rebind(RmiInvocationHandler.class.getName(), rmiInvocationAdapter);
            setState(STARTED);
        } catch (RemoteException re) {
            throw new JRemotingException("Some problem setting up RMI server", re);
        }
    }

    /**
     * Stop the server.
     */
    public void stop() {

        setState(SHUTTINGDOWN);

        killAllConnections();

        try {
            registry.unbind(RmiInvocationHandler.class.getName());
        } catch (RemoteException re) {
            serverMonitor.stopServerError(this.getClass(), "RmiServer.stop(): Error stopping RMI server - RemoteException", re);
        } catch (NotBoundException nbe) {
            serverMonitor.stopServerError(this.getClass(), "RmiServer.stop(): Error stopping RMI server - NotBoundException", nbe);
        }

        setState(STOPPED);
    }
}
