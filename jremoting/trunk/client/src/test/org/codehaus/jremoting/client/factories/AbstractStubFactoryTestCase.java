package org.codehaus.jremoting.client.factories;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.ClientInvocationHandler;
import org.codehaus.jremoting.client.HostContext;
import org.codehaus.jremoting.requests.CloseConnection;
import org.codehaus.jremoting.requests.ListServices;
import org.codehaus.jremoting.requests.LookupService;
import org.codehaus.jremoting.requests.OpenConnection;
import org.codehaus.jremoting.responses.ConnectionClosed;
import org.codehaus.jremoting.responses.ConnectionOpened;
import org.codehaus.jremoting.responses.ExceptionThrown;
import org.codehaus.jremoting.responses.NotPublished;
import org.codehaus.jremoting.responses.Service;
import org.codehaus.jremoting.responses.ServicesList;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class AbstractStubFactoryTestCase extends MockObjectTestCase {

    public void testOpenCloseSequence() throws ConnectionException {

        Mock hc = mock(HostContext.class);
        Mock ih = mock(ClientInvocationHandler.class);
        hc.expects(once()).method("getInvocationHandler").withNoArguments().will(returnValue(ih.proxy()));
        ih.expects(once()).method("initialize");
        ih.expects(once()).method("handleInvocation").with(isA(OpenConnection.class)).will(returnValue(new ConnectionOpened("", (long) 123)));
        ih.expects(once()).method("handleInvocation").with(isA(LookupService.class)).will(returnValue(new Service((long) 321)));
        ih.expects(once()).method("handleInvocation").with(isA(CloseConnection.class)).will(returnValue(new ConnectionClosed((long) 321)));
        ih.expects(once()).method("close");

        AbstractStubFactory factory = new AbstractStubFactory((HostContext) hc.proxy()) {
            protected Class getStubClass(String publishedServiceName, String objectName) throws ConnectionException, ClassNotFoundException {
                return null;
            }

            protected Object getInstance(String publishedServiceName, String objectName, DefaultProxyHelper proxyHelper) throws ConnectionException {
                return "bar";
            }
        };

        Object bar = factory.lookupService("foo");
        factory.close();
        assertNotNull(bar);
        assertEquals("bar", bar);


    }

    public void testNotPublishedResponseToLookup() throws ConnectionException {

        Mock hc = mock(HostContext.class);
        Mock ih = mock(ClientInvocationHandler.class);
        hc.expects(once()).method("getInvocationHandler").withNoArguments().will(returnValue(ih.proxy()));
        ih.expects(once()).method("initialize");
        ih.expects(once()).method("handleInvocation").with(isA(OpenConnection.class)).will(returnValue(new ConnectionOpened("", (long) 123)));
        ih.expects(once()).method("handleInvocation").with(isA(LookupService.class)).will(returnValue(new NotPublished()));

        AbstractStubFactory factory = new AbstractStubFactory((HostContext) hc.proxy()) {
            protected Class getStubClass(String publishedServiceName, String objectName) throws ConnectionException, ClassNotFoundException {
                return null;
            }
        };

        try {
            factory.lookupService("foo");
        } catch (ConnectionException e) {
            assertEquals("Service 'foo' not published", e.getMessage());
        }


    }

    public void testConnectionExceptionThrownResponseToLookup() throws ConnectionException {

        Mock hc = mock(HostContext.class);
        Mock ih = mock(ClientInvocationHandler.class);
        hc.expects(once()).method("getInvocationHandler").withNoArguments().will(returnValue(ih.proxy()));
        ih.expects(once()).method("initialize");
        ih.expects(once()).method("handleInvocation").with(isA(OpenConnection.class)).will(returnValue(new ConnectionOpened("", (long) 123)));
        ih.expects(once()).method("handleInvocation").with(isA(LookupService.class)).will(returnValue(new ExceptionThrown(new ConnectionException("foo"))));

        AbstractStubFactory factory = new AbstractStubFactory((HostContext) hc.proxy()) {
            protected Class getStubClass(String publishedServiceName, String objectName) throws ConnectionException, ClassNotFoundException {
                return null;
            }
        };

        try {
            factory.lookupService("foo");
        } catch (ConnectionException e) {
            assertEquals("foo", e.getMessage());
        }
    }

    public void testRuntimeExceptionThrownResponseToLookup() throws ConnectionException {

        Mock hc = mock(HostContext.class);
        Mock ih = mock(ClientInvocationHandler.class);
        hc.expects(once()).method("getInvocationHandler").withNoArguments().will(returnValue(ih.proxy()));
        ih.expects(once()).method("initialize");
        ih.expects(once()).method("handleInvocation").with(isA(OpenConnection.class)).will(returnValue(new ConnectionOpened("", (long) 123)));
        ih.expects(once()).method("handleInvocation").with(isA(LookupService.class)).will(returnValue(new ExceptionThrown(new RuntimeException("foo"))));

        AbstractStubFactory factory = new AbstractStubFactory((HostContext) hc.proxy()) {
            protected Class getStubClass(String publishedServiceName, String objectName) throws ConnectionException, ClassNotFoundException {
                return null;
            }
        };

        try {
            factory.lookupService("foo");
        } catch (RuntimeException e) {
            assertEquals("foo", e.getMessage());
        }
    }

    public void testErrorThrownResponseToLookup() throws ConnectionException {

        Mock hc = mock(HostContext.class);
        Mock ih = mock(ClientInvocationHandler.class);
        hc.expects(once()).method("getInvocationHandler").withNoArguments().will(returnValue(ih.proxy()));
        ih.expects(once()).method("initialize");
        ih.expects(once()).method("handleInvocation").with(isA(OpenConnection.class)).will(returnValue(new ConnectionOpened("", (long) 123)));
        ih.expects(once()).method("handleInvocation").with(isA(LookupService.class)).will(returnValue(new ExceptionThrown(new Error("foo"))));

        AbstractStubFactory factory = new AbstractStubFactory((HostContext) hc.proxy()) {
            protected Class getStubClass(String publishedServiceName, String objectName) {
                return null;
            }
        };

        try {
            factory.lookupService("foo");
        } catch (Error e) {
            assertEquals("foo", e.getMessage());
        }
    }

    public void testLookupUpOfServices() throws ConnectionException {

        Mock hc = mock(HostContext.class);
        Mock ih = mock(ClientInvocationHandler.class);
        hc.expects(once()).method("getInvocationHandler").withNoArguments().will(returnValue(ih.proxy()));
        ih.expects(once()).method("initialize");
        ih.expects(once()).method("handleInvocation").with(isA(OpenConnection.class)).will(returnValue(new ConnectionOpened("", (long) 123)));
        ih.expects(once()).method("handleInvocation").with(isA(ListServices.class)).will(returnValue(new ServicesList(new String[] {"1", "2"})));

        AbstractStubFactory factory = new AbstractStubFactory((HostContext) hc.proxy()) {
            protected Class getStubClass(String publishedServiceName, String objectName) {
                return null;
            }
        };

        String[] services = (String[]) factory.listServices();
        assertEquals(2, services.length);
        assertEquals("1", services[0]);
        assertEquals("2", services[1]);
    }



}
