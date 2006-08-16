package org.codehaus.jremoting.test.socket;

import org.codehaus.jremoting.test.*;
import org.codehaus.jremoting.server.transports.socket.CompleteSocketCustomStreamServer;
import org.codehaus.jremoting.server.transports.socket.CompleteSocketXStreamServer;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.client.factories.ClientSideClassFactory;
import org.codehaus.jremoting.client.transports.socket.SocketCustomStreamHostContext;
import org.codehaus.jremoting.client.transports.socket.SocketXStreamHostContext;

/**
 * Test Custom Stream over sockets.
 *
 * @author Paul Hammant
 */
public class XStreamTestCase extends AbstractHelloTestCase {

    public XStreamTestCase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();

        // server side setup.
        server = new CompleteSocketXStreamServer(10099);
        testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        factory = new ClientSideClassFactory(new SocketXStreamHostContext("127.0.0.1", 10099), false);
        testClient = (TestInterface) factory.lookup("Hello");

        // just a kludge for unit testing given we are intrinsically dealing with
        // threads, JRemoting being a client/server thing
        Thread.yield();
    }

    public void testSpeed() throws Exception {
        super.testSpeed();
    }

    protected void tearDown() throws Exception {
        testClient = null;
        System.gc();
        Thread.yield();
        factory.close();
        Thread.yield();
        server.stop();
        Thread.yield();
        server = null;
        testServer = null;
        super.tearDown();
    }


}
