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
package org.codehaus.jremoting.itests.mismatch;

import org.codehaus.jremoting.BadConnectionException;
import org.codehaus.jremoting.client.factories.JRemotingServiceResolver;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.encoders.ByteStreamEncoding;
import org.codehaus.jremoting.client.encoders.ObjectStreamEncoding;
import org.codehaus.jremoting.client.transports.rmi.RmiTransport;
import org.codehaus.jremoting.client.transports.socket.SocketTransport;
import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TestInterface3;
import org.codehaus.jremoting.itests.TestInterfaceImpl;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.transports.rmi.RmiServer;
import org.codehaus.jremoting.server.transports.socket.SocketStreamServer;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * Test Custom Stream over sockets.
 *
 * @author Paul Hammant
 */
public class SocketMismatchTestCase extends MockObjectTestCase {
    private Class x_class;
    private String x_msg;
    private BadConnectionException x_bce;
    private Mock mockServerMonitor;

    protected void setUp() throws Exception {
        super.setUp();
        mockServerMonitor = mock(ServerMonitor.class);
    }

    public void testByteStreamObjectStreamMismatchCanCauseTimeOut() throws Exception {

        SocketStreamServer server = new SocketStreamServer((ServerMonitor) mockServerMonitor.proxy(), 12001);

        TestInterfaceImpl testServer = new TestInterfaceImpl();
        Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestInterface3.class, TestFacade2.class);
        server.publish(testServer, "Hello", pd);
        server.start();

        JRemotingServiceResolver factory = null;
        TestFacade testClient;
        try {

            // Client side setup
            factory = new JRemotingServiceResolver(new SocketTransport(new ConsoleClientMonitor(),
                new ObjectStreamEncoding(), "127.0.0.1", 12001));
            testClient = (TestFacade) factory.lookupService("Hello");

            testClient.hello("hello");


            fail("ByteStreams and ObjectStreams cannot interoperate");
        } catch (BadConnectionException bce) {

            // expected

        } finally {

            testClient = null;
            System.gc();

            try {
                factory.close();
            } catch (Exception e) {
            }

            server.stop();


        }
    }

    public void dont_testObjectStreamByteStreamMismatch() throws Exception {

        // server side setup.
        SocketStreamServer server = new SocketStreamServer((ServerMonitor) mockServerMonitor.proxy(), 12002, new org.codehaus.jremoting.server.encoders.ObjectStreamEncoding());
        TestInterfaceImpl testServer = new TestInterfaceImpl();
        Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestInterface3.class, TestFacade2.class);
        server.publish(testServer, "Hello", pd);
        server.start();

        JRemotingServiceResolver factory = null;
        try {

            // Client side setup
            factory = new JRemotingServiceResolver(new SocketTransport(new ConsoleClientMonitor(),
                new ByteStreamEncoding(), "127.0.0.1", 12002));
            TestFacade testClient = (TestFacade) factory.lookupService("Hello");


            testClient.hello("hello");
            fail("ByteStreams and ObjectStreams cannot interoperate");
        } catch (BadConnectionException bce) {
            // expected.
        } finally {

            System.gc();


            try {
                factory.close();
            } catch (Exception e) {
            }

            server.stop();

        }
    }


    public void dont_testByteStreamRmiMismatch() throws Exception {

        // server side setup.
        SocketStreamServer server = new SocketStreamServer((ServerMonitor) mockServerMonitor.proxy(), 12003);
        TestInterfaceImpl testServer = new TestInterfaceImpl();
        Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestInterface3.class, TestFacade2.class);
        server.publish(testServer, "Hello", pd);
        server.start();

        JRemotingServiceResolver factory = null;
        try {

            // Client side setup
            factory = new JRemotingServiceResolver(new RmiTransport(new ConsoleClientMonitor(), "127.0.0.1", 12003));
            TestFacade testClient = (TestFacade) factory.lookupService("Hello");


            testClient.hello("hello");
            fail("ByteStreams and RMI trasnports cannot interoperate");
        } catch (BadConnectionException bce) {
            // expected.
        } finally {

            System.gc();


            try {
                factory.close();
            } catch (Exception e) {
            }

            server.stop();

        }
    }

    public void dont_testRmiByteStreamMismatch() throws Exception {

        // server side setup.
        RmiServer server = new RmiServer((ServerMonitor) mockServerMonitor.proxy(), 12004);
        TestInterfaceImpl testServer = new TestInterfaceImpl();
        Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestInterface3.class, TestFacade2.class);
        server.publish(testServer, "Hello", pd);
        server.start();

        JRemotingServiceResolver factory = null;
        try {

            // Client side setup
            factory = new JRemotingServiceResolver(new SocketTransport(new ConsoleClientMonitor(),
                new ObjectStreamEncoding(), "127.0.0.1", 12004));
            TestFacade testClient = (TestFacade) factory.lookupService("Hello");


            testClient.hello("hello");
            fail("ByteStreams and RMI trasnports cannot interoperate");
        } catch (BadConnectionException bce) {
            // expected.
        } finally {

            System.gc();


            try {
                factory.close();
            } catch (Exception e) {
            }

            server.stop();

        }
    }

}
