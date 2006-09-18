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

import org.codehaus.jremoting.api.ConnectionException;
import java.util.concurrent.ExecutorService;
import org.codehaus.jremoting.client.ClientInvocationHandler;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionClosedException;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.requests.Ping;
import org.codehaus.jremoting.responses.AbstractResponse;

/**
 * Class AbstractClientInvocationHandler
 *
 * @author Paul Hammant
 * @version $Revision: 1.3 $
 */
public abstract class AbstractClientInvocationHandler implements ClientInvocationHandler {

    protected final ConnectionPinger connectionPinger;
    protected final ClientMonitor clientMonitor;
    protected boolean stopped = false;
    protected final ExecutorService executor;
    protected final boolean methodLogging;


    public AbstractClientInvocationHandler(ExecutorService executor, ClientMonitor clientMonitor, ConnectionPinger connectionPinger) {
        this.executor = executor;
        this.clientMonitor = clientMonitor;
        methodLogging = clientMonitor.methodLogging();
        this.connectionPinger = connectionPinger;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public ClientMonitor getClientMonitor() {
        return clientMonitor;
    }

    public void initialize() throws ConnectionException {
        connectionPinger.setInvocationHandler(this);
        connectionPinger.start();
    }

    public void close() {

        connectionPinger.stop();

        stopped = true;
    }

    public void ping() {

        if (stopped) {
            throw new ConnectionClosedException("Connection closed");
        }

        AbstractResponse ar = handleInvocation(new Ping());
    }

    protected abstract boolean tryReconnect();

    public ClassLoader getInterfacesClassLoader() {
        return AbstractClientInvocationHandler.class.getClassLoader();
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
