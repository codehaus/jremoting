/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
 * Portions copyright 2001 - 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.codehaus.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.jremoting.client.transports;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.RmiInvoker;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.SocketDetails;
import org.codehaus.jremoting.client.pingers.TimingOutPinger;
import org.codehaus.jremoting.client.transports.StatefulTransport;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.ConnectIOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class RmiTransport
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public final class RmiTransport extends StatefulTransport {

    private RmiInvoker rmiInvoker;
    private String url;

    public RmiTransport(ClientMonitor clientMonitor, ScheduledExecutorService executorService, ConnectionPinger connectionPinger, SocketDetails addr) throws ConnectionException {

        super(clientMonitor, executorService, connectionPinger, RmiTransport.class.getClassLoader());

        url = "rmi://" + addr.getHostName() + ":" + addr.getPort() + "/" + RmiInvoker.class.getName();

        try {
            rmiInvoker = (RmiInvoker) Naming.lookup(url);
        } catch (NotBoundException nbe) {
            throw new ConnectionException("Cannot bind to the remote RMI service.  Either an IP or RMI issue.");
        } catch (MalformedURLException mfue) {
            throw new ConnectionException("Malformed URL, host/port (" + addr.getHostName() + "/" + addr.getPort() + ") must be wrong: " + mfue.getMessage());
        } catch (ConnectIOException cioe) {
            throw new ConnectionException("Cannot connect to remote RMI server. " + "It is possible that transport mismatch", cioe);
        } catch (RemoteException re) {
            throw new ConnectionException("Unknown Remote Exception : " + re.getMessage());
        }
    }

    public RmiTransport(ClientMonitor clientMonitor, ConnectionPinger connectionPinger, SocketDetails addr) throws ConnectionException {
        this(clientMonitor, defaultScheduledThreadPool(), connectionPinger, addr);
    }

    public RmiTransport(ClientMonitor clientMonitor, SocketDetails addr) throws ConnectionException {
        this(clientMonitor, defaultScheduledThreadPool(), defaultConnectionPinger(), addr);
    }

    public static ConnectionPinger defaultConnectionPinger() {
        return new TimingOutPinger();
    }

    public static ScheduledExecutorService defaultScheduledThreadPool() {
        return Executors.newScheduledThreadPool(10);
    }

    protected boolean tryReconnect() {
        try {
            rmiInvoker = (RmiInvoker) Naming.lookup(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected Response performInvocation(Request request) throws IOException, ClassNotFoundException {
        try {
            return rmiInvoker.invoke(request);
        } catch (ServerNotActiveException e) {
            throw new ConnectionException("Rmi:ServerNotActiveException:" + e.getMessage(),e); 
        }
    }
}
