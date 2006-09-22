package org.codehaus.jremoting.transports;

import org.codehaus.jremoting.server.transports.socket.SelfContainedSocketXStreamServer;
import org.codehaus.jremoting.server.transports.DefaultServerSideClientContextFactory;
import org.codehaus.jremoting.server.classretrievers.NoStubRetriever;
import org.codehaus.jremoting.server.authenticators.DefaultAuthenticator;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.Server;
import org.codehaus.jremoting.test.TestInterfaceImpl;
import org.codehaus.jremoting.test.TestInterface;
import org.codehaus.jremoting.test.TestInterface3;
import org.codehaus.jremoting.test.TestInterface2;
import org.codehaus.jremoting.client.factories.ClientSideStubFactory;
import org.codehaus.jremoting.client.transports.socket.SocketXStreamHostContext;

import java.util.concurrent.Executors;

import junit.framework.TestCase;

/**
 * Test XStream over sockets.
 *
 * @author Paul Hammant
 */
public class XStream2TestCase extends TestCase {


    public void testSpeed() throws Exception {
        // server side setup.
        SelfContainedSocketXStreamServer server = new SelfContainedSocketXStreamServer(new NoStubRetriever(), new DefaultAuthenticator(), new ConsoleServerMonitor(),
                Executors.newCachedThreadPool(), new DefaultServerSideClientContextFactory(), 10099);
        TestInterfaceImpl testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        ClientSideStubFactory factory = new ClientSideStubFactory(new SocketXStreamHostContext("127.0.0.1", 10099), false);
        TestInterface testClient = (TestInterface) factory.lookupService("Hello");

        testClient.hello("hello 1");

        testClient.hello("hello 2");

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
