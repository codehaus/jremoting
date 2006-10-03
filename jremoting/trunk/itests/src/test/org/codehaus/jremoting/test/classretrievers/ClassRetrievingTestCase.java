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
package org.codehaus.jremoting.test.classretrievers;

import junit.framework.TestCase;
import org.codehaus.jremoting.client.Factory;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.factories.ServerSideStubFactory;
import org.codehaus.jremoting.client.transports.piped.PipedClientStreamInvocationHandler;
import org.codehaus.jremoting.client.transports.ClientCustomStreamDriverFactory;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.stubretrievers.DynamicStubRetriever;
import org.codehaus.jremoting.server.stubretrievers.BcelDynamicStubRetriever;
import org.codehaus.jremoting.server.monitors.NullServerMonitor;
import org.codehaus.jremoting.server.transports.ConnectingServer;
import org.codehaus.jremoting.server.transports.DefaultServerSideClientContextFactory;
import org.codehaus.jremoting.server.transports.ServerCustomStreamDriverFactory;
import org.codehaus.jremoting.server.transports.piped.PipedStreamServer;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Executors;

/**
 * A special test case that tests dynamic class generation on the server side.
 *
 * @author Paul Hammant
 */
public class ClassRetrievingTestCase extends TestCase {

    protected ConnectingServer server;
    protected TestImpl testServer;
    protected TestInterface testClient;


    protected void setUp() throws Exception {

        // server side setup.
        DynamicStubRetriever dyncgen = new BcelDynamicStubRetriever();
        server = new PipedStreamServer(new NullServerMonitor(), dyncgen, new NullAuthenticator(), Executors.newCachedThreadPool(), new DefaultServerSideClientContextFactory(),
                new ServerCustomStreamDriverFactory());
        testServer = new TestImpl();
        server.publish(testServer, "Kewl", TestInterface.class);
        dyncgen.generate("Kewl", TestInterface.class, this.getClass().getClassLoader());

        server.start();

        // For piped, server and client can see each other
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream();
        ((PipedStreamServer) server).makeNewConnection(in, out);

        // Client side setup
        Factory af = new ServerSideStubFactory(new PipedClientStreamInvocationHandler(new ConsoleClientMonitor(),
                new ClientCustomStreamDriverFactory(), in, out));
        testClient = (TestInterface) af.lookupService("Kewl");

    }

    protected void tearDown() throws Exception {
        server.stop();

        server = null;
        testServer = null;
        super.tearDown();
    }


    /**
     * This is the only testXX() method in this class.  Other features of
     * JRemoting are given a thorough test in the 'test' package.
     *
     * @throws Exception as per Junit contract
     */
    public void testDynamicallyGeneratedProxyMethodInvocation() throws Exception {
        // lookup worked ?
        assertNotNull(testClient);

        // Invoke a method over rpc.
        testClient.method0();

        // test the server has logged the message.
        assertEquals("called", (testServer).getStoredState("method0"));
    }


}
