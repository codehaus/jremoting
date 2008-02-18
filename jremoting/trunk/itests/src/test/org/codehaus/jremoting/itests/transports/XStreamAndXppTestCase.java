package org.codehaus.jremoting.itests.transports;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.codehaus.jremoting.client.resolver.ServiceResolver;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.transports.SocketTransport;
import org.codehaus.jremoting.client.SocketDetails;
import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TestFacade3;
import org.codehaus.jremoting.itests.TestFacadeImpl;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.streams.XStreamConnectionFactory;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.transports.SocketServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Test XStream over sockets.
 *
 * @author Paul Hammant
 */
public class XStreamAndXppTestCase extends AbstractHelloTestCase {

    protected void setUp() throws Exception {
        super.setUp();

        // server side setup.
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
        server = new SocketServer((ServerMonitor) mockServerMonitor.proxy(), new RefusingStubRetriever(), new NullAuthenticator(),
                new XStreamConnectionFactory(new XStream(new XppDriver())), executorService, new ThreadLocalServerContextFactory(), new InetSocketAddress(10099));
        testServer = new TestFacadeImpl();
        Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestFacade3.class, TestFacade2.class);
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        jremotingClient = new ServiceResolver(new SocketTransport(new ConsoleClientMonitor(),
                new org.codehaus.jremoting.client.streams.XStreamConnectionFactory(new XStream(new XppDriver())), new SocketDetails("127.0.0.1", 10099)));
        testClient = (TestFacade) jremotingClient.serviceResolver("Hello");

    }

    protected void tearDown() throws Exception {
        super.tearDown();
        testClient = null;
        System.gc();
        Thread.sleep(400);
        jremotingClient.close();
        server.stop();
    }


}
