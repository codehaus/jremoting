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


import org.codehaus.jremoting.client.context.ThreadLocalContextFactory;
import org.codehaus.jremoting.client.streams.ByteStream;
import org.codehaus.jremoting.client.resolver.ServiceResolver;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.stubs.StubsOnClient;
import org.codehaus.jremoting.client.transports.SocketTransport;
import org.codehaus.jremoting.client.SocketDetails;
import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TestFacade3;
import org.codehaus.jremoting.itests.TestFacadeImpl;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.transports.MinaServer;

import java.net.InetSocketAddress;

/**
 * Test Custom Stream over sockets.
 *
 * @author Paul Hammant
 */
public class ByteStreamWithMinaTestCase extends AbstractHelloTestCase {

    protected void setUp() throws Exception {
        super.setUp();

        // server side setup.
        server = new MinaServer((ServerMonitor) mockServerMonitor.proxy(), new InetSocketAddress(10333));
        testServer = new TestFacadeImpl();
        server.publish(testServer, "Hello", TestFacade.class, TestFacade3.class, TestFacade2.class);
        server.start();

        // Client side setup
        ConsoleClientMonitor consoleClientMonitor = new ConsoleClientMonitor();
        ByteStream byteStreamEncoding = new ByteStream();
        SocketDetails details = new SocketDetails("localhost", 10333);
        SocketTransport transport = new SocketTransport(consoleClientMonitor, byteStreamEncoding, details);
        jremotingClient = new ServiceResolver(transport,
                new ThreadLocalContextFactory(), new StubsOnClient());
        testClient = (TestFacade) jremotingClient.resolveService("Hello");

    }

    public void testSpeed() throws Exception {
        super.testSpeed();    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        testClient = null;
        System.gc();
        Thread.sleep(300);
        jremotingClient.close();
        server.stop();
        //Thread.sleep(000);
        
    }


}