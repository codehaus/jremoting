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

import org.codehaus.jremoting.JRemotingException;
import org.codehaus.jremoting.RmiInvoker;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.Authenticator;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.StubRetriever;
import org.codehaus.jremoting.server.adapters.InvokerDelegate;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.ServerContextFactory;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.stubretrievers.FromClassLoaderStubRetriever;
import org.codehaus.jremoting.server.transports.ConnectingServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.net.InetSocketAddress;

/**
 * Class RmiServer for serving of 'over RMI'
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class RmiServer extends ConnectingServer {

    private InetSocketAddress addr;
    private Registry registry;

    public RmiServer(ServerMonitor serverMonitor, InvokerDelegate invokerDelegate, ScheduledExecutorService executorService, InetSocketAddress addr) {
        super(serverMonitor, invokerDelegate, executorService);
        this.addr = addr;
    }

    public RmiServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator, ScheduledExecutorService executorService, ServerContextFactory contextFactory, InetSocketAddress addr) {
        this(serverMonitor, new InvokerDelegate(serverMonitor, stubRetriever, authenticator, contextFactory), executorService, addr);
    }

    public RmiServer(ServerMonitor serverMonitor, InetSocketAddress addr) {
        this(serverMonitor, new FromClassLoaderStubRetriever(), new NullAuthenticator(), Executors.newScheduledThreadPool(10), new ThreadLocalServerContextFactory(), addr);
    }

    public void starting() {
        try {
            final ConnectingServer connectingServer1 = this;
            RmiInvoker rmiInvocationAdapter = new RmiInvokerImpl(connectingServer1);

            UnicastRemoteObject.exportObject(rmiInvocationAdapter);

            registry = LocateRegistry.createRegistry(addr.getPort());

            registry.rebind(RmiInvoker.class.getName(), rmiInvocationAdapter);
        } catch (RemoteException re) {
            throw new JRemotingException("Some problem setting up RMI server", re);
        }
        super.starting();
    }

    public void stopping() {
        killAllConnections();

        try {
            registry.unbind(RmiInvoker.class.getName());
        } catch (RemoteException re) {
            serverMonitor.stopServerError(this.getClass(), "RmiServer.stop(): Error stopping RMI server - RemoteException", re);
        } catch (NotBoundException nbe) {
            serverMonitor.stopServerError(this.getClass(), "RmiServer.stop(): Error stopping RMI server - NotBoundException", nbe);
        }
        super.stopping();
    }

    private static class RmiInvokerImpl implements RmiInvoker {
        private ConnectingServer connectingServer;

        public RmiInvokerImpl(ConnectingServer connectingServer) {
            this.connectingServer = connectingServer;
        }

        public Response invoke(Request request) throws RemoteException {
            return connectingServer.invoke(request, "RMI-TODO");
        }
    }
}
