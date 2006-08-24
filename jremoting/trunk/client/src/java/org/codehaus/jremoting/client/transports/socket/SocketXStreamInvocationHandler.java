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

import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.ClientStreamReadWriter;
import org.codehaus.jremoting.client.transports.ClientCustomStreamReadWriter;
import org.codehaus.jremoting.client.transports.ClientXStreamStreamReadWriter;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class SocketCustomStreamInvocationHandler
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public final class SocketXStreamInvocationHandler extends AbstractSocketStreamInvocationHandler {

    /**
     * Constructor SocketCustomStreamInvocationHandler
     *
     * @param host                  the host name
     * @param port                  the port
     * @param interfacesClassLoader the classloader for deserialization hints.
     * @throws org.codehaus.jremoting.api.ConnectionException if a problem
     */
    public SocketXStreamInvocationHandler(ThreadPool threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger, ClassLoader interfacesClassLoader, String host, int port) throws ConnectionException {
        super(threadPool, clientMonitor, connectionPinger, interfacesClassLoader, host, port);
    }

    /**
     * Create a client stream read/writer
     *
     * @param in  the input stream
     * @param out the output stream
     * @return the read/writer
     * @throws org.codehaus.jremoting.api.ConnectionException if a problem
     */
    protected ClientStreamReadWriter createClientStreamReadWriter(InputStream in, OutputStream out) throws ConnectionException {
        return new ClientXStreamStreamReadWriter(in, out, interfacesClassLoader);
    }
}
