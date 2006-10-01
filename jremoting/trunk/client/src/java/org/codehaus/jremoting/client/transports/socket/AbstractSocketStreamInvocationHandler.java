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

import org.codehaus.jremoting.BadConnectionException;
import org.codehaus.jremoting.ConnectionException;

import java.util.concurrent.ExecutorService;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.ConnectionRefusedException;
import org.codehaus.jremoting.client.transports.AbstractStreamClientInvocationHandler;
import org.codehaus.jremoting.client.transports.ClientStreamDriverFactory;

import java.io.IOException;
import java.net.Socket;

/**
 * Class SocketCustomStreamInvocationHandler
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class AbstractSocketStreamInvocationHandler extends AbstractStreamClientInvocationHandler {

    private final String host;
    private final int port;


    /**
     * AbstractSocketStreamInvocationHandler
     *
     * @param clientMonitor
     * @param executorService
     * @param connectionPinger
     * @param facadesClassLoader The class loader
     * @param host                  The host to connect to
     * @param port                  The port to conenct to
     */
    public AbstractSocketStreamInvocationHandler(ClientMonitor clientMonitor, ExecutorService executorService,
                                                 ConnectionPinger connectionPinger, ClassLoader facadesClassLoader,
                                                 ClientStreamDriverFactory streamDriverFactory,
                                                 String host, int port) throws ConnectionRefusedException, BadConnectionException {
        super(clientMonitor, executorService, connectionPinger, facadesClassLoader, streamDriverFactory);
        this.host = host;
        this.port = port;

        try {
            Socket socket = new Socket(this.host, this.port);
            socket.setSoTimeout(60 * 1000);
            setObjectDriver(streamDriverFactory.makeDriver(socket.getInputStream(), socket.getOutputStream(), facadesClassLoader));
        } catch (IOException ioe) {
            if (ioe.getMessage().startsWith("Connection refused")) {
                throw new ConnectionRefusedException("Connection to port " + port + " on host " + host + " refused.");
            }
            throw new BadConnectionException("Cannot open Stream(s) for socket: " + ioe.getMessage());
        }
    }

    /**
     * Method tryReconnect
     *
     * @return connected or not.
     */
    protected boolean tryReconnect() {

        try {
            Socket socket = new Socket(host, port);
            socket.setSoTimeout(60 * 1000);
            setObjectDriver(streamDriverFactory.makeDriver(socket.getInputStream(), socket.getOutputStream(), facadesClassLoader));
            return true;
        } catch (ConnectionException ce) {
            // TODO log ?
            return false;
        } catch (IOException ce) {

            // TODO log ?
            return false;
        }
    }

}
