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
import org.codehaus.jremoting.client.transports.socket.SocketClientStreamInvocationHandler;
import org.codehaus.jremoting.client.transports.ClientObjectStreamDriverFactory;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.transports.socket.SelfContainedSocketStreamServer;
import org.codehaus.jremoting.test.TestInterface;
import org.codehaus.jremoting.test.TestInterface2;
import org.codehaus.jremoting.test.TestInterface3;
import org.codehaus.jremoting.test.TestInterfaceImpl;


/**
 * Test Object Stream over sockets.
 *
 * @author Paul Hammant
 */
public class ObjectStreamTestCase extends AbstractHelloTestCase {

    public ObjectStreamTestCase() {

        // See http://developer.java.sun.com/developer/bugParade/bugs/4499841.html
        // This bug prevents ObjectStream from functioning correctly when used
        // by JRemoting.  You can still use the ObjectStream transports, but
        // should be aware of the limitations.  See testBugParadeBugNumber4499841()
        // in the parent class.
        bugParadeBug4499841StillExists = true;

    }

    protected void setUp() throws Exception {

        // server side setup.
        server = new SelfContainedSocketStreamServer(new ConsoleServerMonitor(), 10002, SelfContainedSocketStreamServer.OBJECTSTREAM);

        testServer = new TestInterfaceImpl();

        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);

        server.start();

        // Client side setup
        factory = new ClientSideStubFactory(new SocketClientStreamInvocationHandler(new ConsoleClientMonitor(),
                new ClientObjectStreamDriverFactory(),
                "127.0.0.1", 10002));
        testClient = (TestInterface) factory.lookupService("Hello");


    }


    protected void tearDown() throws Exception {
        testClient = null;
        System.gc();
        Thread.sleep(300);
        factory.close();
        server.stop();
    }


}
