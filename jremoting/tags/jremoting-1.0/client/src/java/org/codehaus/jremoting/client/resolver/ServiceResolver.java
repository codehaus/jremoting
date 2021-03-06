/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
 * Portions copyright 2001 - 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.codehaus.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.jremoting.client.resolver;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.RedirectedException;
import org.codehaus.jremoting.authentication.AuthenticationChallenge;
import org.codehaus.jremoting.client.Authenticator;
import org.codehaus.jremoting.client.ContextFactory;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.client.Stub;
import org.codehaus.jremoting.client.StubFactory;
import org.codehaus.jremoting.client.StubHelper;
import org.codehaus.jremoting.client.StubRegistry;
import org.codehaus.jremoting.client.Transport;
import org.codehaus.jremoting.client.authentication.NullAuthenticator;
import org.codehaus.jremoting.client.context.ThreadLocalContextFactory;
import org.codehaus.jremoting.client.stubs.StubsViaReflection;
import org.codehaus.jremoting.requests.CollectGarbage;
import org.codehaus.jremoting.requests.GroupedMethodRequest;
import org.codehaus.jremoting.requests.InvokeAsyncMethod;
import org.codehaus.jremoting.requests.InvokeFacadeMethod;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.requests.ListServices;
import org.codehaus.jremoting.requests.LookupService;
import org.codehaus.jremoting.requests.Servicable;
import org.codehaus.jremoting.responses.AuthenticationFailed;
import org.codehaus.jremoting.responses.BadServerSideEvent;
import org.codehaus.jremoting.responses.ConnectionClosed;
import org.codehaus.jremoting.responses.ConnectionOpened;
import org.codehaus.jremoting.responses.ExceptionThrown;
import org.codehaus.jremoting.responses.FacadeArrayMethodInvoked;
import org.codehaus.jremoting.responses.FacadeMethodInvoked;
import org.codehaus.jremoting.responses.GarbageCollected;
import org.codehaus.jremoting.responses.MethodInvoked;
import org.codehaus.jremoting.responses.NoSuchReference;
import org.codehaus.jremoting.responses.NoSuchSession;
import org.codehaus.jremoting.responses.NotPublished;
import org.codehaus.jremoting.responses.Redirected;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.responses.Service;
import org.codehaus.jremoting.responses.ServicesList;
import org.codehaus.jremoting.util.FacadeRefHolder;
import org.codehaus.jremoting.util.StaticStubHelper;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Class ServiceResolver
 *
 * @author Paul Hammant
 * @author Peter Royal <a href="mailto:proyal@managingpartners.com">proyal@managingpartners.com</a>
 * @author Mauro Talevi
 */
public class ServiceResolver {

    private static final int STUB_PREFIX_LENGTH = StaticStubHelper.getStubPrefixLength();
    protected final Transport transport;
    private final ContextFactory contextFactory;
    private final StubFactory stubFactory;
    private final Authenticator authenticator;
    protected final HashMap<Long,WeakReference<Object>> refObjs = new HashMap<Long, WeakReference<Object>>();
    protected final long session;
    private final StubRegistry stubRegistry;
    private AuthenticationChallenge authChallenge;

    public ServiceResolver(Transport transport) throws ConnectionException {
        this(transport, defaultContextFactory());
    }

    public ServiceResolver(final Transport transport, ContextFactory contextFactory) throws ConnectionException {
        this(transport, contextFactory, defaultStubFactory());
    }

    public ServiceResolver(final Transport transport, StubFactory stubFactory) throws ConnectionException {
        this(transport, defaultContextFactory(), stubFactory, defaultAuthenticator());
    }

    public ServiceResolver(final Transport transport, Authenticator authenticator) throws ConnectionException {
        this(transport, defaultContextFactory(), defaultStubFactory(), authenticator);
    }

    public ServiceResolver(final Transport transport, ContextFactory contextFactory, StubFactory stubFactory) throws ConnectionException {
        this(transport, contextFactory, stubFactory, defaultAuthenticator());
    }

