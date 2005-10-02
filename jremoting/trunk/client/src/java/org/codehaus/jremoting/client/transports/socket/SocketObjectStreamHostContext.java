/* ====================================================================
 * Copyright 2005 JRemoting Committers
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
package org.codehaus.jremoting.client.transports.socket;

import org.codehaus.jremoting.client.transports.piped.PipedObjectStreamHostContext;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.factories.AbstractHostContext;
import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.api.DefaultThreadPool;
import org.codehaus.jremoting.client.pingers.NeverConnectionPinger;
import org.codehaus.jremoting.client.monitors.DumbClientMonitor;
import org.codehaus.jremoting.client.factories.AbstractSameVmBindableHostContext;
import org.codehaus.jremoting.client.factories.AbstractHostContext;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Method;

/**
 * Class SocketObjectStreamHostContext
 *
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class SocketObjectStreamHostContext extends AbstractSameVmBindableHostContext
{

    private final ThreadPool threadPool;
    private final ClientMonitor clientMonitor;
    private final ConnectionPinger connectionPinger;
    private int port;
    private ClassLoader classLoader;

    /**
     * Constructor SocketObjectStreamHostContext
     *
     * @param threadPool
     * @param clientMonitor
     * @param connectionPinger
     * @param interfacesClassLoader
     * @param host
     * @param port
     * @throws ConnectionException
     */
    public SocketObjectStreamHostContext( ThreadPool threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger, ClassLoader interfacesClassLoader, String host, int port )
        throws ConnectionException
    {
        super(
                threadPool, clientMonitor, connectionPinger,
                new SocketObjectStreamInvocationHandler( threadPool, clientMonitor, connectionPinger, host, port, interfacesClassLoader )
        );
        this.threadPool = threadPool;
        this.clientMonitor = clientMonitor;
        this.connectionPinger = connectionPinger;
        classLoader = interfacesClassLoader;
        this.port = port;
    }

    public static class WithCurrentClassLoader extends SocketObjectStreamHostContext
    {
        /**
         *
         * @param threadPool
         * @param clientMonitor
         * @param connectionPinger
         * @param host
         * @param port
         * @throws ConnectionException
         */
        public WithCurrentClassLoader(ThreadPool threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger, String host, int port) throws ConnectionException
        {
            super(threadPool,
                    clientMonitor,
                    connectionPinger,
                    SocketObjectStreamHostContext.class.getClassLoader(),
                    host, port);
        }
    }

    public static class WithSimpleDefaults extends SocketObjectStreamHostContext
    {
        /**
         *
         * @param host
         * @param port
         * @throws ConnectionException
         */
        public WithSimpleDefaults(String host, int port) throws ConnectionException
        {
            super(new DefaultThreadPool(),
                    new DumbClientMonitor(),
                    new NeverConnectionPinger(),
                    SocketObjectStreamHostContext.class.getClassLoader(),
                    host, port);
        }
    }


    /**
     * Make a HostContext for this using SameVM connections nstead of socket based ones.
     * @return the HostContext
     * @throws ConnectionException if a problem
     */
    public AbstractHostContext makeSameVmHostContext() throws ConnectionException
    {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream();
        try
        {
            Object binder = getOptmization("port=" + port);
            if (binder == null)
            {
                return null;
            }
            Object bound = bind(binder, in, out);
            if (bound == null)
            {
                return null;
            }
            PipedObjectStreamHostContext pipedCustomStreamHostContext
                    = new PipedObjectStreamHostContext(threadPool, clientMonitor, connectionPinger, in, out, classLoader);
            pipedCustomStreamHostContext.initialize();
            return pipedCustomStreamHostContext;
        }
        catch (Exception e)
        {
            throw new ConnectionException("Naming exception during bind :" + e.getMessage());
        }
    }

    private Object bind(Object object, PipedInputStream inputStream,
                       PipedOutputStream outputStream)
    {

        try
        {
            Object[] parms = new Object[]{ inputStream, outputStream };
            Method method = object.getClass().getMethod("bind", new Class[] { parms.getClass() });
            return method.invoke(object, new Object[] { parms });
        }
        catch (Exception e)
        {
            return null;
        }
    }


}
