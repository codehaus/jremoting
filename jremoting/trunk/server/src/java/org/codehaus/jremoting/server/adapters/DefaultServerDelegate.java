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
package org.codehaus.jremoting.server.adapters;

import org.codehaus.jremoting.Contextualizable;
import org.codehaus.jremoting.client.Context;
import org.codehaus.jremoting.requests.CloseConnection;
import org.codehaus.jremoting.requests.CollectGarbage;
import org.codehaus.jremoting.requests.GroupedMethodRequest;
import org.codehaus.jremoting.requests.InvokeAsyncMethod;
import org.codehaus.jremoting.requests.InvokeFacadeMethod;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.requests.LookupService;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.requests.RequestConstants;
import org.codehaus.jremoting.requests.RetrieveStub;
import org.codehaus.jremoting.responses.AuthenticationFailed;
import org.codehaus.jremoting.responses.ConnectionClosed;
import org.codehaus.jremoting.responses.ConnectionOpened;
import org.codehaus.jremoting.responses.ExceptionThrown;
import org.codehaus.jremoting.responses.FacadeArrayMethodInvoked;
import org.codehaus.jremoting.responses.FacadeMethodInvoked;
import org.codehaus.jremoting.responses.GarbageCollected;
import org.codehaus.jremoting.responses.MethodInvoked;
import org.codehaus.jremoting.responses.NoSuchSession;
import org.codehaus.jremoting.responses.NotPublished;
import org.codehaus.jremoting.responses.Ping;
import org.codehaus.jremoting.responses.ProblemResponse;
import org.codehaus.jremoting.responses.Redirected;
import org.codehaus.jremoting.responses.RequestFailed;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.responses.Service;
import org.codehaus.jremoting.responses.ServicesList;
import org.codehaus.jremoting.responses.ServicesSuspended;
import org.codehaus.jremoting.responses.StubClass;
import org.codehaus.jremoting.responses.StubRetrievalFailed;
import org.codehaus.jremoting.server.Authenticator;
import org.codehaus.jremoting.server.ServerDelegate;
import org.codehaus.jremoting.server.Publisher;
import org.codehaus.jremoting.server.ServerContextFactory;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideContext;
import org.codehaus.jremoting.server.Session;
import org.codehaus.jremoting.server.StubRetrievalException;
import org.codehaus.jremoting.server.StubRetriever;
import org.codehaus.jremoting.server.servicehandlers.ServiceHandler;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.util.MethodNameHelper;
import org.codehaus.jremoting.util.StaticStubHelper;

/**
 * Class DefaultServerDelegate
 *
 * @author Paul Hammant
 */
public class DefaultServerDelegate extends SessionAdapter implements ServerDelegate {

    private final StubRetriever stubRetriever;
    private final Authenticator authenticator;
    private final ServerMonitor serverMonitor;
    private final ServerContextFactory contextFactory;

    public DefaultServerDelegate(ServerMonitor serverMonitor, StubRetriever stubRetriever,
                           Authenticator authenticator, ServerContextFactory contextFactory) {
        super(stubRetriever instanceof Publisher ? (Publisher) stubRetriever : null, serverMonitor);
        this.stubRetriever = stubRetriever;
        this.authenticator = authenticator;
        this.serverMonitor = serverMonitor != null ? serverMonitor : new ConsoleServerMonitor();
        this.contextFactory = contextFactory != null ? contextFactory : new ThreadLocalServerContextFactory();
    }

