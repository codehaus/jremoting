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
import java.util.concurrent.ExecutorService;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;


/**
 * A Complete Socket Custom Stream Piped Instance.
 *
 * @author Paul Hammant
 */
public class SelfContainedSocketCustomStreamPipedConnection {

    private SelfContainedCustomStreamPipedServer pipedCustomStreamServer;

    /**
     * Create a Complete Socket CustomStream Piped Connection
     *
     * @param invocationHandlerAdapter the invocation adapter from the SocketCustomStream
     * @param inputStream              the piped input stream
     * @param outputStream             the piped output stream
     * @throws ConnectionException if a problem
     */
    public SelfContainedSocketCustomStreamPipedConnection(InvocationHandlerAdapter invocationHandlerAdapter, PipedInputStream inputStream, PipedOutputStream outputStream, ExecutorService executor, ServerSideClientContextFactory contextFactory, ServerMonitor serverMonitor) throws ConnectionException {
        pipedCustomStreamServer = new SelfContainedCustomStreamPipedServer(serverMonitor, invocationHandlerAdapter, executor, contextFactory);
        pipedCustomStreamServer.start();
        pipedCustomStreamServer.makeNewConnection(inputStream, outputStream);

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
        pipedCustomStreamServer.stop();
    }
}
