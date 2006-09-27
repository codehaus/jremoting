package org.codehaus.jremoting.transports;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.client.factories.ClientSideStubFactory;
import org.codehaus.jremoting.client.transports.socket.SocketXStreamHostContext;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.classretrievers.NoStubRetriever;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.transports.DefaultServerSideClientContextFactory;
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
        super.setUp();

        // server side setup.
        ExecutorService executorService = Executors.newCachedThreadPool();
        ConsoleServerMonitor serverMonitor = new ConsoleServerMonitor();
        server = new SelfContainedSocketStreamServer(serverMonitor, new NoStubRetriever(), new NullAuthenticator(),
                new ServerXStreamDriverFactory(), executorService, new DefaultServerSideClientContextFactory(), 10099);
        testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        factory = new ClientSideStubFactory(new SocketXStreamHostContext(new ConsoleClientMonitor(), "127.0.0.1", 10099));
        testClient = (TestInterface) factory.lookupService("Hello");

    }

    protected void tearDown() throws Exception {

        testClient = null;

        System.gc();

        Thread.sleep(2000);

        factory.close();

        Thread.sleep(1000);

        server.stop();
        server = null;
        testServer = null;
        super.tearDown();
    }


}
