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
package org.codehaus.jremoting.server.transports.piped;

import org.codehaus.jremoting.ConnectionException;

import java.util.concurrent.ScheduledExecutorService;
import org.codehaus.jremoting.server.Authenticator;
import org.codehaus.jremoting.server.StubRetriever;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.StreamEncoding;
import org.codehaus.jremoting.server.StreamEncoder;
import org.codehaus.jremoting.server.ServerContextFactory;
import org.codehaus.jremoting.server.adapters.InvokerDelegate;
import org.codehaus.jremoting.server.transports.*;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Class PipedServer
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class PipedServer extends ConnectingServer {

    private final StreamEncoding streamEncoding;
    private final ClassLoader facadesClassLoader;

    public PipedServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator,
                             ScheduledExecutorService executorService, ServerContextFactory contextFactory,
                             StreamEncoding streamEncoding,
                             ClassLoader facadesClassLoader) {
        super(serverMonitor, new InvokerDelegate(serverMonitor, stubRetriever, authenticator, contextFactory), executorService);
        this.streamEncoding = streamEncoding;
        this.facadesClassLoader = facadesClassLoader;
    }

    public PipedServer(ServerMonitor serverMonitor, InvokerDelegate invokerDelegate,
                             ScheduledExecutorService executorService,
                             StreamEncoding streamEncoding,
                             ClassLoader facadesClassLoader) {
        super(serverMonitor, invokerDelegate, executorService);
        this.streamEncoding = streamEncoding;
        this.facadesClassLoader = facadesClassLoader;
    }

    public PipedServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator, ScheduledExecutorService executorService, ServerContextFactory serverContextFactory, StreamEncoding streamEncoding) {
        this(serverMonitor, stubRetriever, authenticator, executorService, serverContextFactory, streamEncoding, PipedServer.class.getClassLoader());
    }

    public void makeNewConnection(final PipedInputStream in, final PipedOutputStream out) throws ConnectionException {

        if (getState().equals(UNSTARTED) | getState().equals(STARTING)) {
            throw new ConnectionException("Server not started yet");
        } else if (getState().equals(STOPPING)) {
            throw new ConnectionException("Server is Shutting down");
        }

        try {
            final PipedInputStream pIS = new PipedInputStream();
            final PipedOutputStream pOS = new PipedOutputStream();

            pIS.connect(out);
            in.connect(pOS);

            StreamEncoder streamEncoder = streamEncoding.createEncoder(serverMonitor, facadesClassLoader, pIS, pOS, "piped");

            StreamConnection streamConnection = new StreamConnection(this, streamEncoder, serverMonitor) {

                protected void killConnection() {

                    try {
                        pIS.close();
                    } catch (IOException e) {
                        serverMonitor.closeError(this.getClass(), "PipedStreamConnection.killConnection(): Some problem during closing of Input Stream", e);
                    }

                    try {
                        pOS.close();
                    } catch (IOException e) {
                        serverMonitor.closeError(this.getClass(), "PipedStreamConnection.killConnection(): Some problem during closing of Output Stream", e);
                    }
                }
            };

            executorService.execute(streamConnection);

        } catch (IOException pe) {
            throw new ConnectionException("Some problem setting up server : " + pe.getMessage());
        }
    }
}
