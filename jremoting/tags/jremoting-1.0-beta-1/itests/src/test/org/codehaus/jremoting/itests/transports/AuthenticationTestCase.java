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


import org.codehaus.jremoting.client.context.NullContextFactory;
import org.codehaus.jremoting.client.encoders.ByteStreamEncoding;
import org.codehaus.jremoting.client.factories.JRemotingClient;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.stubs.StubsOnClient;
import org.codehaus.jremoting.client.transports.socket.SocketTransport;
import org.codehaus.jremoting.itests.AbstractJRemotingTestCase;
import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TestFacade3;
import org.codehaus.jremoting.itests.TestFacadeImpl;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.authenticators.NameAndPasswordAuthenticator;
import org.codehaus.jremoting.server.transports.socket.SocketServer;
import org.codehaus.jremoting.ConnectionException;

import java.net.InetSocketAddress;


/**
 * Test authentication.
 *
 * @author Paul Hammant
 */
public class AuthenticationTestCase extends AbstractJRemotingTestCase {

    protected void setUp() throws Exception {
        super.setUp();

        // server side setup.
        server = new SocketServer((ServerMonitor) mockServerMonitor.proxy(), new NameAndPasswordAuthenticator("fred", "wilma"), new InetSocketAddress(10333));
        testServer = new TestFacadeImpl();
        Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestFacade3.class, TestFacade2.class);
        server.publish(testServer, "Hello", pd);
        server.start();
    }

    @Override
    public void testHelloCall() throws Exception {

        // Client side setup
        jremotinClient = new JRemotingClient(new SocketTransport(new ConsoleClientMonitor(), new ByteStreamEncoding(), new InetSocketAddress("127.0.0.1", 10333)), new NullContextFactory(),
                new StubsOnClient(), new org.codehaus.jremoting.client.authentication.NameAndPasswordAuthenticator("fred", "wilma"));
        testClient = (TestFacade) jremotinClient.lookupService("Hello");

        super.testHelloCall();
    }

    public void testfailingChallenge() throws Exception {

        // Client side setup
        jremotinClient = new JRemotingClient(new SocketTransport(new ConsoleClientMonitor(), new ByteStreamEncoding(), new InetSocketAddress("127.0.0.1", 10333)), new NullContextFactory(),
                new StubsOnClient(), new org.codehaus.jremoting.client.authentication.NameAndPasswordAuthenticator("FRED", "wilma"));

        try {
            testClient = (TestFacade) jremotinClient.lookupService("Hello");
            fail();
        } catch (ConnectionException e) {
            assertEquals("Authentication Failed", e.getMessage());
        }

    }

    protected void tearDown() throws Exception {
        testClient = null;
        System.gc();
        Thread.sleep(300);
        jremotinClient.close();
        server.stop();
    }

}