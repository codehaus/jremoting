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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.server.Authenticator;
import org.codehaus.jremoting.server.StubRetriever;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;
import org.codehaus.jremoting.server.authenticators.DefaultAuthenticator;
import org.codehaus.jremoting.server.classretrievers.NoStubRetriever;
import org.codehaus.jremoting.server.monitors.NullServerMonitor;
import org.codehaus.jremoting.server.transports.AbstractServerStreamDriver;
import org.codehaus.jremoting.server.transports.DefaultServerSideClientContextFactory;
import org.codehaus.jremoting.server.transports.ServerCustomStreamDriver;

/**
 * Class SelfContainedSocketObjectStreamServer
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */

public class SelfContainedSocketCustomStreamServer extends AbstractSelfContainedSocketStreamServer {
    /**
     * Construct a SelfContainedSocketCustomStreamServer
     *
     * @param stubRetriever
     * @param authenticator
     * @param serverMonitor
     * @param executor
     * @param contextFactory
     * @param port
     */
    public SelfContainedSocketCustomStreamServer(StubRetriever stubRetriever, Authenticator authenticator, ServerMonitor serverMonitor, ExecutorService executor, ServerSideClientContextFactory contextFactory, int port) {
        super(new InvocationHandlerAdapter(stubRetriever, authenticator, serverMonitor, contextFactory), serverMonitor, executor, contextFactory, port);
    }

    public SelfContainedSocketCustomStreamServer(int port) {
        this(new NoStubRetriever(), new DefaultAuthenticator(), new NullServerMonitor(), Executors.newCachedThreadPool(), new DefaultServerSideClientContextFactory(), port);
    }

    /**
     * Create a Server Stream Driver.
     *
     * @return The Server Stream Driver.
     */
    protected AbstractServerStreamDriver createServerStreamDriver() {
        ServerCustomStreamDriver csd = new ServerCustomStreamDriver(serverMonitor, executor);
        return csd;
    }
}
