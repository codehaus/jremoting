package org.codehaus.jremoting.server.adapters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.authentication.Authentication;
import org.codehaus.jremoting.authentication.NameAndPasswordAuthentication;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.requests.LookupService;
import org.codehaus.jremoting.requests.OpenConnection;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.responses.AuthenticationFailed;
import org.codehaus.jremoting.responses.ConnectionOpened;
import org.codehaus.jremoting.responses.Service;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.authenticators.NameAndPasswordAuthenticator;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.transports.ConnectingServer;
import org.jmock.MockObjectTestCase;

public class AuthenticationTestCase extends MockObjectTestCase {

    private DefaultInvocationHandler invocationHandler;
    private ConnectingServer server;
    HashMap impl = new HashMap();

    private void makeServer() {
        server = new ConnectingServer(new ConsoleServerMonitor(), invocationHandler, Executors.newScheduledThreadPool(10)){};
    }

    public void testNullAuthenticationAuthorizes() throws PublicationException, IOException, ClassNotFoundException {
        invocationHandler = new DefaultInvocationHandler(new ConsoleServerMonitor(), new RefusingStubRetriever(), new NullAuthenticator(), new ThreadLocalServerContextFactory());
        makeServer();
        server.publish(impl, "foo", Map.class);
        Response resp = serDeSerResponse(putTestEntry(null));
        assertTrue(resp instanceof Service);
    }

    public void testNullAuthenticationBlocksIfMismatchedAuthentication() throws PublicationException, IOException, ClassNotFoundException {
        invocationHandler = new DefaultInvocationHandler(new ConsoleServerMonitor(), new RefusingStubRetriever(), new NullAuthenticator(), new ThreadLocalServerContextFactory());
        makeServer();
        server.publish(impl, "foo", Map.class);
        Response resp = serDeSerResponse(putTestEntry(new NameAndPasswordAuthentication("", "")));
        assertTrue(resp instanceof AuthenticationFailed);
    }

    public void testWorkingPasswordAuthorizes() throws PublicationException, IOException, ClassNotFoundException {
        invocationHandler = new DefaultInvocationHandler(new ConsoleServerMonitor(), new RefusingStubRetriever(), new NameAndPasswordAuthenticator("fred", "wilma"), new ThreadLocalServerContextFactory());
        makeServer();
        server.publish(impl, "foo", Map.class);
        Response resp = serDeSerResponse(putTestEntry(new NameAndPasswordAuthentication("fred", "wilma")));
        assertTrue(resp instanceof Service);
    }

    public void testBogusPasswordBlocks() throws PublicationException, IOException, ClassNotFoundException {
        invocationHandler = new DefaultInvocationHandler(new ConsoleServerMonitor(), new RefusingStubRetriever(), new NameAndPasswordAuthenticator("fred", "wilma"), new ThreadLocalServerContextFactory());
        makeServer();
        server.publish(impl, "foo", Map.class);
        Response resp = serDeSerResponse(putTestEntry(new NameAndPasswordAuthentication("FRED", "wilma")));
        assertTrue(resp instanceof AuthenticationFailed);
    }

    private Response putTestEntry(Authentication auth)  {
        ConnectionOpened co = (ConnectionOpened) invocationHandler.invoke(new OpenConnection(), "");
        return invocationHandler.invoke(new LookupService("foo", auth, co.getSessionID()), "");
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