    public Response invoke(Request request, String connectionDetails) {
        try {
            if (isSuspended()) {
                return new ServicesSuspended();
            }
            // Method request is positioned first as
            // it is the one we want to be most speedy.
            if (request.getRequestCode() == RequestConstants.METHODREQUEST) {

                InvokeMethod invokeMethod = (InvokeMethod) request;
                setClientContext(invokeMethod);
                return doMethodRequest(invokeMethod, connectionDetails);

            } else if (request.getRequestCode() == RequestConstants.METHODFACADEREQUEST) {
                InvokeFacadeMethod invokeFacadeMethod = (InvokeFacadeMethod) request;
                setClientContext(invokeFacadeMethod);
                return doMethodFacadeRequest(invokeFacadeMethod, connectionDetails);

            } else if (request.getRequestCode() == RequestConstants.METHODASYNCREQUEST) {
                InvokeAsyncMethod invokeAsyncMethod = (InvokeAsyncMethod) request;
                setClientContext(invokeAsyncMethod);
                return doMethodAsyncRequest(invokeAsyncMethod, connectionDetails);

            } else if (request.getRequestCode() == RequestConstants.GCREQUEST) {

                return doGarbageCollectionRequest(request);

            } else if (request.getRequestCode() == RequestConstants.LOOKUPREQUEST) {
                return doLookupRequest(request);

            } else if (request.getRequestCode() == RequestConstants.CLASSREQUEST) {
                return doClassRequest(request);

            } else if (request.getRequestCode() == RequestConstants.OPENCONNECTIONREQUEST) {
                return doOpenConnectionRequest(connectionDetails);

            } else if (request.getRequestCode() == RequestConstants.CLOSECONNECTIONREQUEST) {
                CloseConnection closeConnection = (CloseConnection) request;
                return doCloseConnectionRequest(closeConnection.getSessionID());

            } else if (request.getRequestCode() == RequestConstants.PINGREQUEST) {
                return doPing(request);

            } else if (request.getRequestCode() == RequestConstants.LISTSERVICESREQUEST) {
                return doServiceListRequest();

            } else {
                return new RequestFailed("Unknown Request Type: " + request.getClass().getName());
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            if (request instanceof InvokeMethod) {
                String methd = ((InvokeMethod) request).getMethodSignature();
                serverMonitor.unexpectedException(DefaultServerDelegate.class, "InvokerDelegate.invoke() NPE processing method " + methd, npe);
                throw new NullPointerException("Null pointer exception, processing method " + methd);
            } else {
                serverMonitor.unexpectedException(DefaultServerDelegate.class, "InvokerDelegate.invoke() NPE", npe);
                throw npe;
            }
        }
    }

    private void setClientContext(Contextualizable request) {
        long session = request.getSessionID();
        Context clientSideContext = request.getContext();
        // *always* happens before method invocations.
        contextFactory.set(new ServerSideContext(session, clientSideContext));

    }

    private Response doMethodFacadeRequest(InvokeFacadeMethod facadeRequest, Object connectionDetails) {
        if (!doesSessionExistAndRefreshItIfItDoes(facadeRequest.getSessionID()) && (connectionDetails == null || !connectionDetails.equals("callback"))) {
            return new NoSuchSession(facadeRequest.getSessionID());
        }
        String publishedThing = facadeRequest.getService() + "_" + facadeRequest.getObjectName();
        if (!isPublished(publishedThing)) {
            return new NotPublished();
        }
        ServiceHandler serviceHandler = getServiceHandler(publishedThing);
        Response response = serviceHandler.handleMethodInvocation(facadeRequest, connectionDetails);
        if (response instanceof ExceptionThrown) {
            return response;
        } else if (response instanceof ProblemResponse) {
            return response;
        } else if (response instanceof MethodInvoked) {
            Object methodResponse = ((MethodInvoked) response).getResponseObject();

            if (methodResponse == null) {
                return new FacadeMethodInvoked(null, null);    // null passing
            } else if (!methodResponse.getClass().isArray()) {
                return doMethodFacadeRequestNonArray(methodResponse, facadeRequest);
            } else {
                return doMethodFacadeRequestArray(methodResponse, facadeRequest);
            }
        } else {
            // unknown reply type from
            return new RequestFailed("Unknown Request Type: " + response.getClass().getName());
        }
    }

    private Response doMethodFacadeRequestArray(Object methodResponse, InvokeFacadeMethod invokeFacadeMethod) {
        Object[] instances = (Object[]) methodResponse;
        Long[] refs = new Long[instances.length];
        String[] objectNames = new String[instances.length];

        if (!doesSessionExistAndRefreshItIfItDoes(invokeFacadeMethod.getSessionID())) {
            return new NoSuchSession(invokeFacadeMethod.getSessionID());
        }

        for (int i = 0; i < instances.length; i++) {
            Object impl = instances[i];
            ServiceHandler mainServiceHandler = getServiceHandler(StaticStubHelper.formatServiceName(invokeFacadeMethod.getService()));
            objectNames[i] = MethodNameHelper.encodeClassName(mainServiceHandler.getMostDerivedType(impl).getName());
            ServiceHandler serviceHandler2 = getServiceHandler(StaticStubHelper.formatServiceName(invokeFacadeMethod.getService(), objectNames[i]));
            if (serviceHandler2 == null) {
                return new NotPublished();
            }
            if (impl == null) {
                refs[i] = null;
            } else {
                refs[i] = serviceHandler2.getOrMakeReferenceIDForInstance(impl);
                getSession(invokeFacadeMethod.getSessionID()).addInstanceInUse(refs[i], impl);
            }
        }
        return new FacadeArrayMethodInvoked(refs, objectNames);
    }

    private Response doMethodFacadeRequestNonArray(Object instance, InvokeFacadeMethod invokeFacadeMethod) {
        if (!doesSessionExistAndRefreshItIfItDoes(invokeFacadeMethod.getSessionID())) {
            return new NoSuchSession(invokeFacadeMethod.getSessionID());
        }
        ServiceHandler mainServiceHandler = getServiceHandler(StaticStubHelper.formatServiceName(invokeFacadeMethod.getService()));
        String objectName = MethodNameHelper.encodeClassName(mainServiceHandler.getMostDerivedType(instance).getName());
        ServiceHandler serviceHandler = getServiceHandler(invokeFacadeMethod.getService() + "_" + objectName);
        if (serviceHandler == null) {
            return new NotPublished();
        }
        Long newRef = serviceHandler.getOrMakeReferenceIDForInstance(instance);

        // make sure the instance is not garbage collected.
        Session sess = getSession(invokeFacadeMethod.getSessionID());

        sess.addInstanceInUse(newRef, instance);

        //long newRef2 = asih2.getOrMakeReferenceIDForInstance(instance);
        return new FacadeMethodInvoked(newRef, objectName);
    }

    private Response doMethodRequest(InvokeMethod invokeMethod, Object connectionDetails) {
        if (!doesSessionExistAndRefreshItIfItDoes(invokeMethod.getSessionID()) && (connectionDetails == null || !connectionDetails.equals("callback"))) {
            return new NoSuchSession(invokeMethod.getSessionID());
        }
        String publishedThing = invokeMethod.getService() + "_" + invokeMethod.getObjectName();
        if (!isPublished(publishedThing)) {
            return new NotPublished();
        }
        ServiceHandler serviceHandler = getServiceHandler(publishedThing);
        return serviceHandler.handleMethodInvocation(invokeMethod, connectionDetails);
    }

    private Response doMethodAsyncRequest(InvokeAsyncMethod methodRequest, Object connectionDetails) {
        long session = methodRequest.getSessionID();
        if (!doesSessionExistAndRefreshItIfItDoes(session)) {
            return new NoSuchSession(session);
        }
        String publishedThing = methodRequest.getService() + "_" + methodRequest.getObjectName();
        if (!isPublished(publishedThing)) {
            return new NotPublished();
        }
        Session sess = getSession(session);
        ServiceHandler serviceHandler = getServiceHandler(publishedThing);
        GroupedMethodRequest[] requests = methodRequest.getGroupedRequests();
        for (GroupedMethodRequest rawRequest : requests) {
            serviceHandler.handleMethodInvocation(new InvokeMethod(methodRequest.getService(), methodRequest.getObjectName(), rawRequest.getMethodSignature(), rawRequest.getArgs(), methodRequest.getReference(), methodRequest.getSessionID()), connectionDetails);
        }
        return new MethodInvoked();
    }

    private Response doLookupRequest(Request request) {
        LookupService lr = (LookupService) request;
        String publishedServiceName = lr.getService();
        if (!authenticator.checkAuthority(lr.getAuthentication(), publishedServiceName)) {
            return new AuthenticationFailed();
        }
        String service = StaticStubHelper.formatServiceName(publishedServiceName);
        if (isRedirected(publishedServiceName)) {
            return new Redirected(getRedirectedTo(publishedServiceName));
        }
        if (!isPublished(service)) {
            return new NotPublished();
        }
        //TODO a decent ref number for main?
        return new Service((long) 0, getFacadeClass(service).getName(), getAdditionalFacades(service));
    }

    private Response doClassRequest(Request request) {
        RetrieveStub cr = (RetrieveStub) request;
        String publishedThing = cr.getService() + "_" + cr.getObjectName();
        try {
            return new StubClass(stubRetriever.getStubClassBytes(publishedThing));
        } catch (StubRetrievalException e) {
            return new StubRetrievalFailed(e.getMessage());
        }
    }

    private Response doOpenConnectionRequest(String connectionDetails) {
        return new ConnectionOpened(authenticator.getAuthenticationChallenge(), newSession(connectionDetails));
    }

    private Response doCloseConnectionRequest(long session) {
        if (!sessionExists(session)) {
            return new NoSuchSession(session);
        } else {
            removeSession(session);
            return new ConnectionClosed(session);
        }
    }

    private Response doServiceListRequest() {
        return new ServicesList(getPublishedServices());
    }

    private Response doGarbageCollectionRequest(Request request) {
        CollectGarbage gcr = (CollectGarbage) request;
        String publishedThing = gcr.getService() + "_" + gcr.getObjectName();
        if (!isPublished(publishedThing)) {
            return new NotPublished();
        }
        long session = gcr.getSessionID();
        if (doesSessionExistAndRefreshItIfItDoes(session)) {
            Session sess = getSession(session);
            if (sess != null) {
                // session may have been removed before GC kicks in.
                if (gcr.getReference() == null) {
                    System.err.println("DEBUG- GC on missing reference -" + gcr.getReference());
                } else {
                    sess.removeInstanceInUse(gcr.getReference());
                }
            }
        }
        return new GarbageCollected();
    }

    private Response doPing(Request request) {
        org.codehaus.jremoting.requests.Ping ping = (org.codehaus.jremoting.requests.Ping) request;
        super.doesSessionExistAndRefreshItIfItDoes(ping.getSession());
        return new Ping();
    }
}
