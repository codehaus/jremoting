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

import org.codehaus.jremoting.api.ConnectionException;
import java.util.concurrent.ExecutorService;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ClientStreamDriver;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.transports.ClientObjectStreamDriver;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class SocketObjectStreamInvocationHandler
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public final class SocketObjectStreamInvocationHandler extends AbstractSocketStreamInvocationHandler {

    /**
     * Const a SocketObjectStreamInvocationHandler.
     *
     * @param threadPool
     * @param clientMonitor
     * @param connectionPinger
     * @param host
     * @param port
     * @param interfacesClassLoader
     * @throws ConnectionException
     */
    public SocketObjectStreamInvocationHandler(ExecutorService threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger, String host, int port, ClassLoader interfacesClassLoader) throws ConnectionException {

        super(threadPool, clientMonitor, connectionPinger, interfacesClassLoader, host, port);

        //TODO - is this a pending problem?  The superclass invokes createClientStreamDriver
        // perhaps an init() is needed.
    }

    /**
     * Create a client stream read/writer
     *
     * @param in  the input stream
     * @param out the output stream
     * @return the read/writer
     * @throws ConnectionException if a problem
     */
    protected ClientStreamDriver createClientStreamDriver(InputStream in, OutputStream out) throws ConnectionException {
        return new ClientObjectStreamDriver(in, out);
    }
}
