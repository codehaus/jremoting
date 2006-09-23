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
package org.codehaus.jremoting.server.transports.direct;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.requests.AbstractRequest;
import org.codehaus.jremoting.responses.AbstractResponse;
import org.codehaus.jremoting.responses.InvocationExceptionThrown;
import org.codehaus.jremoting.server.Authenticator;
import org.codehaus.jremoting.server.StubRetriever;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;
import org.codehaus.jremoting.server.authenticators.DefaultAuthenticator;
import org.codehaus.jremoting.server.classretrievers.NoStubRetriever;
import org.codehaus.jremoting.server.monitors.NullServerMonitor;
import org.codehaus.jremoting.server.transports.AbstractServer;
import org.codehaus.jremoting.server.transports.DefaultServerSideClientContextFactory;

/**
 * Class DirectServer
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class DirectServer extends AbstractServer {

    /**
     * Constructor DirectServer for use with pre-exiting InvocationHandlerAdapter.
     *
     * @param stubRetriever
     * @param authenticator
     * @param serverMonitor
     * @param executor
     * @param contextFactory
     */
    public DirectServer(StubRetriever stubRetriever, Authenticator authenticator, ServerMonitor serverMonitor, ExecutorService executor, ServerSideClientContextFactory contextFactory) {
        super(new InvocationHandlerAdapter(stubRetriever, authenticator, serverMonitor, contextFactory), serverMonitor, executor);
    }

    public DirectServer() {
        this(new NoStubRetriever(), new DefaultAuthenticator(), new NullServerMonitor(), Executors.newCachedThreadPool(), new DefaultServerSideClientContextFactory());
    }

    /**
     * Method start
     */
    public void start() {
        setState(STARTED);
    }

    /**
     * Method stop
     */
    public void stop() {

        setState(SHUTTINGDOWN);

        killAllConnections();

        setState(STOPPED);
    }

    /**
     * Method handleInvocation
     *
     * @param request
     * @return
     */
    public AbstractResponse handleInvocation(AbstractRequest request) {

        if (getState() == STARTED) {
            return super.handleInvocation(request, "");
        } else {
            return new InvocationExceptionThrown("Service is not started");
        }
    }
}
