package org.codehaus.jremoting.server.adapters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.requests.ListServices;
import org.codehaus.jremoting.requests.OpenConnection;
import org.codehaus.jremoting.requests.RetrieveStub;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.responses.ConnectionOpened;
import org.codehaus.jremoting.responses.NotPublished;
import org.codehaus.jremoting.responses.RequestFailed;
import org.codehaus.jremoting.responses.ServicesList;
import org.codehaus.jremoting.responses.ServicesSuspended;
import org.codehaus.jremoting.responses.StubRetrievalFailed;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.adapters.DefaultServerDelegate;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.transports.ConnectingServer;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.jmock.MockObjectTestCase;

public class AbstractServerTestCase extends MockObjectTestCase {

    private DefaultServerDelegate serverDelegate;
    private ConnectingServer server;
    HashMap impl = new HashMap();

    protected void setUp() throws Exception {

        serverDelegate = new DefaultServerDelegate(new ConsoleServerMonitor(), new RefusingStubRetriever(), new NullAuthenticator(), new ThreadLocalServerContextFactory());
        server = new ConnectingServer(new ConsoleServerMonitor(), serverDelegate, Executors.newScheduledThreadPool(10)) {
        };

    }

    public void testPublishAndUnpublish() throws PublicationException, IOException, ClassNotFoundException {
        server.publish(impl, "serverDelegate", Map.class);
        putTestEntry();
        server.unPublish(impl, "serverDelegate");
        Response resp = serDeSerResponse(putTestEntry());
        assertTrue(resp instanceof NotPublished);
    }

    public void testPublishAndUnpublishDoes() throws PublicationException {
        server.publish(impl, "serverDelegate", Map.class);
        server.unPublish(impl, "serverDelegate");
        try {
            server.unPublish(impl, "serverDelegate");
            fail("should have barfed");
        } catch (PublicationException e) {
        }
    }

    public void testCantUnpublishSomethingThatWasNeverPublished() throws PublicationException {
        try {
            server.unPublish(impl, "serverDelegate");
            fail("should have barfed");
        } catch (PublicationException e) {
        }
    }

    public void testCantPublishServiceTwice() throws PublicationException {
        server.publish(impl, "serverDelegate", Map.class);
        try {
            server.publish(impl, "serverDelegate", Map.class);
            fail("should have barfed");
        } catch (PublicationException e) {
            assertEquals("Service 'serverDelegate' already published",e.getMessage());
        }
    }

    public void testPublishAndRePublish() throws PublicationException, IOException, ClassNotFoundException {
        HashMap impl2 = new HashMap();
        server.publish(impl, "serverDelegate", Map.class);
        server.replacePublished(impl, "serverDelegate", impl2);
        putTestEntry();
        assertEquals("2", impl2.get("1"));
        assertNull(impl.get("1"));
    }

    private Response putTestEntry() throws IOException, ClassNotFoundException {
        ConnectionOpened co = (ConnectionOpened) serverDelegate.invoke(new OpenConnection(), "");
        Request request = new InvokeMethod("serverDelegate", "Main", "put(java.lang.Object, java.lang.Object)", new Object[]{"1", "2"}, (long) 0, co.getSessionID());
        return serverDelegate.invoke(serDeSerRequest(request), "");
    }

    private Request serDeSerRequest(Request request) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(request);
        oos.flush();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        return (Request) ois.readObject();
    }

    private Response serDeSerResponse(Response response) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(response);
        oos.flush();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        return (Response) ois.readObject();
    }

    public void testPublishAndSuspendBlocksOperations() throws PublicationException, IOException, ClassNotFoundException {
        server.publish(impl, "serverDelegate", Map.class);
        server.suspend();
        assertTrue(serDeSerResponse(serverDelegate.invoke(serDeSerRequest(new OpenConnection()), "")) instanceof ServicesSuspended);
    }

    public void testPublishAndSuspendAndResumeDoesNotBlockOperation() throws PublicationException, IOException, ClassNotFoundException {
        server.publish(impl, "serverDelegate", Map.class);
        server.suspend();
        server.resume();
        assertTrue(serDeSerResponse(serverDelegate.invoke(serDeSerRequest(new OpenConnection()), "")) instanceof ConnectionOpened);
    }

    public void testStubRetrievalFailsWhenItsAppropriate() throws PublicationException, IOException, ClassNotFoundException {
        server.publish(impl, "serverDelegate", Map.class);
        Response response = serDeSerResponse(serverDelegate.invoke(serDeSerRequest(new RetrieveStub("serverDelegate", "Main")), ""));
        assertTrue(response instanceof StubRetrievalFailed);
    }

    public void testRequestFailsOnUnknownRequestType() throws PublicationException, IOException, ClassNotFoundException {
        server.publish(impl, "serverDelegate", Map.class);
        Response response = serDeSerResponse(serverDelegate.invoke(serDeSerRequest(new MyRequest()), ""));
        assertTrue(response instanceof RequestFailed);
        assertEquals("Unknown Request Type: org.codehaus.jremoting.server.adapters.AbstractServerTestCase$MyRequest", ((RequestFailed) response).getFailureReason());
    }

    public void testListServicesRespondsAppropriately() throws PublicationException, IOException, ClassNotFoundException {
        server.publish(impl, "serverDelegate", Map.class);
        Response response = serverDelegate.invoke(serDeSerRequest(new ListServices()), "");
        assertTrue(serDeSerResponse(response) instanceof ServicesList);
        assertEquals(1, ((ServicesList ) response).getServices().length);
        assertEquals("serverDelegate", ((ServicesList ) response).getServices()[0]);
    }

    public void testListServicesRespondsAppropriatelyWhenThereAreNone() throws PublicationException, IOException, ClassNotFoundException {
        Response response = serverDelegate.invoke(serDeSerRequest(new ListServices()), "");
        assertTrue(serDeSerResponse(response) instanceof ServicesList);
        assertEquals(0, ((ServicesList ) response).getServices().length);
    }

    private static class MyRequest extends Request {
        private static final long serialVersionUID = -8376074179235205591L;

        public MyRequest() {
        }

        public int getRequestCode() {
            return 999;
        }
    }
}
