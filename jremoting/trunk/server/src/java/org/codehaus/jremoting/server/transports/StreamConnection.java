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

import org.codehaus.jremoting.BadConnectionException;
import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.requests.AbstractRequest;
import org.codehaus.jremoting.responses.AbstractResponse;
import org.codehaus.jremoting.responses.ConnectionKilled;
import org.codehaus.jremoting.responses.InvocationExceptionThrown;
import org.codehaus.jremoting.server.Connection;
import org.codehaus.jremoting.server.ServerMonitor;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;

/**
 * Class StreamConnection
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public abstract class StreamConnection implements Runnable, Connection {

    /**
     * The Abstract Server
     */
    private ConnectingServer connectingServer;

    /**
     * End connections
     */
    private boolean endConnection = false;

    /**
     * The Sever Stream Driver.
     */
    private ServerStreamDriver driver;

    protected final ServerMonitor serverMonitor;

    /**
     * Construct a StreamConnection
     *
     * @param connectingServer The Abstract Server handling requests
     * @param driver         The Driver.
     */
    public StreamConnection(ConnectingServer connectingServer, ServerStreamDriver driver, ServerMonitor serverMonitor) {
        this.connectingServer = connectingServer;
        this.driver = driver;
        this.serverMonitor = serverMonitor;
    }


    /**
     * Method run
     */
    public void run() {

        connectingServer.connectionStart(this);

        try {

            driver.initialize();

            boolean more = true;
            AbstractRequest request = null;
            AbstractResponse response = null;
            while (more) {
                try {
                    if (request != null) {
                        response = connectingServer.handleInvocation(request, driver.getConnectionDetails());
                    }

                    request = driver.writeResponseAndGetRequest(response);
                    //oOS.reset();
                    if (endConnection) {
                        response = new ConnectionKilled();
                        more = false;
                    }
                } catch (BadConnectionException bce) {
                    more = false;
                    serverMonitor.badConnection(this.getClass(), "StreamConnection.run(): Bad connection #0", bce);
                    driver.close();
                } catch (ConnectionException ace) {
                    more = false;
                    serverMonitor.unexpectedException(this.getClass(), "StreamConnection.run(): Unexpected ConnectionException #0", ace);
                    driver.close();
                } catch (IOException ioe) {
                    more = false;

                    if (ioe instanceof EOFException) {
                        driver.close();
                    } else if (isSafeEnd(ioe)) {
                        //ioe.printStackTrace();
                        // TODO implement implementation independant logger
                        driver.close();
                    } else {
                        serverMonitor.unexpectedException(this.getClass(), "StreamConnection.run(): Unexpected IOE #1", ioe);
                        driver.close();
                    }
                } catch (NullPointerException npe) {
                    serverMonitor.unexpectedException(this.getClass(), "StreamConnection.run(): Unexpected NPE", npe);
                    response = new InvocationExceptionThrown("NullPointerException on server: " + npe.getMessage());
                }
            }
        } catch (IOException e) {
            serverMonitor.unexpectedException(this.getClass(), "StreamConnection.run(): Unexpected IOE #2", e);
        } catch (ClassNotFoundException e) {
            serverMonitor.classNotFound(this.getClass(), e);
        }

        connectingServer.connectionCompleted(this);
    }

    private boolean isSafeEnd(IOException ioe) {
        if ((ioe instanceof SocketException) || ioe.getClass().getName().equals("java.net.SocketTimeoutException") // 1.3 safe
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
     * Method endConnection
     */
    public void endConnection() {
        endConnection = true;
        driver.close();
    }

    /**
     * Method killConnection
     */
    protected abstract void killConnection();

}
