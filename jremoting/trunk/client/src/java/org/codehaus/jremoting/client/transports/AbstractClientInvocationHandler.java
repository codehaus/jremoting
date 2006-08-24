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

import org.codehaus.jremoting.api.CallbackException;
import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.client.ClientInvocationHandler;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionClosedException;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.commands.PingRequest;
import org.codehaus.jremoting.commands.Response;

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
    protected final ThreadPool threadPool;
    protected final boolean methodLogging;


    public AbstractClientInvocationHandler(ThreadPool threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger) {
        this.threadPool = threadPool;
        this.clientMonitor = clientMonitor;
        methodLogging = clientMonitor.methodLogging();
        this.connectionPinger = connectionPinger;
    }

    public ThreadPool getThreadPool() {
        return threadPool;
    }

    public ClientMonitor getClientMonitor() {
        return clientMonitor;
    }

    /**
     * Method initialize
     *
     * @throws ConnectionException
     */
    public void initialize() throws ConnectionException {
        connectionPinger.setInvocationHandler(this);
        connectionPinger.start();
    }

    /**
     * Method close
     */
    public void close() {

        connectionPinger.stop();

        stopped = true;
    }

    /**
     * Method ping
     */
    public void ping() {

        if (stopped) {
            throw new ConnectionClosedException("Connection closed");
        }

        Response ar = handleInvocation(new PingRequest());
    }

    protected abstract boolean tryReconnect();

    /**
     * Method getInterfacesClassLoader
     *
     * @return
     */
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

    public boolean isCallBackEnabled() {
        return false;
    }

    public boolean exposeObject(Object exposedObject, Class exposedInterface) throws CallbackException {
        throw new UnsupportedOperationException();
    }

    public String getPublishedName(Object exposedObject) {
        throw new UnsupportedOperationException();
    }

}