    public ServiceResolver(final Transport transport, ContextFactory contextFactory, Authenticator authenticator) throws ConnectionException {
        this(transport, contextFactory, defaultStubFactory(), authenticator);
    }

    public ServiceResolver(final Transport transport, StubFactory stubFactory, Authenticator authenticator) throws ConnectionException {
        this(transport, defaultContextFactory(), stubFactory, authenticator);
    }

    public ServiceResolver(final Transport transport, ContextFactory contextFactory, StubFactory stubFactory, Authenticator authenticator) throws ConnectionException {
        this.transport = transport;
        this.contextFactory = contextFactory;
        this.stubFactory = stubFactory;
        this.authenticator = authenticator;

        ConnectionOpened response = transport.openConnection();

        session = response.getSessionID();
        authChallenge = response.getAuthenticationChallenge();

        stubRegistry = new StubRegistry() {

            public void registerReferenceObject(Object instance, Long reference) {
                synchronized (this) {
                    refObjs.put(reference, new WeakReference<Object>(instance));
                }
            }

            public Object getInstance(Long reference) {
                WeakReference<Object> wr;
                synchronized (this) {
                    wr = refObjs.get(reference);
                }
                if (wr == null) {
                    return null;
                }
                Object obj = wr.get();

                if (obj == null) {
                    refObjs.remove(reference);
                }
                return obj;
            }

            public Object getInstance(String facadeClassName, String publishedServiceName, String objectName, StubHelper stubHelper) throws ConnectionException {
                return ServiceResolver.this.getInstance(facadeClassName, publishedServiceName, objectName, stubHelper);
            }

            public void marshallCorrection(String remoteObjectName, String methodSignature, Object[] args, Class[] argClasses) {
                for (int i = 0; i < argClasses.length; i++) {
                    Class argClass = argClasses[i];
                    if (argClass == null) {
                        continue;
                    }
                    //All remote references implement Stub interface
                    if (args[i] instanceof Stub) {
                        Stub stub = (Stub) args[i];

                        if (getReferenceID(stub) != null) {
                            //The stripping STUB_PREFIX from the stub names generated by StubGenerator
                            String objName;

                            if (args[i] instanceof StubsViaReflection.ReflectionStub) {
                                objName = ((StubsViaReflection.ReflectionStub) args[i]).jRemotingGetObjectName();
                            } else {
                                objName = args[i].getClass().getName().substring(STUB_PREFIX_LENGTH);
                            }

                            args[i] = makeFacadeRefHolder(stub, objName);
                        }
                    } else // Let the specific Invokers be given the last chance to modify the arguments.
                    {
                        args[i] = transport.resolveArgument(remoteObjectName, methodSignature, argClasses[i], args[i]);
                    }
                }
            }
        };
    }

    private static Authenticator defaultAuthenticator() {
        return new NullAuthenticator();
    }

    private static StubFactory defaultStubFactory() {
        return new StubsViaReflection();
    }

    private static ContextFactory defaultContextFactory() {
        return new ThreadLocalContextFactory();
    }


    public final Long getReferenceID(Stub obj) {
        return obj.jRemotingGetReferenceID(this);
    }

