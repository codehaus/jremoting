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
import org.codehaus.jremoting.client.NoSuchSessionException;
import org.codehaus.jremoting.client.encoders.ByteStreamEncoding;
import org.codehaus.jremoting.client.factories.JRemotingClient;
import org.codehaus.jremoting.client.transports.socket.SocketTransport;
import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TestFacade3;
import org.codehaus.jremoting.itests.TestFacadeImpl;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.transports.socket.SocketStreamServer;
import org.codehaus.jremoting.tools.generator.BcelStubGenerator;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;


/**
 * Tests concerning the bouncing of a server.
 *
 * @author Paul Hammant
 */
public class BouncingServerTestCase extends MockObjectTestCase {

    Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestFacade3.class, TestFacade2.class);
    private Mock mockServerMonitor;

    protected void setUp() throws Exception {
        BcelStubGenerator generator = new BcelStubGenerator();
        String testClassesDir = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        generator.setClassGenDir(testClassesDir);
        generator.setPrimaryFacade(pd.getPrimaryFacade());
        generator.setGenName("Hello55");
        generator.generateClass(this.getClass().getClassLoader());
        mockServerMonitor = mock(ServerMonitor.class);
    }

    public void testBouncingOfServerCausesClientProblems() throws Exception {

        // server side setup.
        SocketStreamServer server = startServer();

        JRemotingClient jRemotingClient = null;
        try {

            // Client side setup

            Mock clientMonitor = mock(ClientMonitor.class);
            clientMonitor.expects(once()).method("methodLogging").withNoArguments().will(returnValue(false));
            clientMonitor.expects(once()).method("invocationFailure").with(new Constraint[] { eq(JRemotingClient.DefaultStubHelper.class), isA(String.class), isA(String.class), isA(String.class), isA(InvocationException.class
            )});

            jRemotingClient = new JRemotingClient(new SocketTransport((ClientMonitor) clientMonitor.proxy(),
                    new ByteStreamEncoding(), "127.0.0.1", 12201));
            TestFacade testClient = (TestFacade) jRemotingClient.lookupService("Hello55");


            testClient.intParamReturningInt(100);

            // Stop server and restarting (essentially binning sessions).
            server.stop();
            server = startServer();

            try {
                testClient.intParamReturningInt(123);
                fail("Should have barfed with NoSuchSessionException");
            } catch (NoSuchSessionException e) {
                // expected
            }


        } finally {
            System.gc();

            try {
                jRemotingClient.close();
            } catch (NoSuchSessionException e) {
            }

            server.stop();

        }
    }

    private SocketStreamServer startServer() throws PublicationException {
        SocketStreamServer server = new SocketStreamServer((ServerMonitor) mockServerMonitor.proxy(), 12201,
                new org.codehaus.jremoting.server.encoders.ByteStreamEncoding());
        TestFacadeImpl testServer = new TestFacadeImpl();
        server.publish(testServer, "Hello55", pd);
        server.start();
        return server;
    }

}
