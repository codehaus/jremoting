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

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.factories.StubsOnClient;
import org.codehaus.jremoting.client.pingers.NeverConnectionPinger;
import org.codehaus.jremoting.client.transports.direct.DirectMarshalledTransport;
import org.codehaus.jremoting.itests.TestInterface;
import org.codehaus.jremoting.itests.TestInterface2;
import org.codehaus.jremoting.itests.TestInterface3;
import org.codehaus.jremoting.itests.TestInterfaceImpl;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.transports.direct.DirectMarshalledServer;

import java.util.concurrent.Executors;


/**
 * Test Direct Marshalled Transport
 *
 * @author Paul Hammant
 */
public class DirectMarshalledTestCase extends AbstractHelloTestCase {

    protected void setUp() throws Exception {
        super.setUp();        
        // server side setup.
        server = new DirectMarshalledServer((ServerMonitor) mockServerMonitor.proxy());
        testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        mockClientMonitor.expects(atLeastOnce()).method("methodLogging").will(returnValue(false));
        factory = new StubsOnClient(new DirectMarshalledTransport((ClientMonitor) mockClientMonitor.proxy(), Executors.newScheduledThreadPool(10), new NeverConnectionPinger(), (DirectMarshalledServer) server, this.getClass().getClassLoader()));

        testClient = (TestInterface) factory.lookupService("Hello");

    }

    protected void tearDown() throws Exception {
        testClient = null;
        System.gc();

        factory.close();

        server.stop();

    }


}
