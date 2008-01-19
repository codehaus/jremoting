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

import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.InvocationHandler;
import org.codehaus.jremoting.server.ServerMarshalledInvoker;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.adapters.DefaultInvocationHandler;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.transports.StatefulServer;
import org.codehaus.jremoting.util.SerializationHelper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class DirectMarshalledServer
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class DirectMarshalledServer extends StatefulServer implements ServerMarshalledInvoker {

    private final ServerMarshalledInvokerImpl marshalledInvokerAdapter;

    private DirectMarshalledServer(ServerMonitor serverMonitor, DefaultInvocationHandler invocationHandler, ScheduledExecutorService executorService, ServerMarshalledInvokerImpl marshalledInvokerAdapter) {
        super(serverMonitor, invocationHandler, executorService);
        this.marshalledInvokerAdapter = marshalledInvokerAdapter;
    }

    private DirectMarshalledServer(ServerMonitor serverMonitor, DefaultInvocationHandler invocationHandler, ServerMarshalledInvokerImpl marshalledInvokerAdapter) {
        this(serverMonitor, invocationHandler, Executors.newScheduledThreadPool(10), marshalledInvokerAdapter);
    }

    public DirectMarshalledServer(ServerMonitor serverMonitor, ScheduledExecutorService executorService, DefaultInvocationHandler invocationHandler) {
        this(serverMonitor, invocationHandler, executorService, new ServerMarshalledInvokerImpl(invocationHandler));
    }

    public DirectMarshalledServer(ServerMonitor serverMonitor, DefaultInvocationHandler invocationHandler) {
        this(serverMonitor, invocationHandler, new ServerMarshalledInvokerImpl(invocationHandler));
    }

    public DirectMarshalledServer(ServerMonitor serverMonitor) {
        this(serverMonitor, new DefaultInvocationHandler(serverMonitor, new RefusingStubRetriever(), new NullAuthenticator(), new ThreadLocalServerContextFactory()));
    }

    public byte[] invoke(byte[] request, Object connectionDetails) {
        return marshalledInvokerAdapter.invoke(request, connectionDetails);
    }


    private static class ServerMarshalledInvokerImpl implements ServerMarshalledInvoker {

        private InvocationHandler invoker;
        private ClassLoader facadesClassLoader;

        public ServerMarshalledInvokerImpl(InvocationHandler invoker) {
            this.invoker = invoker;
            facadesClassLoader = getClass().getClassLoader();
        }

        public ServerMarshalledInvokerImpl(InvocationHandler invoker, ClassLoader facadesClassLoader) {
            this.invoker = invoker;
            this.facadesClassLoader = facadesClassLoader;
        }

        public byte[] invoke(byte[] request, Object connectionDetails) {

            try {
                Request ar = (Request) SerializationHelper.getInstanceFromBytes(request, facadesClassLoader);
                Response response = invoker.invoke(ar, connectionDetails);

                return SerializationHelper.getBytesFromInstance(response);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();

                return null;
            }
        }
    }


}
