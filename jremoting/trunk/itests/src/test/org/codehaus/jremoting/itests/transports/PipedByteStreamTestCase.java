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

import org.codehaus.jremoting.client.resolver.ServiceResolver;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.transports.PipedTransport;
import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TestFacade3;
import org.codehaus.jremoting.itests.TestFacadeImpl;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.streams.ByteStream;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.transports.PipedServer;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Executors;

/**
 * Test Piped Trasnport (Custom Stream)
 *
 * @author Paul Hammant
 */
public class PipedByteStreamTestCase extends AbstractHelloTestCase {


    protected void setUp() throws Exception {
        super.setUp();

        // server side setup.
        server = new PipedServer((ServerMonitor) mockServerMonitor.proxy(), new RefusingStubRetriever(), new NullAuthenticator(),
                Executors.newScheduledThreadPool(10) ,new ThreadLocalServerContextFactory(), new ByteStream());
        testServer = new TestFacadeImpl();
        Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestFacade3.class, TestFacade2.class);
        server.publish(testServer, "Hello", pd);
        server.start();

        // For piped, server and client can see each other
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream();
        ((PipedServer) server).makeNewConnection(in, out);

        // Client side setup
        sr = new ServiceResolver(new PipedTransport(new ConsoleClientMonitor(),
                new org.codehaus.jremoting.client.streams.ByteStream(), in, out));
        testClient = sr.resolveService("Hello");

    }

    protected int getNumIterationsForSpeedTest() {
        return super.getNumIterationsForSpeedTest() * 2;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        testClient = null;
        System.gc();

        sr.close();

        server.stop();

    }


}
