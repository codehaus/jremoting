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
import org.codehaus.jremoting.server.context.ServerContextFactory;
import org.codehaus.jremoting.server.adapters.InvokerDelegate;
import org.codehaus.jremoting.server.transports.*;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Class PipedStreamServer
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class PipedStreamServer extends ConnectingServer {

    private final StreamEncoding streamEncoding;
    private final ClassLoader facadesClassLoader;

    public PipedStreamServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator,
                             ScheduledExecutorService executorService, ServerContextFactory contextFactory,
                             StreamEncoding streamEncoding,
                             ClassLoader facadesClassLoader) {
        super(serverMonitor, new InvokerDelegate(serverMonitor, stubRetriever, authenticator, contextFactory), executorService);
        this.streamEncoding = streamEncoding;
        this.facadesClassLoader = facadesClassLoader;
    }

    public PipedStreamServer(ServerMonitor serverMonitor, InvokerDelegate invocationHandlerDelegate,
                             ScheduledExecutorService executorService,
                             StreamEncoding streamEncoding,
                             ClassLoader facadesClassLoader) {
        super(serverMonitor, invocationHandlerDelegate, executorService);
        this.streamEncoding = streamEncoding;
        this.facadesClassLoader = facadesClassLoader;
    }

    public PipedStreamServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator, ScheduledExecutorService executorService, ServerContextFactory serverContextFactory, StreamEncoding streamEncoding) {
        this(serverMonitor, stubRetriever, authenticator, executorService, serverContextFactory, streamEncoding, PipedStreamServer.class.getClassLoader());
    }

    /**
     * Method connect
     *
     * @param in
     * @param out
     * @throws ConnectionException
     */
    public void makeNewConnection(PipedInputStream in, PipedOutputStream out) throws ConnectionException {

        if (getState().equals(UNSTARTED) | getState().equals(STARTING)) {
            throw new ConnectionException("Server not started yet");
        } else if (getState().equals(SHUTTINGDOWN)) {
            throw new ConnectionException("Server is Shutting down");
        }

        try {
            PipedInputStream pIS = new PipedInputStream();
            PipedOutputStream pOS = new PipedOutputStream();

            pIS.connect(out);
            in.connect(pOS);


            StreamEncoder ssd = streamEncoding.createEncoder(serverMonitor, facadesClassLoader, pIS, pOS, "piped");

            PipedStreamConnection pssc = new PipedStreamConnection(this, pIS, pOS, ssd, serverMonitor);

            executorService.execute(pssc);

        } catch (IOException pe) {
            throw new ConnectionException("Some problem setting up server : " + pe.getMessage());
        }
    }
}
