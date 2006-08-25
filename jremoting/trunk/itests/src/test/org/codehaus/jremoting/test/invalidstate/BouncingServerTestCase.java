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

import junit.framework.TestCase;
import org.codehaus.jremoting.client.ClientInvocationHandler;
import org.codehaus.jremoting.client.HostContext;
import org.codehaus.jremoting.client.NoSuchSessionException;
import org.codehaus.jremoting.client.factories.ClientSideClassFactory;
import org.codehaus.jremoting.client.transports.socket.SocketCustomStreamHostContext;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.ServerException;
import org.codehaus.jremoting.server.transports.socket.SelfContainedSocketCustomStreamServer;
import org.codehaus.jremoting.test.TestInterface;
import org.codehaus.jremoting.test.TestInterface2;
import org.codehaus.jremoting.test.TestInterface3;
import org.codehaus.jremoting.test.TestInterfaceImpl;


/**
 * Tests concerning the bouncing of a server.
 *
 * @author Paul Hammant
 */
public class BouncingServerTestCase extends TestCase {

    public BouncingServerTestCase(String name) {
        super(name);
    }

    public void testBouncingOfServerCausesClientProblems() throws Exception {

        // server side setup.
        SelfContainedSocketCustomStreamServer server = startServer();

        ClientSideClassFactory factory = null;
        try {

            // Client side setup
            HostContext hostContext = new SocketCustomStreamHostContext("127.0.0.1", 12201);
            factory = new ClientSideClassFactory(hostContext, false);
            ClientInvocationHandler ih = hostContext.getInvocationHandler();
            TestInterface testClient = (TestInterface) factory.lookup("Hello");

            // just a kludge for unit testing given we are intrinsically dealing with
            // threads, JRemoting being a client/server thing
            Thread.yield();

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
            Thread.yield();
            factory.close();
            Thread.yield();
            server.stop();
            Thread.yield();
        }
    }

    private SelfContainedSocketCustomStreamServer startServer() throws ServerException, PublicationException {
        SelfContainedSocketCustomStreamServer server = new SelfContainedSocketCustomStreamServer(12201);
        TestInterfaceImpl testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();
        return server;
    }

}
