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
package org.codehaus.jremoting.client.transports.rmi;

import org.codehaus.jremoting.BadConnectionException;
import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.RmiInvoker;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.pingers.NeverConnectionPinger;
import org.codehaus.jremoting.client.transports.StatefulClientInvoker;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.ConnectIOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class RmiClientInvoker
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public final class RmiClientInvoker extends StatefulClientInvoker {

    private RmiInvoker rmiInvoker;
    private String url;

    public RmiClientInvoker(ClientMonitor clientMonitor, ScheduledExecutorService executorService, ConnectionPinger connectionPinger, String host, int port) throws ConnectionException {

        super(clientMonitor, executorService, connectionPinger, RmiClientInvoker.class.getClassLoader());

        url = "rmi://" + host + ":" + port + "/" + RmiInvoker.class.getName();

        try {
            rmiInvoker = (RmiInvoker) Naming.lookup(url);
        } catch (NotBoundException nbe) {
            throw new ConnectionException("Cannot bind to the remote RMI service.  Either an IP or RMI issue.");
        } catch (MalformedURLException mfue) {
            throw new ConnectionException("Malformed URL, host/port (" + host + "/" + port + ") must be wrong: " + mfue.getMessage());
        } catch (ConnectIOException cioe) {
            throw new BadConnectionException("Cannot connect to remote RMI server. " + "It is possible that transport mismatch");
        } catch (RemoteException re) {
            throw new ConnectionException("Unknown Remote Exception : " + re.getMessage());
        }
    }

    public RmiClientInvoker(ClientMonitor clientMonitor, String host, int port) throws ConnectionException {
        this(clientMonitor, Executors.newScheduledThreadPool(10), new NeverConnectionPinger(), host, port);

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
        return rmiInvoker.invoke(request);
    }
}
