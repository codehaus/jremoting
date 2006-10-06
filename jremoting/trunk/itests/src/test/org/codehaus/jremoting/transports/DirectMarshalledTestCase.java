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
import org.codehaus.jremoting.client.transports.direct.DirectClientInvoker;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.transports.direct.DirectMarshalledServer;
import org.codehaus.jremoting.test.TestInterface;
import org.codehaus.jremoting.test.TestInterface2;
import org.codehaus.jremoting.test.TestInterface3;
import org.codehaus.jremoting.test.TestInterfaceImpl;


/**
 * Test Direct Marshalled Transport
 *
 * @author Paul Hammant
 */
public class DirectMarshalledTestCase extends AbstractHelloTestCase {

    protected void setUp() throws Exception {

        // server side setup.
        server = new DirectMarshalledServer(new ConsoleServerMonitor());
        testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        factory = new ClientSideStubFactory(new DirectClientInvoker(new ConsoleClientMonitor(), (DirectMarshalledServer) server));

        testClient = (TestInterface) factory.lookupService("Hello");

    }

    public void testHello2Call() throws Exception {
        super.testHello2Call();
    }


    public void testBytes() throws Exception {
        super.testBytes(); 
    }

    protected void tearDown() throws Exception {
        testClient = null;
        System.gc();

        factory.close();

        server.stop();

    }


}
