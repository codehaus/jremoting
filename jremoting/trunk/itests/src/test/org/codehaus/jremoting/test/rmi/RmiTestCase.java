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
package org.codehaus.jremoting.test.rmi;

import junit.framework.TestCase;
import org.codehaus.jremoting.client.Factory;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.factories.ClientSideStubFactory;
import org.codehaus.jremoting.client.transports.rmi.RmiClientInvocationHandler;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.transports.ConnectingServer;
import org.codehaus.jremoting.server.transports.rmi.RmiServer;
import org.codehaus.jremoting.test.TestInterface;
import org.codehaus.jremoting.test.TestInterface2;
import org.codehaus.jremoting.test.TestInterface3;
import org.codehaus.jremoting.test.TestInterfaceImpl;


/**
 * Test RMI transport
 * <p/>
 * This test only contains a single tesXXX() method because of
 * http://developer.java.sun.com/developer/bugParade/bugs/4267864.html
 *
 * @author Paul Hammant
 */
public class RmiTestCase extends TestCase {

    private ConnectingServer server;
    private TestInterfaceImpl testServer;
    private TestInterface testClient;

    protected void setUp() throws Exception {
        super.setUp();

        // server side setup.
        server = new RmiServer(new ConsoleServerMonitor(), 10003);
        testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        Factory af = new ClientSideStubFactory(new RmiClientInvocationHandler(new ConsoleClientMonitor(), "127.0.0.1", 10003));
        testClient = (TestInterface) af.lookupService("Hello");

        // just a kludge for unit testing given we are intrinsically dealing with
        // threads, JRemoting being a client/server thing
        Thread.yield();
    }

    protected void tearDown() throws Exception {
        server.stop();
        Thread.yield();
        server = null;
        testServer = null;
        super.tearDown();
    }

    public void testSpeed() throws Exception {

        for (int i = 1; i < 10000; i++) {
            testClient.testSpeed();
        }

    }

}
