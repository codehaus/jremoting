package org.codehaus.jremoting.transports;

import org.codehaus.jremoting.test.*;
import org.codehaus.jremoting.server.transports.socket.SelfContainedSocketXStreamServer;
import org.codehaus.jremoting.server.*;
import org.codehaus.jremoting.client.factories.ClientSideStubFactory;
import org.codehaus.jremoting.client.transports.socket.SocketXStreamHostContext;

/**
 * Test XStream over sockets.
 *
 * @author Paul Hammant
 */
public class XStreamTestCase extends AbstractHelloTestCase {

    protected void setUp() throws Exception {
        super.setUp();

        // server side setup.
        server = new SelfContainedSocketXStreamServer(10099);
        testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        factory = new ClientSideStubFactory(new SocketXStreamHostContext("127.0.0.1", 10099), false);
        testClient = (TestInterface) factory.lookupService("Hello");

        // just a kludge for unit testing given we are intrinsically dealing with
        // threads, JRemoting being a client/server thing
        Thread.yield();
    }

    public void testSpeed() throws Exception {
        super.testSpeed();
    }

    public void testLongParamMethod() {
        super.testLongParamMethod();
    }

    int myState;

    protected void tearDown() throws Exception {
        testClient = null;
        System.gc();
        Thread.yield();
        factory.close();
        Thread.yield();
        server.stop();
        if (myState != Server.STOPPED) {
            Thread.sleep(100);
        }
        if (myState != Server.STOPPED) {
            Thread.sleep(100);
        }
        if (myState != Server.STOPPED) {
            Thread.sleep(100);
        }
        if (myState != Server.STOPPED) {
            Thread.sleep(100);
        }
        if (myState != Server.STOPPED) {
            Thread.sleep(100);
        }
        Thread.yield();
        server = null;
        testServer = null;
        super.tearDown();
    }


}
