/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
 * Portions copyright 2001 - 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.jremoting.test.dynamic;

import junit.framework.TestCase;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.NotPublishedException;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.invokers.DynamicInvoker;
import org.codehaus.jremoting.client.transports.socket.SocketStreamInvocationHandler;
import org.codehaus.jremoting.client.transports.ClientCustomStreamDriverFactory;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.transports.ConnectingServer;
import org.codehaus.jremoting.server.transports.socket.SelfContainedSocketStreamServer;
import org.codehaus.jremoting.test.TestInterface;
import org.codehaus.jremoting.test.TestInterface2;
import org.codehaus.jremoting.test.TestInterface3;
import org.codehaus.jremoting.test.TestInterfaceImpl;

/**
 * Test case for the stubless invoker of remote methods
 *
 * @author <a href="mailto:vinayc@apache.org">Vinay Chandran</a>
 */
public class DynamicInvokerTestCase extends TestCase {

    //-------Variables------------//
    protected ConnectingServer server;
    protected TestInterfaceImpl testServer;
    protected TestInterface testClient;
    protected DynamicInvoker dynamicInvoker;


    //-------TestCase overrides-----//

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        // server side setup.
        server = new SelfContainedSocketStreamServer(new ConsoleServerMonitor(), 10101);
        testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        dynamicInvoker = new DynamicInvoker(new SocketStreamInvocationHandler(new ConsoleClientMonitor(),
                new ClientCustomStreamDriverFactory(),
                "127.0.0.1", 10101));

        // just a kludge for unit testing given we are intrinsically dealing with
        // threads, JRemoting being a client/server thing
        Thread.yield();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */

    protected void tearDown() throws Exception {
        testClient = null;
        System.gc();
        Thread.yield();
        dynamicInvoker.close(); //close@client
        Thread.yield();
        server.stop(); //close@server
        Thread.yield();
        server = null;
        testServer = null;
        super.tearDown();
    }

    //-------test cases------------//
    /**
     * A very simple test
     */
    public void testInvocation() throws ConnectionException {

        // Invoking the methods returning void
        dynamicInvoker.invoke("Hello", "hello(java.lang.String)", new Object[]{"Hello!?"}, new Class[]{String.class});
        // test the server has logged the message.
        assertEquals("Hello!?", ((TestInterfaceImpl) testServer).getStoredState("void:hello(String)"));

        //invoke a method returning primitive type
        Integer ret = (Integer) dynamicInvoker.invoke("Hello", "hello2(int)", new Object[]{new Integer(11)}, new Class[]{Integer.TYPE});
        assertEquals(ret, new Integer(11));

        // Invoke on a  non-existent remote object
        try {
            dynamicInvoker.invoke("Hellooo?", "some method", null, null);
            fail("Dynamic Invoker should have failed");
        } catch (NotPublishedException e) {
            // expected
        }


    }

    public void testListMethods() {
        String[] methods = dynamicInvoker.listOfMethods("Hello");
        assertNotNull(methods);
        assertTrue(methods.length > 0);

        methods = dynamicInvoker.listOfMethods("does not exist");
        assertNotNull(methods);
        assertTrue(methods.length == 0);

    }


    public void testList() {
        String[] publications = dynamicInvoker.listServices();
        assertNotNull(publications);
        assertTrue(publications.length > 0);
        assertEquals("Hello", publications[0]);
    }


    /**
     * test methods with multiple arguments
     */

    public void testMultiArgumentMethodInvocation() throws Exception {
        /* JRemoting right now expects method signature in a specific format.
         * within the InvokeMethod.namely with  arguments spaced out by
         * a comma+space.
         *         e.g. hello4(float, double) )
         */
        //Here we test a case where the signature is developed liberally

        StringBuffer buf = (StringBuffer) dynamicInvoker.invoke("Hello", "hello4(float,double)", new Object[]{new Float(10.12), new Double(10.13)}, new Class[]{Float.TYPE, Double.TYPE});
        assertEquals("10.12 10.13", buf.toString());

        buf = (StringBuffer) dynamicInvoker.invoke("Hello", "  hello4  (  float    ,     double   )  ", new Object[]{new Float(10.15), new Double(10.17)}, new Class[]{Float.TYPE, Double.TYPE});
        assertEquals("10.15 10.17", buf.toString());
    }
}
