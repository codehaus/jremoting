package org.codehaus.jremoting.itests.transports;

import org.codehaus.jremoting.client.factories.JRemotingClient;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.transports.socket.SocketTransport;
import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TestFacade3;
import org.codehaus.jremoting.itests.TestFacadeImpl;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.encoders.XStreamEncoding;
import org.codehaus.jremoting.server.transports.socket.SocketStreamServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Test XStream over sockets.
 *
 * @author Paul Hammant
 */
public class XStreamTestCase extends AbstractHelloTestCase {

    protected void setUp() throws Exception {
        super.setUp();

        // server side setup.
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
        server = new SocketStreamServer((ServerMonitor) mockServerMonitor.proxy(), new RefusingStubRetriever(), new NullAuthenticator(),
                new XStreamEncoding(), executorService, new ThreadLocalServerContextFactory(), 10099);
        testServer = new TestFacadeImpl();
        Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestFacade3.class, TestFacade2.class);
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        jremotinClient = new JRemotingClient(new SocketTransport(new ConsoleClientMonitor(),
                new org.codehaus.jremoting.client.encoders.XStreamEncoding(), "localhost", 10099));
        testClient = (TestFacade) jremotinClient.lookupService("Hello");

    }

    protected void tearDown() throws Exception {
        testClient = null;
        System.gc();
        Thread.sleep(400);
        jremotinClient.close();
        server.stop();
    }


}
