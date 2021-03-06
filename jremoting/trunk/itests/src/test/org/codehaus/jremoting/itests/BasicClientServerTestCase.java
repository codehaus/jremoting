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

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionRefusedException;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.client.SocketDetails;
import org.codehaus.jremoting.client.pingers.TimingOutPinger;
import org.codehaus.jremoting.client.streams.ByteStream;
import org.codehaus.jremoting.client.streams.ObjectStream;
import org.codehaus.jremoting.client.resolver.ServiceResolver;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.monitors.NullClientMonitor;
import org.codehaus.jremoting.client.transports.RmiTransport;
import org.codehaus.jremoting.client.transports.SocketTransport;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.responses.NoSuchSession;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.Session;
import org.codehaus.jremoting.server.transports.SocketServer;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;


/**
 * Test basic client server features.
 *
 * @author Paul Hammant
 */
public class BasicClientServerTestCase extends MockObjectTestCase {

    private Mock mockServerMonitor;
    private Mock mockClientMonitor;

    protected void setUp() throws Exception {
        mockServerMonitor = mock(ServerMonitor.class);
        mockClientMonitor = mock(ClientMonitor.class);

        super.setUp(); 
    }

    public void testNoServer() throws Exception {
        try {
            new ServiceResolver(new SocketTransport(new ConsoleClientMonitor(),
                new ByteStream(), new SocketDetails("127.0.0.1", 12345)));
            fail("Should have have failed.");
        } catch (ConnectionRefusedException e) {
            // what we expetcted
        }
    }

