/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
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
package org.codehaus.jremoting.client.transports.socket;

import org.codehaus.jremoting.client.factories.AbstractSocketStreamHostContext;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.pingers.NeverConnectionPinger;
import org.codehaus.jremoting.client.pingers.DefaultConnectionPinger;
import org.codehaus.jremoting.client.monitors.NullClientMonitor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.api.ConnectionException;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Method;

/**
 * Class SocketCustomStreamHostContext
 *
 * @author Paul Hammant
 * @version $Revision: 1.3 $
 */
public class SocketXStreamHostContext extends AbstractSocketStreamHostContext {

    private int port;

    /**
     * Constructor SocketCustomStreamHostContext
     *
     * @param executor
     * @param clientMonitor
     * @param connectionPinger
     * @param host
     * @param port
     * @throws org.codehaus.jremoting.api.ConnectionException
     */
    public SocketXStreamHostContext(ExecutorService executor, ClientMonitor clientMonitor, ConnectionPinger connectionPinger, ClassLoader interfacesClassLoader, String host, int port) throws ConnectionException {
        super(executor, clientMonitor, connectionPinger, new SocketXStreamInvocationHandler(executor, clientMonitor, connectionPinger, interfacesClassLoader, host, port));
        this.port = port;
    }

    public SocketXStreamHostContext(String host, int port, ClassLoader classLoader) throws ConnectionException {
        this(Executors.newCachedThreadPool(), new NullClientMonitor(), new DefaultConnectionPinger(), classLoader, host, port);
    }

    public SocketXStreamHostContext(String host, int port) throws ConnectionException {
        this(Executors.newCachedThreadPool(), new NullClientMonitor(), new DefaultConnectionPinger(), SocketXStreamHostContext.class.getClassLoader(), host, port);
    }


    private Object bind(Object object, PipedInputStream inputStream, PipedOutputStream outputStream) {

        try {
            Object[] parms = new Object[]{inputStream, outputStream};
            Method method = object.getClass().getMethod("bind", new Class[]{parms.getClass()});
            return method.invoke(object, new Object[]{parms});
        } catch (Exception e) {
            return null;
        }
    }


}