    public final Object resolveService(String publishedServiceName) throws ConnectionException, RedirectedException {
        Response response = transport.invoke(new LookupService(publishedServiceName, authenticator.authenticate(authChallenge), session), true);

        if (response instanceof NotPublished) {
            throw new ConnectionException("Service '" + publishedServiceName + "' not published");
        } else if (response instanceof Redirected) {
            throw new RedirectedException(((Redirected) response).getRedirectedTo());
        } else if (response instanceof AuthenticationFailed) {
            throw new ConnectionException("Authentication Failed");
        } else if (response instanceof ExceptionThrown) {
            ExceptionThrown er = (ExceptionThrown) response;
            Throwable t = er.getResponseException();

            if (t instanceof ConnectionException) {
                throw (ConnectionException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new ConnectionException("Problem doing lookup on service [exception: " + t.getMessage() + "]");
            }
        }

        Service service = (Service) response;
        DefaultStubHelper stubHelper = new DefaultStubHelper(contextFactory, publishedServiceName, "Main", service.getReference(), service);
        Object retVal = getInstance(service.getPrimaryFacadeName(), publishedServiceName, "Main", stubHelper);

        stubHelper.registerInstance(retVal);

        return retVal;

    }

    public String[] getServiceNames() {
        Response ar = transport.invoke(new ListServices(), true);
        return ((ServicesList) ar).getServices();
    }

    public boolean hasService(String publishedServiceName) {
        final String[] services = getServiceNames();
        for (String service : services) {
            if (service.equals(publishedServiceName)) {
                return true;
            }
        }
        return false;
    }

    private FacadeRefHolder makeFacadeRefHolder(Stub obj, String objectName) {
        Long refID = getReferenceID(obj);
        return new FacadeRefHolder(refID, objectName);
    }


    protected Object getInstance(String facadeClassName, String publishedServiceName, String objectName, StubHelper stubHelper) throws ConnectionException {
            return stubFactory.instantiateStub(facadeClassName, publishedServiceName, objectName, transport, stubHelper);
    }

    public void close() {
        transport.closeConnection(session);
    }

    public final class DefaultStubHelper implements StubHelper {

        private final transient String publishedServiceName;
        private final transient String objectName;
        private final transient Long reference;
        private final Service service;
        //private ContextFactory contextFactory;
        private ArrayList<GroupedMethodRequest> queuedAsyncRequests = new ArrayList<GroupedMethodRequest>();

        public DefaultStubHelper(ContextFactory contextFactory, String pubishedServiceName, String objectName, Long reference, Service service) {
            //this.contextFactory = contextFactory;
            publishedServiceName = pubishedServiceName;
            this.objectName = objectName;
            this.reference = reference;
            this.service = service;
            if (stubRegistry == null) {
                throw new IllegalArgumentException("stubRegistry cannot be null");
            }
            if (transport == null) {
                throw new IllegalArgumentException("transport cannot be null");
            }

        }

        public void registerInstance(Object instance) {
            stubRegistry.registerReferenceObject(instance, reference);
        }

        public Object processObjectRequestGettingFacade(Class returnClassType, String methodSignature, Object[] args, String objectName) throws Throwable {
            try {
                Object result;

                InvokeFacadeMethod request;

                if (objectName.endsWith("[]")) {
                    request = new InvokeFacadeMethod(publishedServiceName, this.objectName, methodSignature, args, reference, objectName.substring(0, objectName.length() - 2), session);
                } else {
                    request = new InvokeFacadeMethod(publishedServiceName, this.objectName, methodSignature, args, reference, objectName, session);
                }

                setContext(request);
                Response response = transport.invoke(request, true);

                if (response instanceof FacadeMethodInvoked) {
                    result = facadeMethodInvoked((FacadeMethodInvoked)response, returnClassType);
                } else if (response instanceof FacadeArrayMethodInvoked) {
                    result = facadeArrayMethodInvoked(response, returnClassType);
                } else {
                    throw makeUnexpectedResponseThrowable(response);
                }
                return result;
            } catch (InvocationException ie) {
                transport.getClientMonitor().invocationFailure(this.getClass(), publishedServiceName, objectName, methodSignature, ie);
                throw ie;
            }
        }

        private Object facadeArrayMethodInvoked(Response response, Class returnClassType) {
            FacadeArrayMethodInvoked mfar = (FacadeArrayMethodInvoked) response;
            Long[] refs = mfar.getReferences();
            String[] objectNames = mfar.getObjectNames();
            Object[] instances = (Object[]) Array.newInstance(returnClassType, refs.length);
            for (int i = 0; i < refs.length; i++) {
                facadeArrayMethodInvokedRef(returnClassType, refs, objectNames, instances, i);
            }
            return instances;
        }

        private void facadeArrayMethodInvokedRef(Class returnClassType, Long[] refs, String[] objectNames, Object[] instances, int i) {
            Long ref = refs[i];
            if (ref == null) {
                instances[i] = null;
            } else {
                Object o = stubRegistry.getInstance(ref);
                instances[i] = o;
                if (instances[i] == null) {
                    // TODO = by interface not impl
                    DefaultStubHelper sh = new DefaultStubHelper(contextFactory, publishedServiceName, objectNames[i], refs[i], service);
                    Object retFacade = null;
                    try {
                        retFacade = stubRegistry.getInstance(returnClassType.getName(), publishedServiceName, objectNames[i], sh);
                    } catch (Exception e) {
                        // TODO - get rid of this
                        System.out.println("objNameWithoutArray=" + returnClassType.getName());
                        System.out.flush();
                        e.printStackTrace();
                    }
                    sh.registerInstance(retFacade);
                    instances[i] = retFacade;
                }
            }
        }

        private Object facadeMethodInvoked(FacadeMethodInvoked mfr, Class returnClassType) throws ConnectionException {
            Long ref = mfr.getReference();
            // it might be that the return value was intended to be null.
            if (ref == null) {
                return null;
            }
            Object instance = stubRegistry.getInstance(ref);
            if (instance == null) {
                String facadeClassName = mfr.getObjectName().replace('$', '.');
                DefaultStubHelper stubHelper = new DefaultStubHelper(contextFactory, publishedServiceName, mfr.getObjectName(), ref, service);
                Object retFacade = stubRegistry.getInstance(facadeClassName, publishedServiceName, mfr.getObjectName(), stubHelper);
                stubHelper.registerInstance(retFacade);
                return retFacade;
            } else {
                return instance;
            }
        }

        public Object processObjectRequest(String methodSignature, Object[] args, Class[] argClasses) throws Throwable {
            try {
                Response response = invokeViaTransport(methodSignature, args, argClasses);
                if (response instanceof MethodInvoked) {
                    MethodInvoked or = (MethodInvoked) response;
                    return or.getResponseObject();
                } else {
                    throw makeUnexpectedResponseThrowable(response);
                }
            } catch (InvocationException ie) {
                transport.getClientMonitor().invocationFailure(this.getClass(), publishedServiceName, objectName, methodSignature, ie);
                throw ie;
            }
        }

        public void processVoidRequest(String methodSignature, Object[] args, Class[] argClasses) throws Throwable {
            try {
                Response response = invokeViaTransport(methodSignature, args, argClasses);
                if (response instanceof MethodInvoked) {
                    MethodInvoked or = (MethodInvoked) response;
                    return;
                } else {
                    throw makeUnexpectedResponseThrowable(response);
                }
            } catch (InvocationException ie) {
                transport.getClientMonitor().invocationFailure(this.getClass(), publishedServiceName, objectName, methodSignature, ie);
                throw ie;
            }
        }

        private Response invokeViaTransport(String methodSignature, Object[] args, Class[] argClasses) {
            stubRegistry.marshallCorrection(publishedServiceName, methodSignature, args, argClasses);
            InvokeMethod request = new InvokeMethod(publishedServiceName, objectName, methodSignature, args, reference, session);
            setContext(request);
            Response response = transport.invoke(request, true);
            return response;
        }

        public void queueAsyncRequest(String methodSignature, Object[] args, Class[] argClasses) {
            synchronized (queuedAsyncRequests) {
                GroupedMethodRequest request = new GroupedMethodRequest(methodSignature, args);
                queuedAsyncRequests.add(request);
            }
        }

        public void commitAsyncRequests() throws Throwable {
            synchronized (queuedAsyncRequests) {
                try {
                    GroupedMethodRequest[] rawRequests = new GroupedMethodRequest[queuedAsyncRequests.size()];
                    queuedAsyncRequests.toArray(rawRequests);
                    InvokeAsyncMethod request = new InvokeAsyncMethod(publishedServiceName, objectName, rawRequests, reference, session);
                    setContext(request);
                    Response response = transport.invoke(request, true);
                    if (response instanceof MethodInvoked) {
                        MethodInvoked or = (MethodInvoked) response;
                        return;
                    } else {
                        throw makeUnexpectedResponseThrowable(response);
                    }
                } catch (InvocationException ie) {
                    transport.getClientMonitor().invocationFailure(this.getClass(), publishedServiceName, objectName, "<various-async-grouped>", ie);
                    throw ie;
                }
            }
        }

        public void rollbackAsyncRequests() {
            synchronized (queuedAsyncRequests) {
                queuedAsyncRequests.clear();
            }
        }


        public void processVoidRequestWithRedirect(String methodSignature, Object[] args, Class[] argClasses) throws Throwable {
            Object[] newArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Stub) {
                    //TODO somehow get the reference details and put a redirect place holder here
                } else {
                    newArgs[i] = args[i];
                }
            }
            processVoidRequest(methodSignature, newArgs, argClasses);
        }


