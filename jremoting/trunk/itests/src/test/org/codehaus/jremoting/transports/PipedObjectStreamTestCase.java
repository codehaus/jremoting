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
package org.codehaus.jremoting.transports;

import org.codehaus.jremoting.client.factories.ClientSideStubFactory;
import org.codehaus.jremoting.client.transports.piped.PipedClientStreamInvocationHandler;
import org.codehaus.jremoting.client.transports.ClientObjectStreamDriverFactory;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.transports.piped.PipedStreamServer;
import org.codehaus.jremoting.server.transports.DefaultServerSideContextFactory;
import org.codehaus.jremoting.server.transports.ServerObjectStreamDriverFactory;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.test.TestInterface;
import org.codehaus.jremoting.test.TestInterface2;
import org.codehaus.jremoting.test.TestInterface3;
import org.codehaus.jremoting.test.TestInterfaceImpl;
import org.codehaus.jremoting.tools.generator.BcelProxyGenerator;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Executors;

/**
 * Test Piped Trasnport (Object Stream)
 *
 * @author Paul Hammant
 */
public class PipedObjectStreamTestCase extends AbstractHelloTestCase {


    public PipedObjectStreamTestCase() {

        // See http://developer.java.sun.com/developer/bugParade/bugs/4499841.html
        // This bug prevents ObjectStream from functioning correctly when used
        // by JRemoting.  You can still use the ObjectStream transports, but
        // should be aware of the limitations.  See testBugParadeBugNumber4499841()
        // in the parent class.
        bugParadeBug4499841StillExists = true;

    }

    protected void setUp() throws Exception {

        // server side setup.
        server = new PipedStreamServer(new ConsoleServerMonitor(), new RefusingStubRetriever(), new NullAuthenticator(),
                Executors.newCachedThreadPool() ,new DefaultServerSideContextFactory(), new ServerObjectStreamDriverFactory());
        testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});


        BcelProxyGenerator generator = new BcelProxyGenerator();
        String testClassesDir = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        generator.setClassGenDir(testClassesDir);
        generator.setPrimaryFacades(pd.getPrimaryFacades());
        generator.setAdditionalFacades(pd.getAdditionalFacades());
        generator.setGenName("Hello33");
        generator.generateSrc(this.getClass().getClassLoader());
        generator.generateClass(this.getClass().getClassLoader());


        server.publish(testServer, "Hello33", pd);
        server.start();

        // For piped, server and client can see each other
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream();
        ((PipedStreamServer) server).makeNewConnection(in, out);

        // Client side setup
        factory = new ClientSideStubFactory(new PipedClientStreamInvocationHandler(new ConsoleClientMonitor(),
                new ClientObjectStreamDriverFactory(), in, out));
        testClient = (TestInterface) factory.lookupService("Hello33");

    }

    protected void tearDown() throws Exception {
        testClient = null;
        System.gc();
        factory.close();
        server.stop();
    }


}
