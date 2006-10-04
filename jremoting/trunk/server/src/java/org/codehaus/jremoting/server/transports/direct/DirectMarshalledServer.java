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

import org.codehaus.jremoting.server.ServerMarshalledInvocationHandler;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;
import org.codehaus.jremoting.server.adapters.MarshalledInvocationHandlerAdapter;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.transports.DefaultServerSideContextFactory;
import org.codehaus.jremoting.server.transports.StatefulServer;

/**
 * Class DirectMarshalledServer
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class DirectMarshalledServer extends StatefulServer implements ServerMarshalledInvocationHandler {

    private final MarshalledInvocationHandlerAdapter marshalledInvocationHandlerAdapter;

    /**
     * Constructor DirectMarshalledServer for use with pre-exiting InvocationHandlerAdapter and MarshalledInvocationHandler
     *
     * @param serverMonitor
     * @param invocationHandlerAdapter
     * @param executorService
     * @param marshalledInvocationHandlerAdapter
     *
     */
    public DirectMarshalledServer(ServerMonitor serverMonitor, InvocationHandlerAdapter invocationHandlerAdapter, ExecutorService executorService, MarshalledInvocationHandlerAdapter marshalledInvocationHandlerAdapter) {
        super(serverMonitor, invocationHandlerAdapter, executorService);
        this.marshalledInvocationHandlerAdapter = marshalledInvocationHandlerAdapter;
    }

    public DirectMarshalledServer(ServerMonitor serverMonitor, InvocationHandlerAdapter invocationHandlerAdapter, MarshalledInvocationHandlerAdapter marshalledInvocationHandlerAdapter) {
        this(serverMonitor, invocationHandlerAdapter, Executors.newCachedThreadPool(), marshalledInvocationHandlerAdapter);
    }

    public DirectMarshalledServer(ServerMonitor serverMonitor, InvocationHandlerAdapter invocationHandlerAdapter) {
        this(serverMonitor, invocationHandlerAdapter, new MarshalledInvocationHandlerAdapter(invocationHandlerAdapter));
    }

    public DirectMarshalledServer(ServerMonitor serverMonitor) {
        this(serverMonitor, new InvocationHandlerAdapter(serverMonitor, new RefusingStubRetriever(), new NullAuthenticator(), new DefaultServerSideContextFactory()));
    }

    public byte[] handleInvocation(byte[] request, Object connectionDetails) {
        return marshalledInvocationHandlerAdapter.handleInvocation(request, connectionDetails);
    }

}
