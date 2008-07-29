package org.codehaus.jremoting.client.resolver;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.ContextFactory;
import org.codehaus.jremoting.client.StubHelper;
import org.codehaus.jremoting.client.Transport;
import org.codehaus.jremoting.requests.ListServices;
import org.codehaus.jremoting.requests.LookupService;
import org.codehaus.jremoting.responses.ConnectionOpened;
import org.codehaus.jremoting.responses.ExceptionThrown;
import org.codehaus.jremoting.responses.NotPublished;
import org.codehaus.jremoting.responses.Service;
import org.codehaus.jremoting.responses.ServicesList;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.util.Map;

public class XAbstractFactoryTestCase extends MockObjectTestCase {

    public void testOpenCloseSequence() throws ConnectionException {

        Mock ih = mock(Transport.class);
        ih.expects(once()).method("openConnection").withNoArguments().will(returnValue(new ConnectionOpened(null, (long) 123)));
        ih.expects(once()).method("invoke").with(isA(LookupService.class), eq(true)).will(returnValue(new Service((long) 321, Map.class.getName(), new String[0])));
        ih.expects(once()).method("closeConnection").with(eq(123L));

        Mock cf = mock(ContextFactory.class);

        ServiceResolver factory = new ServiceResolver((Transport) ih.proxy(), (ContextFactory) cf.proxy()) {
            @Override
            protected Object getInstance(String facadeClassName, String publishedServiceName, String objectName, StubHelper stubHelper) throws ConnectionException {
                return "bar";
            }
        };

        Object bar = factory.resolveService("foo");
        factory.close();
        assertNotNull(bar);
        assertEquals("bar", bar);


    }

    public void testNotPublishedResponseToLookup() throws ConnectionException {

        Mock ih = mock(Transport.class);
        ih.expects(once()).method("openConnection").withNoArguments().will(returnValue(new ConnectionOpened(null, (long) 123)));
        ih.expects(once()).method("invoke").with(isA(LookupService.class), eq(true)).will(returnValue(new NotPublished()));

        Mock cf = mock(ContextFactory.class);

        ServiceResolver factory = new ServiceResolver((Transport) ih.proxy(), (ContextFactory) cf.proxy()) {
            protected Class getStubClass(String publishedServiceName, String objectName) throws ConnectionException, ClassNotFoundException {
                return null;
            }
        };

        try {
            factory.resolveService("foo");
        } catch (ConnectionException e) {
            assertEquals("Service 'foo' not published", e.getMessage());
        }


    }

    public void testConnectionExceptionThrownResponseToLookup() throws ConnectionException {

        Mock ih = mock(Transport.class);
        ih.expects(once()).method("openConnection").withNoArguments().will(returnValue(new ConnectionOpened(null, (long) 123)));
        ih.expects(once()).method("invoke").with(isA(LookupService.class), eq(true)).will(returnValue(new ExceptionThrown(new ConnectionException("foo"))));

        Mock cf = mock(ContextFactory.class);

        ServiceResolver factory = new ServiceResolver((Transport) ih.proxy(), (ContextFactory) cf.proxy()) {
            protected Class getStubClass(String publishedServiceName, String objectName) throws ConnectionException, ClassNotFoundException {
                return null;
            }
        };

        try {
            factory.resolveService("foo");
        } catch (ConnectionException e) {
            assertEquals("foo", e.getMessage());
        }
    }

    public void testRuntimeExceptionThrownResponseToLookup() throws ConnectionException {

        Mock ih = mock(Transport.class);
        ih.expects(once()).method("openConnection").withNoArguments().will(returnValue(new ConnectionOpened(null, (long) 123)));
        ih.expects(once()).method("invoke").with(isA(LookupService.class), eq(true)).will(returnValue(new ExceptionThrown(new RuntimeException("foo"))));

        Mock cf = mock(ContextFactory.class);

        ServiceResolver factory = new ServiceResolver((Transport) ih.proxy(), (ContextFactory) cf.proxy()) {
            protected Class getStubClass(String publishedServiceName, String objectName) throws ConnectionException, ClassNotFoundException {
                return null;
            }
        };

        try {
            factory.resolveService("foo");
        } catch (RuntimeException e) {
            assertEquals("foo", e.getMessage());
        }
    }

    public void testErrorThrownResponseToLookup() throws ConnectionException {

        Mock ih = mock(Transport.class);
        ih.expects(once()).method("openConnection").withNoArguments().will(returnValue(new ConnectionOpened(null, (long) 123)));
        ih.expects(once()).method("invoke").with(isA(LookupService.class), eq(true)).will(returnValue(new ExceptionThrown(new Error("foo"))));

        Mock cf = mock(ContextFactory.class);

        ServiceResolver factory = new ServiceResolver((Transport) ih.proxy(), (ContextFactory) cf.proxy()) {
            protected Class getStubClass(String publishedServiceName, String objectName) {
                return null;
            }
        };

        try {
            factory.resolveService("foo");
        } catch (Error e) {
            assertEquals("foo", e.getMessage());
        }
    }

    public void testLookupUpOfServices() throws ConnectionException {

        Mock ih = mock(Transport.class);
        ih.expects(once()).method("openConnection").withNoArguments().will(returnValue(new ConnectionOpened(null, (long) 123)));
        ih.expects(once()).method("invoke").with(isA(ListServices.class), eq(true)).will(returnValue(new ServicesList(new String[] {"1", "2"})));

        Mock cf = mock(ContextFactory.class);

        ServiceResolver factory = new ServiceResolver((Transport) ih.proxy(), (ContextFactory) cf.proxy()) {
            protected Class getStubClass(String publishedServiceName, String objectName) {
                return null;
            }
        };

        String[] services = factory.getServiceNames();
        assertEquals(2, services.length);
        assertEquals("1", services[0]);
        assertEquals("2", services[1]);
    }



}
