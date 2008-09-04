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
package org.codehaus.jremoting.server.transports;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.BadServerSideEvent;
import org.codehaus.jremoting.responses.ConnectionKilled;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.Connection;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.InvocationHandler;
import org.codehaus.jremoting.server.StreamConnection;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.StreamCorruptedException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Class RunningConnection is a connection that that will be run implicitly in a different thread.
 *
 * @author Paul Hammant
 *
 */
public abstract class RunningConnection implements Runnable, Connection {

    private InvocationHandler invocationHandler;
    private boolean endConnection = false;
    private StreamConnection connection;
    private final ServerMonitor serverMonitor;

    public RunningConnection(InvocationHandler invocationHandler, StreamConnection connection, ServerMonitor serverMonitor) {
        this.invocationHandler = invocationHandler;
        this.connection = connection;
        this.serverMonitor = serverMonitor;
    }

    /**
     * {@inheritDoc}
     */
    public void run() {

        try {

            connection.initialize();

            boolean more = true;
            Request request = null;
            Response response = null;
            while (more) {
                try {
                    if (request != null) {
                        response = invocationHandler.invoke(request, connection.getConnectionDetails());
                    }

                    request = connection.writeResponseAndGetRequest(response);
                    if (endConnection) {
                        response = new ConnectionKilled();
                        more = false;
                    }
                } catch (ConnectionException ace) {
                    more = false;
                    serverMonitor.unexpectedException(this.getClass(), "RunningConnection.run(): Unexpected ConnectionException #0", ace);
                    connection.closeConnection();
                } catch (IOException ioe) {
                    more = false;
                    if (ioe instanceof EOFException) {
                        connection.closeConnection();
                    } else if (isSafeEnd(ioe)) {
                        connection.closeConnection();
                    } else {
                        serverMonitor.unexpectedException(this.getClass(), "RunningConnection.run(): Unexpected IOE #1", ioe);
                        connection.closeConnection();
                    }
                } catch (NullPointerException npe) {
                    serverMonitor.unexpectedException(this.getClass(), "RunningConnection.run(): Unexpected NPE", npe);
                    response = new BadServerSideEvent("NullPointerException on server: " + npe.getMessage());
                }
            }
        } catch (StreamCorruptedException e) {
            serverMonitor.unexpectedException(this.getClass(), "RunningConnection.run(): Unexpected IOE #2 (possible transport mismatch?)", e);
        } catch (IOException e) {
            serverMonitor.unexpectedException(this.getClass(), "RunningConnection.run(): Unexpected IOE #3", e);
        } catch (ClassNotFoundException e) {
            serverMonitor.classNotFound(this.getClass(), e);
        }

    }

    private boolean isSafeEnd(IOException ioe) {
        if ((ioe instanceof SocketException) || ioe instanceof SocketTimeoutException
                || (ioe instanceof InterruptedIOException)) {
            return true;
        }

        if (ioe.getMessage() != null) {
            String msg = ioe.getMessage();
            if (msg.equals("Write end dead") | msg.equals("Pipe broken") | msg.equals("Pipe closed")) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void closeConnection() {
        endConnection = true;
        connection.closeConnection();
    }

}
