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

import org.codehaus.jremoting.api.RmiInvocationHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.server.*;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;
import org.codehaus.jremoting.server.authenticators.DefaultAuthenticator;
import org.codehaus.jremoting.server.classretrievers.PlainStubRetriever;
import org.codehaus.jremoting.server.monitors.NullServerMonitor;
import org.codehaus.jremoting.server.transports.AbstractServer;
import org.codehaus.jremoting.server.transports.DefaultServerSideClientContextFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Class RmiServer for serving of 'over RMI'
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class RmiServer extends AbstractServer {

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
     * @param invocationHandlerAdapter
     * @param serverMonitor
     * @param executor
     * @param contextFactory
     * @param port
     */
    public RmiServer(InvocationHandlerAdapter invocationHandlerAdapter, ServerMonitor serverMonitor, ExecutorService executor, ServerSideClientContextFactory contextFactory, int port) {
        super(invocationHandlerAdapter, serverMonitor, executor, contextFactory);
        this.port = port;
    }

    public RmiServer(StubRetriever stubRetriever, Authenticator authenticator, ServerMonitor serverMonitor, ExecutorService executor, ServerSideClientContextFactory contextFactory, int port) {
        this(new InvocationHandlerAdapter(stubRetriever, authenticator, serverMonitor, contextFactory), serverMonitor, executor, contextFactory, port);
    }

    public RmiServer(int port) {
        this(new PlainStubRetriever(), new DefaultAuthenticator(), new NullServerMonitor(), Executors.newCachedThreadPool(), new DefaultServerSideClientContextFactory(), port);
    }

    /**
     * Start the server.
     *
     * @throws ServerException if an exception during starting.
     */
    public void start() throws ServerException {
        setState(STARTING);
        try {
            rmiInvocationAdapter = new RmiInvocationAdapter(this);

            UnicastRemoteObject.exportObject(rmiInvocationAdapter);

            registry = LocateRegistry.createRegistry(port);

            registry.rebind(RmiInvocationHandler.class.getName(), rmiInvocationAdapter);
            setState(STARTED);
        } catch (RemoteException re) {
            throw new ServerException("Some problem setting up RMI server", re);
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
