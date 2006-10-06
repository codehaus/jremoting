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
package org.codehaus.jremoting.test;

import org.codehaus.jremoting.BadConnectionException;
import org.codehaus.jremoting.client.ConnectionRefusedException;
import org.codehaus.jremoting.client.NotPublishedException;
import org.codehaus.jremoting.client.NoSuchSessionException;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.factories.ClientSideStubFactory;
import org.codehaus.jremoting.client.transports.rmi.RmiClientInvoker;
import org.codehaus.jremoting.client.transports.socket.SocketClientStreamInvoker;
import org.codehaus.jremoting.client.transports.ClientObjectStreamDriverFactory;
import org.codehaus.jremoting.client.transports.ClientCustomStreamDriverFactory;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.transports.socket.SelfContainedSocketStreamServer;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.jmock.MockObjectTestCase;


/**
 * Test basic client server features.
 *
 * @author Paul Hammant
 */
public class BasicClientServerTestCase extends MockObjectTestCase {

    public void testNoServer() throws Exception {
        try {
            new ClientSideStubFactory(new SocketClientStreamInvoker(new ConsoleClientMonitor(),
                new ClientCustomStreamDriverFactory(), "127.0.0.1", 12345));
            fail("Should have have failed.");
        } catch (ConnectionRefusedException e) {
            // what we expetcted
        }
    }

    public void testNotPublishedExceptionThrownWhenNeeded() throws Exception {

        // server side setup.
        SelfContainedSocketStreamServer server = new SelfContainedSocketStreamServer(new ConsoleServerMonitor(), 12333);

        TestInterfaceImpl testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        try {

            ClientSideStubFactory cssf = new ClientSideStubFactory(new SocketClientStreamInvoker(new ConsoleClientMonitor(),
                new ClientCustomStreamDriverFactory(), "127.0.0.1", 12333));
            cssf.lookupService("foo");

            fail("should have barfed");
        } catch (NotPublishedException e) {
            //expected 
        } finally {
            server.stop();
        }



    }


    public void testNoReferenceExceptionThrownWhenNeeded() throws Exception {

        // server side setup.
        SelfContainedSocketStreamServer server = new SelfContainedSocketStreamServer(new ConsoleServerMonitor(), 12331,
                SelfContainedSocketStreamServer.OBJECTSTREAM);

        TestInterfaceImpl testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        try {

            SocketClientStreamInvoker clientInvocationHandler = new SocketClientStreamInvoker(new ConsoleClientMonitor(),
                    new ClientObjectStreamDriverFactory(), "localhost", 12331);
            ClientSideStubFactory cssf = new ClientSideStubFactory(clientInvocationHandler);
            cssf.lookupService("Hello");
            clientInvocationHandler.invoke(new InvokeMethod("Hello", "Main", "ping()",new Object [0], (long) 44332, (long) 21));

            fail("should have barfed");
        } catch (NoSuchSessionException e) {
            //expected
        } finally {
            server.stop();
        }



    }




    public void donttestMismatch2() throws Exception {

        // server side setup.
        // Object
        SelfContainedSocketStreamServer server = new SelfContainedSocketStreamServer(new ConsoleServerMonitor(), 12347, SelfContainedSocketStreamServer.OBJECTSTREAM);
        TestInterfaceImpl testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();


        // Client side setup
        try {
            new ClientSideStubFactory(new SocketClientStreamInvoker(new ConsoleClientMonitor(),
                new ClientCustomStreamDriverFactory(), "127.0.0.1", 12347));
            fail("Expected mismatch exception");
        } catch (BadConnectionException e) {
            if (e.getMessage().indexOf("mismatch") < 0) {
                throw e;
            }

        } finally {
            server.stop();
        }
    }

    public void donttestMismatch3() throws Exception {

        // server side setup.
        SelfContainedSocketStreamServer server = new SelfContainedSocketStreamServer(new ConsoleServerMonitor(), 12348);
        TestInterfaceImpl testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        try {
            new ClientSideStubFactory(new RmiClientInvoker(new ConsoleClientMonitor(), "127.0.0.1", 12348));
            fail("Expected mismatch exception");
        } catch (BadConnectionException e) {
            if (e.getMessage().indexOf("mismatch") < 0) {
                throw e;
            }

        } finally {
            server.stop();
        }
    }

}
