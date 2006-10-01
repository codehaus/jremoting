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

import org.codehaus.jremoting.ConnectionException;

import java.util.concurrent.ExecutorService;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ClientStreamDriver;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.client.transports.AbstractStreamClientInvocationHandler;
import org.codehaus.jremoting.client.transports.ClientStreamDriverFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Class AbstractPipedStreamInvocationHandler
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class AbstractPipedStreamInvocationHandler extends AbstractStreamClientInvocationHandler {

    private PipedInputStream inputStream;
    private PipedOutputStream outputStream;

    /**
     * Constructor AbstractPipedStreamInvocationHandler
     *
     * @param clientMonitor
     * @param executorService
     * @param connectionPinger
     * @param is
     * @param os
     * @param facadesClassLoader
     */
    public AbstractPipedStreamInvocationHandler(ClientMonitor clientMonitor, ExecutorService executorService,
                                                ConnectionPinger connectionPinger, PipedInputStream is,
                                                PipedOutputStream os, ClassLoader facadesClassLoader,
                                                ClientStreamDriverFactory clientStreamDriverFactory) {

        super(clientMonitor, executorService, connectionPinger, facadesClassLoader, clientStreamDriverFactory);

        inputStream = is;
        outputStream = os;
    }

    /**
     * Method initialize
     *
     * @throws ConnectionException
     */
    public void initialize() throws ConnectionException {
        setObjectDriver(streamDriverFactory.makeDriver(inputStream, outputStream, facadesClassLoader));
        super.initialize();
    }

    protected boolean tryReconnect() {

        // blimey how do we reconnect this?
        throw new InvocationException("Piped connection broken, unable to reconnect.");
    }

}
