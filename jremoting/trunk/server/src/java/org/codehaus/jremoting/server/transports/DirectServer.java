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

import org.codehaus.jremoting.server.Authenticator;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.StubRetriever;
import org.codehaus.jremoting.server.adapters.DefaultServerDelegate;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.ServerContextFactory;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.transports.StatefulServer;

/**
 * Class DirectServer
 *
 * @author Paul Hammant
 */
public class DirectServer extends StatefulServer {

    /**
     * Constructor DirectServer
     *
     * @param authenticator
     * @param serverMonitor
     * @param contextFactory
     */
    public DirectServer(Authenticator authenticator, ServerMonitor serverMonitor, ServerContextFactory contextFactory) {
        super(serverMonitor, new DefaultServerDelegate(serverMonitor, new RefusingStubRetriever(), authenticator, contextFactory));
    }

    public DirectServer(ServerMonitor monitor) {
        this(new NullAuthenticator(), monitor, new ThreadLocalServerContextFactory());
    }

}
