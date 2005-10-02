/* ====================================================================
 * Copyright 2005 JRemoting Committers
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
import org.codehaus.jremoting.registry.BindException;
import org.codehaus.jremoting.registry.Binder;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Vector;


/**
 * A Complete Socket Object Stream Piped Binder
 *
 * @author Paul Hammant
 */
public class CompleteSocketObjectStreamPipedBinder implements Binder {

    private InvocationHandlerAdapter invocationHandlerAdapter;
    private final ServerMonitor serverMonitor;
    private final ThreadPool threadPool;
    private final ServerSideClientContextFactory contextFactory;
    private Vector connections = new Vector();

    /**
     * Construuct a Piped Binder
     *
     * @param invocationHandlerAdapter An invocation handler adapter to handle requests
     */
    public CompleteSocketObjectStreamPipedBinder(InvocationHandlerAdapter invocationHandlerAdapter, ServerMonitor serverMonitor, ThreadPool threadPool, ServerSideClientContextFactory contextFactory) {
        this.invocationHandlerAdapter = invocationHandlerAdapter;
        this.serverMonitor = serverMonitor;
        this.threadPool = threadPool;
        this.contextFactory = contextFactory;
    }

    /**
     * Bind to an piped stream
     *
     * @param bindParms the piped input stream and piped output stream
     * @return thebound object
     * @throws org.codehaus.jremoting.registry.BindException
     *          if a problem
     */
    public Object bind(Object[] bindParms) throws BindException {
        PipedInputStream inputStream = (PipedInputStream) bindParms[0];
        PipedOutputStream outputStream = (PipedOutputStream) bindParms[1];
        try {
            Object connection = new CompleteSocketObjectStreamPipedConnection(invocationHandlerAdapter, this, serverMonitor, threadPool, contextFactory, inputStream, outputStream);
            connections.add(connection);
            return connection;
        } catch (ConnectionException e) {
            throw new BindException("Problem binding: " + e.getMessage());
        }
    }

    /**
     * Stop the server
     */
    public void stop() {
        for (int i = 0; i < connections.size(); i++) {
            CompleteSocketObjectStreamPipedConnection completeSocketObjectStreamPipedConnection = (CompleteSocketObjectStreamPipedConnection) connections.elementAt(i);
            completeSocketObjectStreamPipedConnection.stop();
        }
    }

    /**
     * End a connection
     *
     * @param connection the connection
     */
    void endConnection(CompleteSocketObjectStreamPipedConnection connection) {
        for (int i = 0; i < connections.size(); i++) {
            CompleteSocketObjectStreamPipedConnection completeSocketObjectStreamPipedConnection = (CompleteSocketObjectStreamPipedConnection) connections.elementAt(i);
            if (connection == completeSocketObjectStreamPipedConnection) {
                connection.stop();
            }

        }
    }
}