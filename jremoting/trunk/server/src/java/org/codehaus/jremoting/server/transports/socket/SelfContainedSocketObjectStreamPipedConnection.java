/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
 * Portions copyright 2001 - 2004 Apache Software Foundation
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
package org.codehaus.jremoting.server.transports.socket;

import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;


/**
 * A Complete Sockect Object Stream Piped Connection.
 *
 * @author Paul Hammant
 */
public class SelfContainedSocketObjectStreamPipedConnection {

    private SelfContainedCustomStreamPipedServer selfContainedCustomStreamPipedServer;

    /**
     * Create a Complete Socket ObjectStream Piped Connection
     *
     * @param invocationHandlerAdapter the invocation adapter from the SocketObjectStream
     * @param inputStream              the piped input stream
     * @param outputStream             the piped output stream
     * @throws ConnectionException if a problem
     */
    public SelfContainedSocketObjectStreamPipedConnection(InvocationHandlerAdapter invocationHandlerAdapter, ServerMonitor serverMonitor, ThreadPool threadPool, ServerSideClientContextFactory contextFactory, PipedInputStream inputStream, PipedOutputStream outputStream) throws ConnectionException {
        selfContainedCustomStreamPipedServer = new SelfContainedCustomStreamPipedServer(invocationHandlerAdapter, serverMonitor, threadPool, contextFactory);
        selfContainedCustomStreamPipedServer.makeNewConnection(inputStream, outputStream);
        selfContainedCustomStreamPipedServer.start();
    }

    /**
     * Close the connection
     */
    public void close() {
    }

    /**
     * Stop the server.
     */
    protected void stop() {
        selfContainedCustomStreamPipedServer.stop();
    }
}

