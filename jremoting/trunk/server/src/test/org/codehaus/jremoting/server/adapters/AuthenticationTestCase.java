package org.codehaus.jremoting.server.adapters;

import org.jmock.MockObjectTestCase;
import org.codehaus.jremoting.server.transports.AbstractServer;
import org.codehaus.jremoting.server.transports.DefaultServerSideClientContextFactory;
import org.codehaus.jremoting.server.classretrievers.NoStubRetriever;
import org.codehaus.jremoting.server.authenticators.SinglePasswordAuthenticator;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.responses.*;
import org.codehaus.jremoting.requests.*;
import org.codehaus.jremoting.api.Authentication;
import org.codehaus.jremoting.api.NamePasswordAuthentication;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.io.*;

public class AuthenticationTestCase extends MockObjectTestCase {

    private InvocationHandlerAdapter iha;
    private AbstractServer server;
    HashMap impl = new HashMap();

    private void makeServer() {
        server = new AbstractServer(iha, new ConsoleServerMonitor(), Executors.newCachedThreadPool()) {
            public void start() {
            }

            public void stop() {
            }
        };
    }

    public void testNullAuthenticationAuthorizes() throws PublicationException, IOException, ClassNotFoundException {
        iha = new InvocationHandlerAdapter(new NoStubRetriever(), new NullAuthenticator(), new ConsoleServerMonitor(), new DefaultServerSideClientContextFactory());
        makeServer();
        server.publish(impl, "foo", Map.class);
        AbstractResponse resp = serDeSerResponse(putTestEntry(null));
        assertTrue(resp instanceof Service);
    }

    public void testNullAuthenticationBlocksIfMismatchedAuthentication() throws PublicationException, IOException, ClassNotFoundException {
        iha = new InvocationHandlerAdapter(new NoStubRetriever(), new NullAuthenticator(), new ConsoleServerMonitor(), new DefaultServerSideClientContextFactory());
        makeServer();
        server.publish(impl, "foo", Map.class);
        AbstractResponse resp = serDeSerResponse(putTestEntry(new NamePasswordAuthentication("", "")));
        assertTrue(resp instanceof AuthenticationFailed);
    }

    public void testWorkingPasswordAuthorizes() throws PublicationException, IOException, ClassNotFoundException {
        iha = new InvocationHandlerAdapter(new NoStubRetriever(), new SinglePasswordAuthenticator("fred"), new ConsoleServerMonitor(), new DefaultServerSideClientContextFactory());
        makeServer();
        server.publish(impl, "foo", Map.class);
        AbstractResponse resp = serDeSerResponse(putTestEntry(new NamePasswordAuthentication("FRED", "fred")));
        assertTrue(resp instanceof Service);
    }

    public void testBogusPasswordBlocks() throws PublicationException, IOException, ClassNotFoundException {
        iha = new InvocationHandlerAdapter(new NoStubRetriever(), new SinglePasswordAuthenticator("fred"), new ConsoleServerMonitor(), new DefaultServerSideClientContextFactory());
        makeServer();
        server.publish(impl, "foo", Map.class);
        AbstractResponse resp = serDeSerResponse(putTestEntry(new NamePasswordAuthentication("FRED", "wilma")));
        assertTrue(resp instanceof AuthenticationFailed);
    }

    private AbstractResponse putTestEntry(Authentication auth)  {
        ConnectionOpened co = (ConnectionOpened) iha.handleInvocation(new OpenConnection(), new Object());
        return iha.handleInvocation(new LookupService("foo", auth, co.getSessionID()), new Object());
    }

    private AbstractRequest serDeSerRequest(AbstractRequest request) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(request);
        oos.flush();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        return (AbstractRequest) ois.readObject();
    }

    private AbstractResponse serDeSerResponse(AbstractResponse response) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(response);
        oos.flush();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        return (AbstractResponse) ois.readObject();
    }




}