        private Throwable makeUnexpectedResponseThrowable(Response response) {
            if (response instanceof ExceptionThrown) {
                ExceptionThrown er = (ExceptionThrown) response;
                return er.getResponseException();
            } else if (response instanceof NoSuchSession) {
                NoSuchSession nssr = (NoSuchSession) response;
                return new InvocationException("There is no session on the server with id " + nssr.getSessionID()
                        + ". The server may have been bounced since the client first did a lookup.");
            } else if (response instanceof NoSuchReference) {
                NoSuchReference nsrr = (NoSuchReference) response;
                return new InvocationException("There is no instance on the server mapped to reference " + reference);
            } else if (response instanceof BadServerSideEvent) {
                BadServerSideEvent ier = (BadServerSideEvent) response;
                return new InvocationException(ier.getMessage());
            } else if (response instanceof ConnectionClosed) {
                throw new InvocationException("Connection closed by Server");
            } else {
                return new InvocationException("Internal Error : Unknown response type :" + response.getClass().getName());
            }
        }

        public Long getReference(Object serviceResolver) {
            // this checks the factory because reference IDs should not be
            // given out to any requester.  It should be privileged information.
            if (serviceResolver == ServiceResolver.this) {
                return reference;
            } else {
                return null;
            }
        }

        public boolean isEquals(Object o1, Object o2) {
            if (o2 == null) {
                return false;
            }
            if (o1 == o2) {
                return true;
            }
            if (o1.getClass() != o2.getClass()) {
                return false;
            }
            try {
                Object retVal = processObjectRequest("equals(java.lang.Object)", new Object[]{o2}, new Class[]{Object.class});
                return (Boolean) retVal;
            } catch (Throwable t) {
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else if (t instanceof Error) {
                    throw (Error) t;
                } else {
                    t.printStackTrace();
                    throw new org.codehaus.jremoting.client.InvocationException("Should never get here: " + t.getMessage());
                }
            }


        }

