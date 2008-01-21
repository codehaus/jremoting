/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
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

package org.codehaus.jremoting.server.adapters;

import junit.framework.TestCase;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.requests.CloseConnection;
import org.codehaus.jremoting.requests.OpenConnection;
import org.codehaus.jremoting.responses.ConnectionClosed;
import org.codehaus.jremoting.responses.ConnectionOpened;
import org.codehaus.jremoting.responses.NoSuchSession;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.monitors.NullServerMonitor;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;

public class DefaultInvocationHandlerTestCase extends TestCase {

    public void testOpenConnection() {
        DefaultInvocationHandler invocationHandle = new DefaultInvocationHandler(new NullServerMonitor(), new RefusingStubRetriever(), new NullAuthenticator(), null);
        ConnectionOpened connectionOpened = (ConnectionOpened) invocationHandle.invoke(new OpenConnection(), "");
        assertNotNull(connectionOpened);
        assertNotNull(connectionOpened.getSessionID());
    }

    public void testCloseConnectionAfterOpenConnection() {
        DefaultInvocationHandler invocationHandle = new DefaultInvocationHandler(new NullServerMonitor(), new RefusingStubRetriever(), new NullAuthenticator(), null);
        ConnectionOpened connectionOpened = (ConnectionOpened) invocationHandle.invoke(new OpenConnection(), "");
        ConnectionClosed connectionClosed = (ConnectionClosed) invocationHandle.invoke(new CloseConnection(connectionOpened.getSessionID()), "");
        assertNotNull(connectionClosed);
        assertNotNull(connectionClosed.getSessionID());
        assertEquals(connectionClosed.getSessionID(), connectionOpened.getSessionID());

    }

    public void testCloseConnectionErrorsOnBogusSession() {
        DefaultInvocationHandler invocationHandle = new DefaultInvocationHandler(null, new RefusingStubRetriever(), new NullAuthenticator(), null);
        Response response = invocationHandle.invoke(new CloseConnection((long) 123), "");
        assertTrue(response instanceof NoSuchSession);
        NoSuchSession noSuchSession = (NoSuchSession) response;
        assertNotNull(noSuchSession);
        assertNotNull(noSuchSession.getSessionID());
        assertEquals(noSuchSession.getSessionID(), new Long(123));

    }


}
