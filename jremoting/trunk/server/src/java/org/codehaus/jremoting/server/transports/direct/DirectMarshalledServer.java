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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.server.ServerMarshalledInvoker;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.adapters.InvokerDelegate;
import org.codehaus.jremoting.server.adapters.MarshalledInvokerAdapter;
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
public class DirectMarshalledServer extends StatefulServer implements ServerMarshalledInvoker {

    private final MarshalledInvokerAdapter marshalledInvokerAdapter;

    public DirectMarshalledServer(ServerMonitor serverMonitor, InvokerDelegate invocationHandlerDelegate, ScheduledExecutorService executorService, MarshalledInvokerAdapter marshalledInvokerAdapter) {
        super(serverMonitor, invocationHandlerDelegate, executorService);
        this.marshalledInvokerAdapter = marshalledInvokerAdapter;
    }

    public DirectMarshalledServer(ServerMonitor serverMonitor, InvokerDelegate invocationHandlerDelegate, MarshalledInvokerAdapter marshalledInvokerAdapter) {
        this(serverMonitor, invocationHandlerDelegate, Executors.newScheduledThreadPool(10), marshalledInvokerAdapter);
    }

    public DirectMarshalledServer(ServerMonitor serverMonitor, InvokerDelegate invocationHandlerDelegate) {
        this(serverMonitor, invocationHandlerDelegate, new MarshalledInvokerAdapter(invocationHandlerDelegate));
    }

    public DirectMarshalledServer(ServerMonitor serverMonitor) {
        this(serverMonitor, new InvokerDelegate(serverMonitor, new RefusingStubRetriever(), new NullAuthenticator(), new DefaultServerSideContextFactory()));
    }

    public byte[] invoke(byte[] request, Object connectionDetails) {
        return marshalledInvokerAdapter.invoke(request, connectionDetails);
    }

}
