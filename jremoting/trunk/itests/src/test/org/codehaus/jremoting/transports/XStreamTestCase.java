package org.codehaus.jremoting.transports;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.client.factories.ClientSideStubFactory;
import org.codehaus.jremoting.client.transports.socket.SocketClientStreamInvocationHandler;
import org.codehaus.jremoting.client.transports.ClientXStreamDriverFactory;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.transports.DefaultServerSideContextFactory;
import org.codehaus.jremoting.server.transports.ServerXStreamDriverFactory;
import org.codehaus.jremoting.server.transports.socket.SelfContainedSocketStreamServer;
import org.codehaus.jremoting.test.TestInterface;
import org.codehaus.jremoting.test.TestInterface2;
import org.codehaus.jremoting.test.TestInterface3;
import org.codehaus.jremoting.test.TestInterfaceImpl;

/**
 * Test XStream over sockets.
 *
 * @author Paul Hammant
 */
public class XStreamTestCase extends AbstractHelloTestCase {

    protected void setUp() throws Exception {

        // server side setup.
        ExecutorService executorService = Executors.newCachedThreadPool();
        ConsoleServerMonitor serverMonitor = new ConsoleServerMonitor();
        server = new SelfContainedSocketStreamServer(serverMonitor, new RefusingStubRetriever(), new NullAuthenticator(),
                new ServerXStreamDriverFactory(), executorService, new DefaultServerSideContextFactory(), 10099);
        testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        factory = new ClientSideStubFactory(new SocketClientStreamInvocationHandler(new ConsoleClientMonitor(),
                new ClientXStreamDriverFactory(), "localhost", 10099));
        testClient = (TestInterface) factory.lookupService("Hello");

    }


    protected void tearDown() throws Exception {
        testClient = null;
        System.gc();
        Thread.sleep(400);
        factory.close();
        server.stop();
    }


}
