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

import org.codehaus.jremoting.client.factories.JRemotingClient;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.encoders.ObjectStreamEncoding;
import org.codehaus.jremoting.client.transports.socket.SocketTransport;
import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TestFacade3;
import org.codehaus.jremoting.itests.TestFacadeImpl;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.transports.socket.SocketServer;

import java.net.InetSocketAddress;


/**
 * Test Object Stream over sockets.
 *
 * @author Paul Hammant
 */
public class ObjectStreamOverSocketTestCase extends AbstractHelloTestCase {

    public ObjectStreamOverSocketTestCase() {

        // See http://developer.java.sun.com/developer/bugParade/bugs/4499841.html
        // This bug prevents ObjectStream from functioning correctly when used
        // by JRemoting.  You can still use the ObjectStream transports, but
        // should be aware of the limitations.  See testBugParadeBugNumber4499841()
        // in the parent class.
        bugParadeBug4499841StillExists = true;

    }

    protected void setUp() throws Exception {
        super.setUp();

        // server side setup.
        server = new SocketServer((ServerMonitor) mockServerMonitor.proxy(), new org.codehaus.jremoting.server.encoders.ObjectStreamEncoding(), new InetSocketAddress(10002));

        testServer = new TestFacadeImpl();

        Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestFacade3.class, TestFacade2.class);
        server.publish(testServer, "Hello", pd);

        server.start();

        // Client side setup
        jremotingClient = new JRemotingClient(new SocketTransport(new ConsoleClientMonitor(),
                new ObjectStreamEncoding(),
                new InetSocketAddress("127.0.0.1", 10002)));
        testClient = (TestFacade) jremotingClient.lookupService("Hello");


    }


    protected void tearDown() throws Exception {
        super.tearDown();
        testClient = null;
        System.gc();
        Thread.sleep(300);
        jremotingClient.close();
        server.stop();
    }


}
