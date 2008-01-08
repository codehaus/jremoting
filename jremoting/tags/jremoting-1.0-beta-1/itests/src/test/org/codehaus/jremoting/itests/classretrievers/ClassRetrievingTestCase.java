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
package org.codehaus.jremoting.itests.classretrievers;

import org.codehaus.jremoting.client.ContextFactory;
import org.codehaus.jremoting.client.stubs.StubsFromServer;
import org.codehaus.jremoting.client.factories.JRemotingClient;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.transports.piped.PipedTransport;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.monitors.NullServerMonitor;
import org.codehaus.jremoting.server.stubretrievers.BcelDynamicStubRetriever;
import org.codehaus.jremoting.server.stubretrievers.DynamicStubRetriever;
import org.codehaus.jremoting.server.transports.ConnectingServer;
import org.codehaus.jremoting.server.encoders.ByteStreamEncoding;
import org.codehaus.jremoting.server.transports.piped.PipedServer;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Executors;

/**
 * A special test case that tests dynamic class generation on the server side.
 *
 * @author Paul Hammant
 */
public class ClassRetrievingTestCase extends MockObjectTestCase {

    protected ConnectingServer server;
    protected TestImpl testServer;
    protected TestFacade testClient;


    protected void setUp() throws Exception {

        // server side setup.
        DynamicStubRetriever dyncgen = new BcelDynamicStubRetriever();
        server = new PipedServer(new NullServerMonitor(), dyncgen, new NullAuthenticator(), Executors.newScheduledThreadPool(10), new ThreadLocalServerContextFactory(),
                new ByteStreamEncoding());
        testServer = new TestImpl();
        server.publish(testServer, "Kewl", TestFacade.class);
        dyncgen.generate("Kewl", TestFacade.class, this.getClass().getClassLoader());

        server.start();

        // For piped, server and client can see each other
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream();
        ((PipedServer) server).makeNewConnection(in, out);

        // Client side setup
        Mock mock = mock(ContextFactory.class);
        mock.expects(atLeastOnce()).method("getClientContext").withNoArguments().will(returnValue(null)); 
        JRemotingClient jc = new JRemotingClient(new PipedTransport(new ConsoleClientMonitor(),
                new org.codehaus.jremoting.client.encoders.ByteStreamEncoding(), in, out), (ContextFactory) mock.proxy(), new StubsFromServer());
        testClient = (TestFacade) jc.lookupService("Kewl");

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
