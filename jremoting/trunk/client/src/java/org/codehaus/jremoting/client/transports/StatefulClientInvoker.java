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
import org.codehaus.jremoting.JRemotingException;
import org.codehaus.jremoting.client.ClientInvoker;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionClosedException;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.requests.CloseConnection;
import org.codehaus.jremoting.requests.OpenConnection;
import org.codehaus.jremoting.requests.Ping;
import org.codehaus.jremoting.responses.ConnectionClosed;
import org.codehaus.jremoting.responses.ConnectionOpened;
import org.codehaus.jremoting.responses.Response;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Class StatefulClientInvoker
 *
 * @author Paul Hammant
 * @version $Revision: 1.3 $
 */
public abstract class StatefulClientInvoker implements ClientInvoker {

    protected final ConnectionPinger connectionPinger;
    protected final ClientMonitor clientMonitor;
    private final ClassLoader facadesClassLoader;
    protected boolean stopped = false;
    protected final ScheduledExecutorService executorService;
    protected final boolean methodLogging;


    public StatefulClientInvoker(ClientMonitor clientMonitor, ScheduledExecutorService executorService,
                                           ConnectionPinger connectionPinger, ClassLoader facadesClassLoader) {
        this.executorService = executorService;
        this.clientMonitor = clientMonitor;
        this.facadesClassLoader = facadesClassLoader;
        methodLogging = clientMonitor.methodLogging();
        this.connectionPinger = connectionPinger;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return executorService;
    }

    public ClientMonitor getClientMonitor() {
        return clientMonitor;
    }

    public ConnectionOpened openConnection() throws ConnectionException {
        Response resp = invoke(new OpenConnection());
        connectionPinger.start(this);
        if (!(resp instanceof ConnectionOpened)) {
            throw new ConnectionException("Setting of host context blocked for reasons of unknown, server-side response: (" + resp.getClass().getName() + ")");
        }
        return (ConnectionOpened) resp;
    }

    public void closeConnection(Long sessionID) {
        ConnectionClosed closed = (ConnectionClosed) invoke(new CloseConnection(sessionID));
        connectionPinger.stop();
        // TODO check closed ?
        stopped = true;
    }

    public void ping() {

        if (stopped) {
            throw new ConnectionClosedException("Connection closed");
        }

        try {
            invoke(new Ping());
        } catch (JRemotingException e) {
            clientMonitor.pingFailure(this.getClass(), e);
        }
    }

    protected abstract boolean tryReconnect();

    public ClassLoader getFacadesClassLoader() {
        return facadesClassLoader;
    }


    /**
     * resolveArgument can handle any changes that one has to  do to the arguments being
     * marshalled to the server.
     * Noop Default behaviour.
     *
     * @param remoteObjName
     * @param objClass
     * @param obj
     * @return Object
     */

    public Object resolveArgument(String remoteObjName, String methodSignature, Class objClass, Object obj) {
        return obj;
    }

}
