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
package org.codehaus.jremoting.test.mismatch;

import junit.framework.TestCase;
import org.codehaus.jremoting.api.BadConnectionException;
import org.codehaus.jremoting.client.factories.ClientSideStubFactory;
import org.codehaus.jremoting.client.transports.rmi.RmiHostContext;
import org.codehaus.jremoting.client.transports.socket.SocketCustomStreamHostContext;
import org.codehaus.jremoting.client.transports.socket.SocketObjectStreamHostContext;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.transports.rmi.RmiServer;
import org.codehaus.jremoting.server.transports.socket.SelfContainedSocketStreamServer;
import org.codehaus.jremoting.test.TestInterface;
import org.codehaus.jremoting.test.TestInterface2;
import org.codehaus.jremoting.test.TestInterface3;
import org.codehaus.jremoting.test.TestInterfaceImpl;

/**
 * Test Custom Stream over sockets.
 *
 * @author Paul Hammant
 */
public class SocketMismatchTestCase extends TestCase {

    public void testCustomStreamObjectStreamMismatch() throws Exception {

        // server side setup.
        SelfContainedSocketStreamServer server = new SelfContainedSocketStreamServer(new ConsoleServerMonitor(), 12001);
        TestInterfaceImpl testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        ClientSideStubFactory factory = null;
        try {

            // Client side setup
            factory = new ClientSideStubFactory(new SocketObjectStreamHostContext("127.0.0.1", 12001));
            TestInterface testClient = (TestInterface) factory.lookupService("Hello");

            // just a kludge for unit testing given we are intrinsically dealing with
            // threads, JRemoting being a client/server thing
            Thread.yield();

            testClient.hello("hello");
            fail("CustomStreams and ObjectStreams cannot interoperate");
        } catch (BadConnectionException bce) {
            // expected.
        } finally {

            System.gc();
            Thread.yield();

            try {
                factory.close();
            } catch (Exception e) {
            }
            Thread.yield();
            server.stop();
            Thread.yield();
        }
    }

    public void dont_testObjectStreamCustomStreamMismatch() throws Exception {

        // server side setup.
        SelfContainedSocketStreamServer server = new SelfContainedSocketStreamServer(new ConsoleServerMonitor(), 12002, SelfContainedSocketStreamServer.OBJECTSTREAM);
        TestInterfaceImpl testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        ClientSideStubFactory factory = null;
        try {

            // Client side setup
            factory = new ClientSideStubFactory(new SocketCustomStreamHostContext("127.0.0.1", 12002));
            TestInterface testClient = (TestInterface) factory.lookupService("Hello");

            // just a kludge for unit testing given we are intrinsically dealing with
            // threads, JRemoting being a client/server thing
            Thread.yield();

            testClient.hello("hello");
            fail("CustomStreams and ObjectStreams cannot interoperate");
        } catch (BadConnectionException bce) {
            // expected.
        } finally {

            System.gc();
            Thread.yield();

            try {
                factory.close();
            } catch (Exception e) {
            }
            Thread.yield();
            server.stop();
            Thread.yield();
        }
    }


    public void dont_testCustomStreamRmiMismatch() throws Exception {

        // server side setup.
        SelfContainedSocketStreamServer server = new SelfContainedSocketStreamServer(new ConsoleServerMonitor(), 12003);
        TestInterfaceImpl testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        ClientSideStubFactory factory = null;
        try {

            // Client side setup
            factory = new ClientSideStubFactory(new RmiHostContext("127.0.0.1", 12003));
            TestInterface testClient = (TestInterface) factory.lookupService("Hello");

            // just a kludge for unit testing given we are intrinsically dealing with
            // threads, JRemoting being a client/server thing
            Thread.yield();

            testClient.hello("hello");
            fail("CustomStreams and RMI trasnports cannot interoperate");
        } catch (BadConnectionException bce) {
            // expected.
        } finally {

            System.gc();
            Thread.yield();

            try {
                factory.close();
            } catch (Exception e) {
            }
            Thread.yield();
            server.stop();
            Thread.yield();
        }
    }

    public void dont_testRmiCustomStreamMismatch() throws Exception {

        // server side setup.
        RmiServer server = new RmiServer(new ConsoleServerMonitor(), 12004);
        TestInterfaceImpl testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        ClientSideStubFactory factory = null;
        try {

            // Client side setup
            factory = new ClientSideStubFactory(new SocketCustomStreamHostContext("127.0.0.1", 12004));
            TestInterface testClient = (TestInterface) factory.lookupService("Hello");

            // just a kludge for unit testing given we are intrinsically dealing with
            // threads, JRemoting being a client/server thing
            Thread.yield();

            testClient.hello("hello");
            fail("CustomStreams and RMI trasnports cannot interoperate");
        } catch (BadConnectionException bce) {
            // expected.
        } finally {

            System.gc();
            Thread.yield();

            try {
                factory.close();
            } catch (Exception e) {
            }
            Thread.yield();
            server.stop();
            Thread.yield();
        }
    }

}
