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
package org.codehaus.jremoting.itests.transports;


import org.codehaus.jremoting.client.factories.StubsOnClient;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.transports.ClientByteStreamDriverFactory;
import org.codehaus.jremoting.client.transports.socket.SocketClientInvoker;
import org.codehaus.jremoting.itests.TestInterface;
import org.codehaus.jremoting.itests.TestInterface2;
import org.codehaus.jremoting.itests.TestInterface3;
import org.codehaus.jremoting.itests.TestInterfaceImpl;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.transports.socket.SelfContainedSocketStreamServer;


/**
 * Test Custom Stream over sockets.
 *
 * @author Paul Hammant
 */
public class ByteStreamTestCase extends AbstractHelloTestCase {

    protected void setUp() throws Exception {
        super.setUp();

        // server side setup.
        server = new SelfContainedSocketStreamServer((ServerMonitor) mockServerMonitor.proxy(), 10333);
        testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        factory = new StubsOnClient(new SocketClientInvoker(new ConsoleClientMonitor(),
                new ClientByteStreamDriverFactory(), "localhost", 10333));
        testClient = (TestInterface) factory.lookupService("Hello");

    }


    protected void tearDown() throws Exception {
        testClient = null;
        System.gc();
        Thread.sleep(300);
        factory.close();
        server.stop();
    }


}
