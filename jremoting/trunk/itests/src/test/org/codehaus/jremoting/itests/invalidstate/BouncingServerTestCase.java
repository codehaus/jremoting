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
package org.codehaus.jremoting.itests.invalidstate;

import junit.framework.AssertionFailedError;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.client.encoders.ByteStreamEncoding;
import org.codehaus.jremoting.client.factories.JRemotingClient;
import org.codehaus.jremoting.client.monitors.NullClientMonitor;
import org.codehaus.jremoting.client.transports.socket.SocketTransport;
import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TestFacade3;
import org.codehaus.jremoting.itests.TestFacadeImpl;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.adapters.DefaultInvocationHandler;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.transports.socket.SocketServer;
import org.codehaus.jremoting.tools.generator.BcelStubGenerator;
import org.jmock.MockObjectTestCase;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;


/**
 * Tests concerning the bouncing of a server.
 *
 * @author Paul Hammant
 */
public class BouncingServerTestCase extends MockObjectTestCase {

    Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestFacade3.class, TestFacade2.class);

    protected void setUp() throws Exception {
        BcelStubGenerator generator = new BcelStubGenerator();
        String testClassesDir = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        generator.setClassGenDir(testClassesDir);
        generator.setPrimaryFacade(pd.getPrimaryFacade());
        generator.setGenName("Hello55");
        generator.generateClass(this.getClass().getClassLoader());
    }

    public void testBouncingOfServerCausesClientProblems() throws Exception {

        // server side setup.
        SocketServer server = startServer();

        JRemotingClient jRemotingClient = null;
        try {

            // Client side setup
            jRemotingClient = new JRemotingClient(new SocketTransport(new NullClientMonitor(),
                    new ByteStreamEncoding(), new InetSocketAddress("127.0.0.1", 12201)));
            TestFacade testClient = (TestFacade) jRemotingClient.lookupService("Hello55");
            testClient.intParamReturningInt(100);

            try {
                testClient.intParamReturningInt(123);
                fail("Should have barfed with no such session exception");
            } catch (InvocationException e) {
                assertTrue(e.getMessage().contains("no session on the server"));
                // expected
            }

        } catch(AssertionFailedError afe) {
            System.gc();
            try {
                jRemotingClient.close();
            } catch (InvocationException e) {
            }
            try {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            throw afe;

        }
    }

    private SocketServer startServer() throws PublicationException {
        //SocketServer server = new SocketServer((ServerMonitor) mockServerMonitor.proxy(), new org.codehaus.jremoting.server.encoders.ByteStreamEncoding(), new InetSocketAddress(12201));
        SocketServer server = new SocketServer(new ConsoleServerMonitor(),
                new DefaultInvocationHandler(new ConsoleServerMonitor(), new RefusingStubRetriever(), new NullAuthenticator(), new ThreadLocalServerContextFactory() ) {
                    int ct =0;
                    protected boolean doesSessionExistAndRefreshItIfItDoes(long session) {
                        ct++;
                        if (ct==2) {
                            return false;
                        }
                        return super.doesSessionExistAndRefreshItIfItDoes(session);
                    }
                },
            new org.codehaus.jremoting.server.encoders.ByteStreamEncoding(),
                Executors.newScheduledThreadPool(10), this.getClass().getClassLoader(), new InetSocketAddress(12201));
        TestFacadeImpl testServer = new TestFacadeImpl();
        server.publish(testServer, "Hello55", pd);
        server.start();
        return server;
    }

}
