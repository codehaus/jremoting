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

import org.codehaus.jremoting.server.Authenticator;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.StubRetriever;
import org.codehaus.jremoting.server.adapters.InvokerDelegate;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.ServerContextFactory;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.transports.StatefulServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class DirectServer
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class DirectServer extends StatefulServer {

    /**
     * Constructor DirectServer for use with pre-exiting InvokerDelegate.
     *
     * @param stubRetriever
     * @param authenticator
     * @param serverMonitor
     * @param executorService
     * @param contextFactory
     */
    public DirectServer(StubRetriever stubRetriever, Authenticator authenticator, ServerMonitor serverMonitor, ScheduledExecutorService executorService, ServerContextFactory contextFactory) {
        super(serverMonitor, new InvokerDelegate(serverMonitor, stubRetriever, authenticator, contextFactory), executorService);
    }

    public DirectServer(ServerMonitor monitor) {
        this(new RefusingStubRetriever(), new NullAuthenticator(), monitor, Executors.newScheduledThreadPool(10), new ThreadLocalServerContextFactory());
    }

}