    public void testNotPublishedExceptionThrownWhenNeeded() throws Exception {
        mockServerMonitor.expects(once()).method("newSession").withAnyArguments();
        // server side setup.
        SocketServer server = new SocketServer((ServerMonitor) mockServerMonitor.proxy(), new InetSocketAddress(12333));

        TestFacadeImpl testServer = new TestFacadeImpl();
        Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestFacade3.class, TestFacade2.class);
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        try {

            ServiceResolver sr = new ServiceResolver(new SocketTransport(new ConsoleClientMonitor(),
                new ByteStream(), new SocketDetails("127.0.0.1", 12333)));
            sr.resolveService("serverDelegate");

            fail("should have barfed");
        } catch (ConnectionException e) {
            assertTrue(e.getMessage().contains("Service 'serverDelegate' not published"));
        } finally {
            server.stop();
        }
    }


    public void testNoReferenceExceptionThrownWhenNeeded() throws Exception {

        mockServerMonitor.expects(once()).method("newSession").withAnyArguments();

        // server side setup.
        SocketServer server = new SocketServer((ServerMonitor) mockServerMonitor.proxy(), new org.codehaus.jremoting.server.streams.ObjectStream(), new InetSocketAddress(12331)
        );

        TestFacadeImpl testServer = new TestFacadeImpl();
        Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestFacade3.class, TestFacade2.class);
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup

        SocketTransport invoker = new SocketTransport(new ConsoleClientMonitor(),
                new ObjectStream(), new SocketDetails("127.0.0.1", 12331));
        ServiceResolver sr = new ServiceResolver(invoker);
        sr.resolveService("Hello");
        Object result = invoker.invoke(new InvokeMethod("Hello", "Main", "ping()", new Object[0], (long) 44332, (long) 21), true);

        assertTrue(result instanceof NoSuchSession);

        server.stop();

    }

    public void testMismatch2() throws Exception {

        // server side setup.
        // Object
        SocketServer server = new SocketServer((ServerMonitor) mockServerMonitor.proxy(), new org.codehaus.jremoting.server.streams.ObjectStream(), new InetSocketAddress(12347));
        TestFacadeImpl testServer = new TestFacadeImpl();
        Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestFacade3.class, TestFacade2.class);
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        try {
            new ServiceResolver(new SocketTransport(new NullClientMonitor(),
                new ByteStream(), new SocketDetails("127.0.0.1", 12347)){
                protected int getSocketTimeOut() {
                    return 500;
                }
            });
            fail("Expected mismatch exception");
        } catch (InvocationException e) {
            assertTrue(e.getCause() instanceof SocketTimeoutException);
            SocketTimeoutException ste = (SocketTimeoutException) e.getCause();
            assertEquals(0, ste.bytesTransferred);
        } finally {
            server.stop();
        }
    }

    public void testObjectStreamServerCanRecogniseNonObjectStreamTraffic() throws Exception {

        // server side setup.
        // JMock 1.2.0 not working in thos scenario for some reason.
        final boolean[] didIt = new boolean[] {false};
        final boolean[] mismatch = new boolean[] {false};
        final boolean[] wrong = new boolean[] {false};
        ServerMonitor sm = new ServerMonitor() {
            public void closeError(Class clazz, String s, IOException e) {
                wrong[0] = true;
            }

            public void classNotFound(Class clazz, ClassNotFoundException e) {
                wrong[0] = true;
            }

            public void unexpectedException(Class clazz, String s, Throwable e) {
                mismatch[0] = s.contains("mismatch");
                didIt[0] = true;
            }

            public void stopServerError(Class clazz, String s, Exception e) {
                wrong[0] = true;
            }

            public void newSession(Session session, int size, String connectionDetails) {
            }

            public void removeSession(Session session, int newSize) {
            }

            public void staleSession(Session session, int newSize) {
            }
        };
        SocketServer server = new SocketServer(sm, new org.codehaus.jremoting.server.streams.ObjectStream(), new InetSocketAddress(12347));
        server.setSocketTimeout(10);
        TestFacadeImpl testServer = new TestFacadeImpl();
        Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestFacade3.class, TestFacade2.class);
        server.publish(testServer, "Hello", pd);
        server.start();

        mockClientMonitor.expects(atLeastOnce()).method("methodLogging").will(returnValue(true));
        mockClientMonitor.expects(atLeastOnce()).method("unexpectedIOException").with(isA(Class.class), isA(String.class), isA(SocketTimeoutException.class));


        // Client side setup
        try {
            new ServiceResolver(new SocketTransport((ClientMonitor) mockClientMonitor.proxy(),
                new ByteStream(), new SocketDetails("127.0.0.1", 12347)){
                protected int getSocketTimeOut() {
                    return 100;    
                }
            });
            fail("Expected mismatch exception");
        } catch (InvocationException e) {
            assertTrue(e.getCause() instanceof SocketTimeoutException);
            SocketTimeoutException ste = (SocketTimeoutException) e.getCause();
            assertEquals(0, ste.bytesTransferred);
        } finally {
            server.stop();
            Thread.sleep(100);
        }

        assertTrue(didIt[0]);
        assertTrue(mismatch[0]);
        assertFalse(wrong[0]);

    }

    public void testMismatch3() throws Exception {

        // server side setup.
        SocketServer server = new SocketServer((ServerMonitor) mockServerMonitor.proxy(), new InetSocketAddress(12348));
        TestFacadeImpl testServer = new TestFacadeImpl();
        Publication pd = new Publication(TestFacade.class).addAdditionalFacades(TestFacade3.class, TestFacade2.class);
        server.publish(testServer, "Hello", pd);
        server.start();
        RecordingClientMonitor rcm = new RecordingClientMonitor();

        // Client side setup
        ServiceResolver sr;
        try {
            TimingOutPinger pinger = new TimingOutPinger(1, 2);
            RmiTransport transport = new RmiTransport(rcm, pinger,
                    new SocketDetails("127.0.0.1", 12348));
            fail("Expected mismatch exception");
        } catch (ConnectionException e) {
            assertTrue(e.getMessage().indexOf("mismatch") > 0);
        }
        server.stop();
        assertEquals("methodLogging\n", rcm.sb.toString());

    }
}
