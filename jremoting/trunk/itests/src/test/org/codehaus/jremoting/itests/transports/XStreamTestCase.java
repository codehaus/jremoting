package org.codehaus.jremoting.itests.transports;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.client.factories.StubsOnClient;
import org.codehaus.jremoting.client.transports.socket.SocketClientInvoker;
import org.codehaus.jremoting.client.transports.ClientXStreamDriverFactory;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.factories.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.transports.ServerXStreamDriverFactory;
import org.codehaus.jremoting.server.transports.socket.SelfContainedSocketStreamServer;
import org.codehaus.jremoting.itests.TestInterface;
import org.codehaus.jremoting.itests.TestInterface2;
import org.codehaus.jremoting.itests.TestInterface3;
import org.codehaus.jremoting.itests.TestInterfaceImpl;

/**
 * Test XStream over sockets.
 *
 * @author Paul Hammant
 */
public class XStreamTestCase extends AbstractHelloTestCase {

    protected void setUp() throws Exception {

        // server side setup.
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
        ConsoleServerMonitor serverMonitor = new ConsoleServerMonitor();
        server = new SelfContainedSocketStreamServer(serverMonitor, new RefusingStubRetriever(), new NullAuthenticator(),
                new ServerXStreamDriverFactory(), executorService, new ThreadLocalServerContextFactory(), 10099);
        testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        factory = new StubsOnClient(new SocketClientInvoker(new ConsoleClientMonitor(),
                new ClientXStreamDriverFactory(), "localhost", 10099));
        testClient = (TestInterface) factory.lookupService("Hello");

    }


    public void testHello4Call() throws Exception {
        super.testHello4Call();    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected void tearDown() throws Exception {
        testClient = null;
        System.gc();
        Thread.sleep(400);
        factory.close();
        server.stop();
    }


}
