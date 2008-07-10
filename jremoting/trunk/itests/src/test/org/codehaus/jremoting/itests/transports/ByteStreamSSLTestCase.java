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
import org.codehaus.jremoting.client.streams.ByteStreamConnectionFactory;
import org.codehaus.jremoting.client.resolver.ServiceResolver;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.stubs.StubsViaReflection;
import org.codehaus.jremoting.client.transports.SSLSocketTransport;
import org.codehaus.jremoting.client.SocketDetails;
import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TestFacade3;
import org.codehaus.jremoting.itests.TestFacadeImpl;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.transports.SSLSocketServer;

import java.io.File;
import java.net.InetSocketAddress;


/**
 * Test Custom Stream over sockets.
 *
 * @author Paul Hammant
 */
public class ByteStreamSSLTestCase extends AbstractHelloTestCase {


    //
    //
    //  Make sure you set ...

    //      -Djavax.net.ssl.keyStore=keyStoreForUnitTestingOnly -Djavax.net.ssl.keyStorePassword=123456
    //
    //  ... in your IDE's run options for this test.
    //


    protected void setUp() throws Exception {

        assertNotNull(System.getProperty("javax.net.ssl.keyStore"));
        assertNotNull(new File(System.getProperty("javax.net.ssl.keyStore")).exists());

        super.setUp();

        // server side setup.
        server = new SSLSocketServer((ServerMonitor) mockServerMonitor.proxy(), new InetSocketAddress(10334));
        testServer = new TestFacadeImpl();
        Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestFacade3.class, TestFacade2.class);
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        jremotingClient = new ServiceResolver(new SSLSocketTransport(new ConsoleClientMonitor(), new ByteStreamConnectionFactory(), new SocketDetails("localhost", 10334)),
                new ThreadLocalContextFactory(), new StubsViaReflection());
        testClient = (TestFacade) jremotingClient.resolveService("Hello");

    }

    public void testSpeed() throws Exception {
        super.testSpeed();  
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        testClient = null;
        System.gc();
        Thread.sleep(300);
        jremotingClient.close();
        server.stop();
        Thread.sleep(300);
    }


}