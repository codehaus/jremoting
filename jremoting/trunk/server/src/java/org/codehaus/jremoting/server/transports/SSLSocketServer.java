/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
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

import org.codehaus.jremoting.server.Stream;
import org.codehaus.jremoting.server.*;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ScheduledExecutorService;

public class SSLSocketServer extends SocketServer {

    private ServerSocketFactory serverSocketFactory;

    public SSLSocketServer(ServerMonitor serverMonitor, InetSocketAddress addr) {
        super(serverMonitor, addr);
    }

    public SSLSocketServer(ServerMonitor serverMonitor, Authenticator authenticator, InetSocketAddress addr) {
        super(serverMonitor, authenticator, addr);
    }

    public SSLSocketServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, InetSocketAddress addr) {
        super(serverMonitor, stubRetriever, addr);
    }

    public SSLSocketServer(ServerMonitor serverMonitor, Stream Stream, InetSocketAddress port) {
        super(serverMonitor, Stream, port);
    }

    public SSLSocketServer(ServerMonitor serverMonitor, ScheduledExecutorService executorService, InetSocketAddress addr) {
        super(serverMonitor, executorService, addr);
    }

    public SSLSocketServer(ServerMonitor serverMonitor, ScheduledExecutorService executorService, Stream Stream, InetSocketAddress addr) {
        super(serverMonitor, executorService, Stream, addr);
    }

    public SSLSocketServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator, Stream Stream, ScheduledExecutorService executorService, ServerContextFactory serverContextFactory, InetSocketAddress addr) {
        super(serverMonitor, stubRetriever, authenticator, Stream, executorService, serverContextFactory, addr);
    }

    public SSLSocketServer(ServerMonitor serverMonitor, InetSocketAddress addr, ScheduledExecutorService executorService, Authenticator authenticator) {
        super(serverMonitor, addr, executorService, authenticator);
    }

    public SSLSocketServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator, Stream Stream, ScheduledExecutorService executorService, ServerContextFactory contextFactory, ClassLoader facadesClassLoader, InetSocketAddress addr) {
        super(serverMonitor, stubRetriever, authenticator, Stream, executorService, contextFactory, facadesClassLoader, addr);
    }

    public SSLSocketServer(ServerMonitor serverMonitor, ServerDelegate serverDelegate, Stream Stream, ScheduledExecutorService executorService, ClassLoader facadesClassLoader, InetSocketAddress addr) {
        super(serverMonitor, serverDelegate, Stream, executorService, facadesClassLoader, addr);
    }

    @Override
    protected ServerSocket makeServerSocket(InetSocketAddress addr) throws IOException {
        makeServerSocketFactory();
        return serverSocketFactory.createServerSocket(addr.getPort(), 50, addr.getAddress());
    }

    protected synchronized void makeServerSocketFactory() {
        if (serverSocketFactory == null) {
            serverSocketFactory = SSLServerSocketFactory.getDefault();
        }
    }
}
