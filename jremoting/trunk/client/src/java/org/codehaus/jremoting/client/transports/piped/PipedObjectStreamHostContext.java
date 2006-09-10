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
package org.codehaus.jremoting.client.transports.piped;

import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.api.DefaultThreadPool;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.factories.AbstractHostContext;
import org.codehaus.jremoting.client.monitors.NullClientMonitor;
import org.codehaus.jremoting.client.pingers.DefaultConnectionPinger;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Class PipedObjectStreamHostContext
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class PipedObjectStreamHostContext extends AbstractHostContext {

    /**
     * Constructor PipedObjectStreamHostContext
     *
     * @param threadPool
     * @param clientMonitor
     * @param connectionPinger
     * @param inputStream
     * @param outputStream
     * @param classLoader
     */
    public PipedObjectStreamHostContext(ThreadPool threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger, PipedInputStream inputStream, PipedOutputStream outputStream, ClassLoader classLoader) {
        super(new PipedObjectStreamInvocationHandler(threadPool, clientMonitor, connectionPinger, inputStream, outputStream, classLoader));
    }

    public PipedObjectStreamHostContext(PipedInputStream inputStream, PipedOutputStream outputStream) {
        this(new DefaultThreadPool(), new NullClientMonitor(), new DefaultConnectionPinger(), inputStream, outputStream, PipedObjectStreamHostContext.class.getClassLoader());
    }

    /**
     * Method initialize
     *
     * @throws ConnectionException If a problem
     */
    public void initialize() throws ConnectionException {
        (invocationHandler).initialize();
    }
}