        public boolean isFacadeInterface(Class clazz) {
            if (clazz.getName().equals(service.getPrimaryFacadeName())) {
                return true;
            }
            String[] facadeNames = service.getAdditionalFacadeNames();
            for (String facadeName : facadeNames) {
                if (clazz.getName().equals(facadeName)) {
                    return true;
                }
            }
            return false;
        }

        protected void finalize() throws Throwable {

            synchronized (stubRegistry) {
                Response response = transport.invoke(new CollectGarbage(publishedServiceName, objectName, session, reference), false);
                if (response instanceof ExceptionThrown) {
                    // This happens if the object can not be GCed on the remote server
                    //  for any reason.
                    // There is nothing that we can do about it from here, so just let
                    //  this fall through.
                    // One case where this can happen is if the server is restarted quickly.
                    //  An object created in one ivocation will try to be gced in the second
                    //  invocation.  As the object does not exist, an error is thrown.

                    System.out.println("----> Got an ExceptionResponsensense in response to a CollectGarbage" );
                    ExceptionThrown er = (ExceptionThrown)response;
                    er.getResponseException().printStackTrace();

                } else if (response instanceof ConnectionClosed) {
                    // do nothing. GC came after connection was closed. Just bad timing.
                } else if (!(response instanceof GarbageCollected)) {
                    System.err.println("----> Some problem during Distributed Garbage Collection! Make sure factory is closed. Response = '" + response + "'");
                }
            }
            super.finalize();
        }

        private synchronized void setContext(Servicable request) {
            request.setContext(contextFactory.getClientContext());

        }

    }


}
