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

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.client.SocketDetails;
import org.codehaus.jremoting.client.streams.ByteStreamConnectionFactory;
import org.codehaus.jremoting.client.resolver.ServiceResolver;
import org.codehaus.jremoting.client.monitors.NullClientMonitor;
import org.codehaus.jremoting.client.monitors.SimpleRetryingClientMonitor;
import org.codehaus.jremoting.client.transports.SocketTransport;
import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TestFacade3;
import org.codehaus.jremoting.itests.TestFacadeImpl;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.adapters.DefaultServerDelegate;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.transports.SocketServer;
import org.codehaus.jremoting.tools.generator.BcelStubGenerator;
import org.jmock.MockObjectTestCase;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;


/**
 * Tests concerning the bouncing of a server.
 *
 * @author Paul Hammant
 */
public class SuspendedServerTestCase extends MockObjectTestCase {

    Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestFacade3.class, TestFacade2.class);

    protected void setUp() throws Exception {
        BcelStubGenerator generator = new BcelStubGenerator();
        String testClassesDir = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        generator.setClassGenDir(testClassesDir);
        generator.setPrimaryFacade(pd.getPrimaryFacade());
        generator.setGenName("Hello55");
        generator.generateClass(this.getClass().getClassLoader());
    }

    public void testSuspendingPercolatesToTheClientWithoutRetries() throws Exception {

        // server side setup.
        SocketServer server = startServer();

        ServiceResolver serviceResolver = null;
        try {

            ClientMonitor clientMonitor = new NullClientMonitor(); // no retry policy

            // Client side setup

            serviceResolver = new ServiceResolver(new SocketTransport(clientMonitor,
                    new ByteStreamConnectionFactory(), new SocketDetails("127.0.0.1", 12201)));
            TestFacade testClient = (TestFacade) serviceResolver.resolveService("Hello55");

            testClient.intParamReturningInt(100);

            server.suspend();

            try {
                testClient.intParamReturningInt(123);
                fail("Should have barfed");
            } catch (InvocationException e) {
                String s = e.getMessage();
                e.printStackTrace();
                assertTrue(s.contains("Service suspended"));
                // expected
            }

        } finally {
            System.gc();
            try {
                serviceResolver.close();
            } catch (InvocationException e) {
            }
            try {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void testSuspendingHandledByRetryingMonitor() throws Exception {

        // server side setup.
        final SocketServer server = startServer();

        ServiceResolver serviceResolver = null;
        try {

            ClientMonitor clientMonitor = new SimpleRetryingClientMonitor(); // attempts retries if suspended

            // Client side setup

            serviceResolver = new ServiceResolver(new SocketTransport(clientMonitor,
                    new ByteStreamConnectionFactory(), new SocketDetails("127.0.0.1", 12201)));
            TestFacade testClient = (TestFacade) serviceResolver.resolveService("Hello55");

            testClient.intParamReturningInt(100);

            server.suspend();
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    server.resume();
                }
            }.start();;

            testClient.intParamReturningInt(123);

        } finally {
            System.gc();
            try {
                serviceResolver.close();
            } catch (InvocationException e) {
            }
            try {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private SocketServer startServer() throws PublicationException {
        SocketServer server = new SocketServer(new ConsoleServerMonitor(),
                new DefaultServerDelegate(new ConsoleServerMonitor(), new RefusingStubRetriever(), new NullAuthenticator(), new ThreadLocalServerContextFactory()),
            new org.codehaus.jremoting.server.streams.ByteStreamConnectionFactory(),
                Executors.newScheduledThreadPool(10), this.getClass().getClassLoader(), new InetSocketAddress(12201));
        TestFacadeImpl testServer = new TestFacadeImpl();
        server.publish(testServer, "Hello55", pd);
        server.start();
        return server;
    }

}