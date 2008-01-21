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

import org.codehaus.jremoting.RedirectedException;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.client.encoders.ByteStreamEncoding;
import org.codehaus.jremoting.client.factories.JRemotingClient;
import org.codehaus.jremoting.client.monitors.NullClientMonitor;
import org.codehaus.jremoting.client.transports.socket.SocketTransport;
import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TestFacade3;
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
 * Tests concerning the redirecting of a server.
 *
 * @author Paul Hammant
 */
public class RedirectedServerTestCase extends MockObjectTestCase {

    Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestFacade3.class, TestFacade2.class);

    protected void setUp() throws Exception {
        BcelStubGenerator generator = new BcelStubGenerator();
        String testClassesDir = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        generator.setClassGenDir(testClassesDir);
        generator.setPrimaryFacade(pd.getPrimaryFacade());
        generator.setGenName("Hello55");
        generator.generateClass(this.getClass().getClassLoader());
    }


    public void testRedirectingHandled() throws Exception {

        // server side setup.
        final SocketServer server = startServer();

        JRemotingClient jRemotingClient = null;
        try {

            ClientMonitor clientMonitor = new NullClientMonitor();

            // Client side setup

            jRemotingClient = new JRemotingClient(new SocketTransport(clientMonitor,
                    new ByteStreamEncoding(), new InetSocketAddress("127.0.0.1", 12201)));

            try {
                jRemotingClient.lookupService("Hello55");
                fail("should have barfed");
            } catch (RedirectedException e) {
                assertEquals("redirected to: localhost:12202", e.getMessage());
            }


        } finally {
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

        }
    }

    private SocketServer startServer() throws PublicationException {
        SocketServer server = new SocketServer(new ConsoleServerMonitor(),
                new DefaultInvocationHandler(new ConsoleServerMonitor(), new RefusingStubRetriever(), new NullAuthenticator(), new ThreadLocalServerContextFactory()),
            new org.codehaus.jremoting.server.encoders.ByteStreamEncoding(),
                Executors.newScheduledThreadPool(10), this.getClass().getClassLoader(), new InetSocketAddress(12201));
        server.redirect("Hello55", "localhost", 12202);
        server.start();
        return server;
    }

}