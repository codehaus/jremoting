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
package org.codehaus.jremoting.itests;

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.resolver.ServiceResolver;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.transports.StatefulServer;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * Extended by classes that name the transport.
 *
 * @author Paul Hammant
 */
public abstract class AbstractJRemotingTestCase extends MockObjectTestCase {

    protected StatefulServer server;
    protected TestFacadeImpl testServer;
    protected TestFacade testClient;
    protected ServiceResolver jremotingClient;
    protected boolean bugParadeBug4499841StillExists = true;
    protected Mock mockClientMonitor;
    protected Mock mockServerMonitor;

    protected void setUp() throws Exception {
        mockClientMonitor = mock(ClientMonitor.class);
        mockServerMonitor = mock(ServerMonitor.class);
        mockServerMonitor.expects(once()).method("newSession").withAnyArguments();
    }

    protected void tearDown() throws Exception {
        mockServerMonitor.expects(once()).method("removeSession").withAnyArguments();
    }

    public void testHelloCall() throws Exception {
        // lookup worked ?
        assertNotNull(testClient);

        // Invoke a method over rpc.
        testClient.hello("Hello!?");

        // test the server has logged the message.
        assertEquals("Hello!?", ((TestFacadeImpl) testServer).getStoredState("void:hello(String)"));
    }


}
