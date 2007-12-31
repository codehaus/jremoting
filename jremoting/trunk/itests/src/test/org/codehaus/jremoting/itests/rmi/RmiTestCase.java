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
package org.codehaus.jremoting.itests.rmi;

import org.codehaus.jremoting.client.ServiceResolver;
import org.codehaus.jremoting.client.factories.JRemotingServiceResolver;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.transports.rmi.RmiTransport;
import org.codehaus.jremoting.itests.TestInterface;
import org.codehaus.jremoting.itests.TestInterface2;
import org.codehaus.jremoting.itests.TestInterface3;
import org.codehaus.jremoting.itests.TestInterfaceImpl;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.transports.ConnectingServer;
import org.codehaus.jremoting.server.transports.rmi.RmiServer;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;


/**
 * Test RMI transport
 * <p/>
 * This test only contains a single tesXXX() method because of
 * http://developer.java.sun.com/developer/bugParade/bugs/4267864.html
 *
 * @author Paul Hammant
 */
public class RmiTestCase extends MockObjectTestCase {

    private ConnectingServer server;
    private TestInterfaceImpl testServer;
    private TestInterface testClient;
    private Mock mockServerMonitor;

    protected void setUp() throws Exception {
        super.setUp();

        mockServerMonitor = mock(ServerMonitor.class);

        // server side setup.
        server = new RmiServer((ServerMonitor) mockServerMonitor.proxy(), 10003);
        testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        ServiceResolver af = new JRemotingServiceResolver(new RmiTransport(new ConsoleClientMonitor(), "127.0.0.1", 10003));
        testClient = (TestInterface) af.lookupService(TestInterface.class, "Hello");


    }

    protected void tearDown() throws Exception {
        server.stop();

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
