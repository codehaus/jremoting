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

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.codehaus.jremoting.JRemotingException;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.server.*;
import org.codehaus.jremoting.server.adapters.DefaultServerDelegate;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.streams.ByteStreamProtocolCodecFactory;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Class MinaServer
 *
 * @author Paul Hammant
 *
 */
public class MinaServer extends StatefulServer {

    private final InetSocketAddress addr;
    private final ClassLoader facadesClassLoader;
    private int socketTimeout = 60 * 1000;
    private IoAcceptor acceptor;
    private ProtocolCodecFactory codecFactory;

    public void setSocketTimeout(int millis) {
        this.socketTimeout = millis;
    }

    public MinaServer(ServerMonitor serverMonitor, Authenticator authenticator, InetSocketAddress addr) {
        this(serverMonitor, addr, authenticator);
    }

    public MinaServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, InetSocketAddress addr) {
        this(serverMonitor, stubRetriever, defaultAuthenticator(), defaultProtocolCodecFactory(),
                defaultContextFactory(), addr);
    }

    public MinaServer(ServerMonitor serverMonitor, InetSocketAddress addr) {
        this(serverMonitor, defaultProtocolCodecFactory(), addr);
    }

    public MinaServer(ServerMonitor serverMonitor, ProtocolCodecFactory codecFactory, InetSocketAddress addr) {
        this(serverMonitor, defaultStubRetriever(), defaultAuthenticator(), codecFactory, defaultContextFactory(), defaultClassLoader(), addr);
    }

    public MinaServer(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator, ProtocolCodecFactory codecFactory, ServerContextFactory serverContextFactory, InetSocketAddress addr) {
        this(serverMonitor, stubRetriever, authenticator, codecFactory, serverContextFactory, defaultClassLoader(), addr);
    }

    public MinaServer(ServerMonitor serverMonitor, InetSocketAddress addr, Authenticator authenticator) {
        this(serverMonitor, defaultStubRetriever(), authenticator, defaultProtocolCodecFactory(), defaultContextFactory(), addr);
    }

    public MinaServer(ServerMonitor serverMonitor, StubRetriever stubRetriever,
                        Authenticator authenticator,
                        ProtocolCodecFactory codecFactory,
                        ServerContextFactory contextFactory,
                        ClassLoader facadesClassLoader, InetSocketAddress addr) {
        this(serverMonitor, defaultServerDelegate(serverMonitor, stubRetriever, authenticator, contextFactory),
                codecFactory, facadesClassLoader, addr);
    }

    public MinaServer(ServerMonitor serverMonitor, ServerDelegate serverDelegate,
                                           ProtocolCodecFactory codecFactory,
                                           ClassLoader facadesClassLoader, InetSocketAddress addr) {

        super(serverMonitor, serverDelegate);
        this.codecFactory = codecFactory;
        this.facadesClassLoader = facadesClassLoader;
        this.addr = addr;
    }

    private static ServerDelegate defaultServerDelegate(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator, ServerContextFactory contextFactory) {
        return new DefaultServerDelegate(serverMonitor, stubRetriever, authenticator, contextFactory);
    }

    private static ProtocolCodecFactory defaultProtocolCodecFactory() {
        return new ByteStreamProtocolCodecFactory();
    }

    public static ClassLoader defaultClassLoader() {
        return SocketServer.class.getClassLoader();
    }

    public static StubRetriever defaultStubRetriever() {
        return new RefusingStubRetriever();
    }

    public static Authenticator defaultAuthenticator() {
        return new NullAuthenticator();
    }

    public static ServerContextFactory defaultContextFactory() {
        return new ThreadLocalServerContextFactory();
    }

    public void starting() {
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast( "codec", new ProtocolCodecFilter(codecFactory));
        acceptor.setHandler(new IoHandlerAdapter() {
            @Override
            public void exceptionCaught(IoSession session, Throwable cause) {
                serverMonitor.unexpectedException(MinaServer.class, "MinaServer: Mina Exception Caught", cause);
            }
            @Override
            public void messageReceived(IoSession session, Object message) {
                session.write(MinaServer.this.invoke((Request) message, session.getRemoteAddress().toString()));
            }
            @Override
            public void sessionIdle(IoSession session, IdleStatus status) {
                System.out.println("IDLE " + session.getIdleCount(status));
            }
        });
        acceptor.getSessionConfig().setReadBufferSize( 2048 );
        acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, 10 );
        acceptor.setDefaultLocalAddress(addr);
        try {
            acceptor.bind();
        } catch (IOException ioe) {
            throw new JRemotingException("Could not bind to port '"+addr.getPort()+"', address '"+addr.getAddress()+"'when setting up the server", ioe);
        }
        super.starting();
    }

    public void stopping() {
        acceptor.unbind();
        acceptor.dispose();
        super.stopping();
    }

    public void stopped() {
        super.stopped();
    }

    public void redirect(String serviceName, String host, int port) {
        super.redirect(serviceName, host + ":" + port);
    }

}