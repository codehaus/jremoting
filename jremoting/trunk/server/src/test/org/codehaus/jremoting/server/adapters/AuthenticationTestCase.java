package org.codehaus.jremoting.server.adapters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.authentications.Authentication;
import org.codehaus.jremoting.authentications.NamePasswordAuthentication;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.requests.LookupService;
import org.codehaus.jremoting.requests.OpenConnection;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.responses.AuthenticationFailed;
import org.codehaus.jremoting.responses.ConnectionOpened;
import org.codehaus.jremoting.responses.Service;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.authenticators.SinglePasswordAuthenticator;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.transports.ConnectingServer;
import org.codehaus.jremoting.server.transports.DefaultServerSideContextFactory;
import org.jmock.MockObjectTestCase;

public class AuthenticationTestCase extends MockObjectTestCase {

    private InvocationHandlerDelegate iha;
    private ConnectingServer server;
    HashMap impl = new HashMap();

    private void makeServer() {
        server = new ConnectingServer(new ConsoleServerMonitor(), iha, Executors.newScheduledThreadPool(10)){};
    }

    public void testNullAuthenticationAuthorizes() throws PublicationException, IOException, ClassNotFoundException {
        iha = new InvocationHandlerDelegate(new ConsoleServerMonitor(), new RefusingStubRetriever(), new NullAuthenticator(), new DefaultServerSideContextFactory());
        makeServer();
        server.publish(impl, "foo", Map.class);
        Response resp = serDeSerResponse(putTestEntry(null));
        assertTrue(resp instanceof Service);
    }

    public void testNullAuthenticationBlocksIfMismatchedAuthentication() throws PublicationException, IOException, ClassNotFoundException {
        iha = new InvocationHandlerDelegate(new ConsoleServerMonitor(), new RefusingStubRetriever(), new NullAuthenticator(), new DefaultServerSideContextFactory());
        makeServer();
        server.publish(impl, "foo", Map.class);
        Response resp = serDeSerResponse(putTestEntry(new NamePasswordAuthentication("", "")));
        assertTrue(resp instanceof AuthenticationFailed);
    }

    public void testWorkingPasswordAuthorizes() throws PublicationException, IOException, ClassNotFoundException {
        iha = new InvocationHandlerDelegate(new ConsoleServerMonitor(), new RefusingStubRetriever(), new SinglePasswordAuthenticator("fred"), new DefaultServerSideContextFactory());
        makeServer();
        server.publish(impl, "foo", Map.class);
        Response resp = serDeSerResponse(putTestEntry(new NamePasswordAuthentication("FRED", "fred")));
        assertTrue(resp instanceof Service);
    }

    public void testBogusPasswordBlocks() throws PublicationException, IOException, ClassNotFoundException {
        iha = new InvocationHandlerDelegate(new ConsoleServerMonitor(), new RefusingStubRetriever(), new SinglePasswordAuthenticator("fred"), new DefaultServerSideContextFactory());
        makeServer();
        server.publish(impl, "foo", Map.class);
        Response resp = serDeSerResponse(putTestEntry(new NamePasswordAuthentication("FRED", "wilma")));
        assertTrue(resp instanceof AuthenticationFailed);
    }

    private Response putTestEntry(Authentication auth)  {
        ConnectionOpened co = (ConnectionOpened) iha.handleInvocation(new OpenConnection(), new Object());
        return iha.handleInvocation(new LookupService("foo", auth, co.getSessionID()), new Object());
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




}