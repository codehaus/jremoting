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
package org.codehaus.jremoting.test.invalidstate;

import org.codehaus.jremoting.client.*;
import org.codehaus.jremoting.client.factories.ClientSideStubFactory;
import org.codehaus.jremoting.client.factories.DefaultStubHelper;
import org.codehaus.jremoting.client.transports.socket.SocketClientStreamInvoker;
import org.codehaus.jremoting.client.transports.ClientCustomStreamDriverFactory;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.transports.socket.SelfContainedSocketStreamServer;
import org.codehaus.jremoting.test.TestInterface;
import org.codehaus.jremoting.test.TestInterface2;
import org.codehaus.jremoting.test.TestInterface3;
import org.codehaus.jremoting.test.TestInterfaceImpl;
import org.codehaus.jremoting.tools.generator.BcelStubGenerator;
import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import org.jmock.core.Constraint;


/**
 * Tests concerning the bouncing of a server.
 *
 * @author Paul Hammant
 */
public class BouncingServerTestCase extends MockObjectTestCase {

    PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});

    protected void setUp() throws Exception {
        BcelStubGenerator generator = new BcelStubGenerator();
        String testClassesDir = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        generator.setClassGenDir(testClassesDir);
        generator.setPrimaryFacades(pd.getPrimaryFacades());
        generator.setGenName("Hello55");
        generator.generateClass(this.getClass().getClassLoader());
    }

    public void testBouncingOfServerCausesClientProblems() throws Exception {

        // server side setup.
        SelfContainedSocketStreamServer server = startServer();

        ClientSideStubFactory factory = null;
        try {

            // Client side setup

            Mock clientMonitor = mock(ClientMonitor.class);
            clientMonitor.expects(once()).method("methodLogging").withNoArguments().will(returnValue(false));
            clientMonitor.expects(once()).method("invocationFailure").with(new Constraint[] { eq(DefaultStubHelper.class), isA(String.class), isA(String.class), isA(String.class), isA(InvocationException.class
            )});

            factory = new ClientSideStubFactory(new SocketClientStreamInvoker((ClientMonitor) clientMonitor.proxy(),
                    new ClientCustomStreamDriverFactory(), "127.0.0.1", 12201));
            TestInterface testClient = (TestInterface) factory.lookupService("Hello55");


            testClient.hello2(100);

            // Stop server and restarting (essentially binning sessions).
            server.stop();
            server = startServer();

            try {
                testClient.hello2(123);
                fail("Should have barfed with NoSuchSessionException");
            } catch (NoSuchSessionException e) {
                // expected
            }


        } finally {
            System.gc();

            try {
                factory.close();
            } catch (NoSuchSessionException e) {
            }

            server.stop();

        }
    }

    private SelfContainedSocketStreamServer startServer() throws PublicationException {
        SelfContainedSocketStreamServer server = new SelfContainedSocketStreamServer(new ConsoleServerMonitor(), 12201,
                SelfContainedSocketStreamServer.CUSTOMSTREAM);
        TestInterfaceImpl testServer = new TestInterfaceImpl();
        server.publish(testServer, "Hello55", pd);
        server.start();
        return server;
    }

}
