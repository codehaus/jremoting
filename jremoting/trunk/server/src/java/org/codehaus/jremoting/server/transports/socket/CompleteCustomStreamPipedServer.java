package org.codehaus.jremoting.server.transports.socket;

import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;
import org.codehaus.jremoting.server.transports.AbstractServerStreamReadWriter;
import org.codehaus.jremoting.server.transports.ServerCustomStreamReadWriter;
import org.codehaus.jremoting.server.transports.piped.AbstractPipedServer;


public class CompleteCustomStreamPipedServer extends AbstractPipedServer {
    public CompleteCustomStreamPipedServer(InvocationHandlerAdapter invocationHandlerAdapter, ServerMonitor serverMonitor, ThreadPool threadPool, ServerSideClientContextFactory contextFactory) {
        super(invocationHandlerAdapter, serverMonitor, threadPool, contextFactory);
    }

    protected AbstractServerStreamReadWriter createServerStreamReadWriter() {
        return new ServerCustomStreamReadWriter(m_serverMonitor, m_threadPool);
    }
}
