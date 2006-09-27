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
package org.codehaus.jremoting.client.transports.socket;

import org.codehaus.jremoting.ConnectionException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.factories.AbstractSocketStreamHostContext;
import org.codehaus.jremoting.client.factories.AbstractHostContext;
import org.codehaus.jremoting.client.pingers.NeverConnectionPinger;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Method;

/**
 * Class SocketCustomStreamHostContext
 *
 * @author Paul Hammant
 * @version $Revision: 1.3 $
 */
public class SocketCustomStreamHostContext extends AbstractHostContext {

    /**
     * Constructor SocketCustomStreamHostContext
     *
     * @param clientMonitor
     * @param executorService
     * @param connectionPinger
     * @param host
     * @param port
     * @throws ConnectionException
     */
    public SocketCustomStreamHostContext(ClientMonitor clientMonitor, ExecutorService executorService, ConnectionPinger connectionPinger, ClassLoader interfacesClassLoader, String host, int port) throws ConnectionException {
        super(new SocketCustomStreamInvocationHandler(clientMonitor, executorService, connectionPinger, interfacesClassLoader, host, port));
    }

    public SocketCustomStreamHostContext(ClientMonitor clientMonitor, String host, int port, ClassLoader classLoader) throws ConnectionException {
        this(clientMonitor, Executors.newCachedThreadPool(), new NeverConnectionPinger(), classLoader, host, port);
    }

    public SocketCustomStreamHostContext(ClientMonitor clientMonitor, String host, int port) throws ConnectionException {
        this(clientMonitor, Executors.newCachedThreadPool(), new NeverConnectionPinger(), SocketCustomStreamHostContext.class.getClassLoader(), host, port);
    }

}
