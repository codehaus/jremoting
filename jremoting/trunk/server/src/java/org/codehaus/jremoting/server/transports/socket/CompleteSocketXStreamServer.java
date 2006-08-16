package org.codehaus.jremoting.server.transports.socket;

import org.codehaus.jremoting.server.ClassRetriever;
import org.codehaus.jremoting.server.Authenticator;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;
import org.codehaus.jremoting.server.transports.DefaultServerSideClientContextFactory;
import org.codehaus.jremoting.server.transports.AbstractServerStreamReadWriter;
import org.codehaus.jremoting.server.transports.ServerCustomStreamReadWriter;
import org.codehaus.jremoting.server.transports.ServerXStreamStreamReadWriter;
import org.codehaus.jremoting.server.monitors.NullServerMonitor;
import org.codehaus.jremoting.server.authenticators.DefaultAuthenticator;
import org.codehaus.jremoting.server.classretrievers.NoClassRetriever;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.api.DefaultThreadPool;

/**
 * Class CompleteSocketObjectStreamServer
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */

public class CompleteSocketXStreamServer extends AbstractCompleteSocketStreamServer {
    /**
     * Construct a CompleteSocketCustomStreamServer
     *
     * @param classRetriever
     * @param authenticator
     * @param serverMonitor
     * @param threadPool
     * @param contextFactory
     * @param port
     */
    public CompleteSocketXStreamServer(ClassRetriever classRetriever, Authenticator authenticator, ServerMonitor serverMonitor, ThreadPool threadPool, ServerSideClientContextFactory contextFactory, int port) {
        super(new InvocationHandlerAdapter(classRetriever, authenticator, serverMonitor, contextFactory), serverMonitor, threadPool, contextFactory, port);
    }

    public CompleteSocketXStreamServer(int port) {
        this(new NoClassRetriever(), new DefaultAuthenticator(), new NullServerMonitor(), new DefaultThreadPool(), new DefaultServerSideClientContextFactory(), port);
    }

    /**
     * Create a Server Stream Read Writer.
     *
     * @return The Server Stream Read Writer.
     */
    protected AbstractServerStreamReadWriter createServerStreamReadWriter() {
        ServerXStreamStreamReadWriter rw = new ServerXStreamStreamReadWriter(serverMonitor, threadPool);
        return rw;
    }
}
