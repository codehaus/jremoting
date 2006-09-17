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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.server.Authenticator;
import org.codehaus.jremoting.server.StubRetriever;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;
import org.codehaus.jremoting.server.authenticators.DefaultAuthenticator;
import org.codehaus.jremoting.server.classretrievers.NoStubRetriever;
import org.codehaus.jremoting.server.monitors.NullServerMonitor;
import org.codehaus.jremoting.server.transports.AbstractServerStreamDriver;
import org.codehaus.jremoting.server.transports.DefaultServerSideClientContextFactory;
import org.codehaus.jremoting.server.transports.ServerObjectStreamDriver;

/**
 * Class PipedObjectStreamServer
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class PipedObjectStreamServer extends AbstractPipedServer {


    /**
     * @param stubRetriever
     * @param authenticator
     * @param serverMonitor
     * @param threadPool
     * @param contextFactory
     */
    public PipedObjectStreamServer(StubRetriever stubRetriever, Authenticator authenticator, ServerMonitor serverMonitor, ExecutorService threadPool, ServerSideClientContextFactory contextFactory) {
        super(stubRetriever, authenticator, serverMonitor, threadPool, contextFactory);
    }

    public PipedObjectStreamServer() {
        this(new NoStubRetriever(), new DefaultAuthenticator(), new NullServerMonitor(), Executors.newCachedThreadPool(), new DefaultServerSideClientContextFactory());
    }

    protected AbstractServerStreamDriver createServerStreamDriver() {
        return new ServerObjectStreamDriver(serverMonitor, threadPool);
    }
}
