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


import org.codehaus.jremoting.ClientContext;
import org.codehaus.jremoting.Contextualizable;
import org.codehaus.jremoting.api.AuthenticationException;
import org.codehaus.jremoting.api.MethodNameHelper;
import org.codehaus.jremoting.api.Session;
import org.codehaus.jremoting.requests.ClassRequest;
import org.codehaus.jremoting.responses.ClassResponse;
import org.codehaus.jremoting.responses.ClassRetrievalFailedResponse;
import org.codehaus.jremoting.responses.ExceptionResponse;
import org.codehaus.jremoting.requests.GarbageCollectionRequest;
import org.codehaus.jremoting.responses.GarbageCollectionResponse;
import org.codehaus.jremoting.requests.GroupedMethodRequest;
import org.codehaus.jremoting.responses.InvocationExceptionResponse;
import org.codehaus.jremoting.requests.ListMethodsRequest;
import org.codehaus.jremoting.responses.ListMethodsResponse;
import org.codehaus.jremoting.responses.ListResponse;
import org.codehaus.jremoting.requests.LookupRequest;
import org.codehaus.jremoting.responses.LookupResponse;
import org.codehaus.jremoting.requests.MethodAsyncRequest;
import org.codehaus.jremoting.responses.MethodFacadeArrayResponse;
import org.codehaus.jremoting.requests.MethodFacadeRequest;
import org.codehaus.jremoting.responses.MethodFacadeResponse;
import org.codehaus.jremoting.responses.MethodResponse;
import org.codehaus.jremoting.responses.NoSuchSessionResponse;
import org.codehaus.jremoting.responses.NotPublishedResponse;
import org.codehaus.jremoting.responses.OpenConnectionResponse;
import org.codehaus.jremoting.responses.PingResponse;
import org.codehaus.jremoting.requests.*;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.responses.ResponseConstants;
import org.codehaus.jremoting.responses.*;
import org.codehaus.jremoting.server.Authenticator;
import org.codehaus.jremoting.server.ClassRetrievalException;
import org.codehaus.jremoting.server.ClassRetriever;
import org.codehaus.jremoting.server.MethodInvocationHandler;
import org.codehaus.jremoting.server.ServerInvocationHandler;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;
import org.codehaus.jremoting.server.authenticators.DefaultAuthenticator;
import org.codehaus.jremoting.server.classretrievers.NoClassRetriever;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.transports.DefaultMethodInvocationHandler;
import org.codehaus.jremoting.server.transports.DefaultServerSideClientContextFactory;

import java.rmi.server.UID;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * Class InvocationHandlerAdapter
 *
 * @author Paul Hammant
 * @version $Revision: 1.3 $
 */
public class InvocationHandlerAdapter extends PublicationAdapter implements ServerInvocationHandler {

    private static long c_session = 0;
    private static final UID U_ID = new UID((short) 20729);
    private Long lastSession = new Long(0);
    private HashMap sessions = new HashMap();
    private boolean suspend = false;
    private ClassRetriever classRetriever = new NoClassRetriever();
    private Authenticator authenticator = new DefaultAuthenticator();
    private ServerMonitor serverMonitor;

    private ServerSideClientContextFactory clientContextFactory;


    public InvocationHandlerAdapter(ClassRetriever classRetriever, Authenticator authenticator, ServerMonitor serverMonitor, ServerSideClientContextFactory clientContextFactory) {
        this.classRetriever = classRetriever;
        this.authenticator = authenticator;
        this.serverMonitor = serverMonitor;
        this.clientContextFactory = clientContextFactory;
    }

    /**
     * Handle an invocation
     *
     * @param request The request
     * @return The reply.
     */
    public Response handleInvocation(Request request, Object connectionDetails) {

        try {
            if (suspend == true) {
                return new SuspendedResponse();
            }

            // Method request is positioned first as
            // it is the one we want to be most speedy.
            if (request.getRequestCode() == RequestConstants.METHODREQUEST) {

                MethodRequest methodRequest = (MethodRequest) request;
                setClientContext(methodRequest);
                return doMethodRequest(methodRequest, connectionDetails);

            } else if (request.getRequestCode() == RequestConstants.METHODFACADEREQUEST) {
                MethodFacadeRequest methodFacadeRequest = (MethodFacadeRequest) request;
                setClientContext(methodFacadeRequest);
                return doMethodFacadeRequest(methodFacadeRequest, connectionDetails);

            } else if (request.getRequestCode() == RequestConstants.METHODASYNCREQUEST) {
                MethodAsyncRequest methodAsyncRequest = (MethodAsyncRequest) request;
                setClientContext(methodAsyncRequest);
                return doMethodAsyncRequest(methodAsyncRequest, connectionDetails);

            } else if (request.getRequestCode() == RequestConstants.GCREQUEST) {

                return doGarbageCollectionRequest(request);

            } else if (request.getRequestCode() == RequestConstants.LOOKUPREQUEST) {
                return doLookupRequest(request);

            } else if (request.getRequestCode() == RequestConstants.CLASSREQUEST) {
                return doClassRequest(request);

            } else if (request.getRequestCode() == RequestConstants.OPENCONNECTIONREQUEST) {
                OpenConnectionRequest openConnectionRequest = (OpenConnectionRequest) request;
                return doOpenConnectionRequest(openConnectionRequest.getMachineID());

            } else if (request.getRequestCode() == RequestConstants.PINGREQUEST) {

                // we could communicate back useful state info in this transaction.
                return new PingResponse();
            } else if (request.getRequestCode() == RequestConstants.LISTREQUEST) {
                return doListRequest();
            } else if (request.getRequestCode() == RequestConstants.LISTMETHODSREQUEST) {
                return doListMethodsRequest(request);
            } else {
                return new RequestFailedResponse("Unknown request :" + request.getClass().getName());
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            if (request instanceof MethodRequest) {
                String methd = ((MethodRequest) request).getMethodSignature();
                getServerMonitor().unexpectedException(InvocationHandlerAdapter.class, "InvocationHandlerAdapter.handleInvocation() NPE processing method " + methd, npe);
                throw new NullPointerException("Null pointer exception, processing method " + methd);
            } else {
                getServerMonitor().unexpectedException(InvocationHandlerAdapter.class, "InvocationHandlerAdapter.handleInvocation() NPE", npe);
                throw npe;
            }
        }
    }

    protected synchronized ServerSideClientContextFactory getClientContextFactory() {
        if (clientContextFactory == null) {
            clientContextFactory = new DefaultServerSideClientContextFactory();
        }
        return clientContextFactory;
    }

    private void setClientContext(Contextualizable request) {
        Long session = request.getSession();
        ClientContext clientSideClientContext = request.getContext();

        // *always* happens before method invocations.
        getClientContextFactory().set(session, clientSideClientContext);

    }


    /**
     * Do a Method Facade Request
     *
     * @param facadeRequest the request
     * @return The reply
     */
    private Response doMethodFacadeRequest(MethodFacadeRequest facadeRequest, Object connectionDetails) {

        if (!sessionExists(facadeRequest.getSession()) && (connectionDetails == null || !connectionDetails.equals("callback"))) {
            return new NoSuchSessionResponse(facadeRequest.getSession());
        }

        String publishedThing = facadeRequest.getPublishedServiceName() + "_" + facadeRequest.getObjectName();

        if (!isPublished(publishedThing)) {
            return new NotPublishedResponse();
        }

        //if( !sessionExists( facadeRequest.getSession() ) )
        //{
        //    return new ExceptionResponse(
        //        new InvocationException( "TODO - you dirty rat/hacker" ) );
        //}

        MethodInvocationHandler methodInvocationHandler = getMethodInvocationHandler(publishedThing);
        Response ar = methodInvocationHandler.handleMethodInvocation(facadeRequest, connectionDetails);

        if (ar.getResponseCode() == ResponseConstants.EXCEPTIONRESPONSE) {
            return ar;
        } else if (ar.getResponseCode() >= ResponseConstants.PROBLEMRESPONSE) {
            return ar;
        } else if (ar.getResponseCode() == ResponseConstants.METHODRESPONSE) {
            Object methodReply = ((MethodResponse) ar).getResponseObject();

            if (methodReply == null) {
                return new MethodFacadeResponse(null, null);    // null passing
            } else if (!methodReply.getClass().isArray()) {
                return doMethodFacadeRequestNonArray(methodReply, facadeRequest);
            } else {
                return doMethodFacadeRequestArray(methodReply, facadeRequest);

            }
        } else {
            // unknown reply type from
            return new RequestFailedResponse("TODO");
        }
    }

    /**
     * Do a method facade request, returning an array
     *
     * @param methodReply         The array to process.
     * @param methodFacadeRequest The request
     * @return The reply
     */
    private Response doMethodFacadeRequestArray(Object methodReply, MethodFacadeRequest methodFacadeRequest) {
        Object[] beanImpls = (Object[]) methodReply;
        Long[] refs = new Long[beanImpls.length];
        String[] objectNames = new String[beanImpls.length];

        if (!sessionExists(methodFacadeRequest.getSession())) {
            return new NoSuchSessionResponse(methodFacadeRequest.getSession());
        }

        for (int i = 0; i < beanImpls.length; i++) {
            Object impl = beanImpls[i];
            MethodInvocationHandler mainMethodInvocationHandler = getMethodInvocationHandler(methodFacadeRequest.getPublishedServiceName() + "_Main");

            objectNames[i] = MethodNameHelper.encodeClassName(mainMethodInvocationHandler.getMostDerivedType(beanImpls[i]).getName());

            MethodInvocationHandler methodInvocationHandler2 = getMethodInvocationHandler(methodFacadeRequest.getPublishedServiceName() + "_" + objectNames[i]);

            if (methodInvocationHandler2 == null) {
                return new NotPublishedResponse();
            }

            //TODO a decent ref number for main?
            if (beanImpls[i] == null) {
                refs[i] = null;
            } else {
                refs[i] = methodInvocationHandler2.getOrMakeReferenceIDForBean(beanImpls[i]);

                Session sess = (Session) sessions.get(methodFacadeRequest.getSession());

                sess.addBeanInUse(refs[i], beanImpls[i]);
            }
        }

        return new MethodFacadeArrayResponse(refs, objectNames);
    }

    /**
     * Do a method facade request, returning things other that an array
     *
     * @param beanImpl            The returned object to process.
     * @param methodFacadeRequest The request
     * @return The reply
     */
    private Response doMethodFacadeRequestNonArray(Object beanImpl, MethodFacadeRequest methodFacadeRequest) {

        if (!sessionExists(methodFacadeRequest.getSession())) {
            return new NoSuchSessionResponse(methodFacadeRequest.getSession());
        }

        MethodInvocationHandler mainMethodInvocationHandler = getMethodInvocationHandler(methodFacadeRequest.getPublishedServiceName() + "_Main");

        String objectName = MethodNameHelper.encodeClassName(mainMethodInvocationHandler.getMostDerivedType(beanImpl).getName());

        MethodInvocationHandler methodInvocationHandler = getMethodInvocationHandler(methodFacadeRequest.getPublishedServiceName() + "_" + objectName);

        if (methodInvocationHandler == null) {
            return new NotPublishedResponse();
        }

        //if( !sessionExists( methodFacadeRequest.getSession() ) )
        //{
        //    return new ExceptionResponse(
        //        new InvocationException( "TODO - you dirty rat/hacker" ) );
        //}

        //TODO a decent ref number for main?
        Long newRef = methodInvocationHandler.getOrMakeReferenceIDForBean(beanImpl);

        // make sure the bean is not garbage collected.
        Session sess = (Session) sessions.get(methodFacadeRequest.getSession());

        sess.addBeanInUse(newRef, beanImpl);

        //long newRef2 = asih2.getOrMakeReferenceIDForBean(beanImpl);
        return new MethodFacadeResponse(newRef, objectName);
    }

    /**
     * Do a method request
     *
     * @param methodRequest The request
     * @return The reply
     */
    private Response doMethodRequest(MethodRequest methodRequest, Object connectionDetails) {

        if (!sessionExists(methodRequest.getSession()) && (connectionDetails == null || !connectionDetails.equals("callback"))) {
            return new NoSuchSessionResponse(methodRequest.getSession());
        }

        String publishedThing = methodRequest.getPublishedServiceName() + "_" + methodRequest.getObjectName();

        if (!isPublished(publishedThing)) {
            return new NotPublishedResponse();
        }

        MethodInvocationHandler methodInvocationHandler = getMethodInvocationHandler(publishedThing);

        return methodInvocationHandler.handleMethodInvocation(methodRequest, connectionDetails);
    }

    private Response doMethodAsyncRequest(MethodAsyncRequest methodRequest, Object connectionDetails) {

        if (!sessionExists(methodRequest.getSession())) {
            return new NoSuchSessionResponse(methodRequest.getSession());
        }

        String publishedThing = methodRequest.getPublishedServiceName() + "_" + methodRequest.getObjectName();

        if (!isPublished(publishedThing)) {
            return new NotPublishedResponse();
        }

        MethodInvocationHandler methodInvocationHandler = getMethodInvocationHandler(publishedThing);

        GroupedMethodRequest[] requests = methodRequest.getGroupedRequests();
        for (int i = 0; i < requests.length; i++) {
            GroupedMethodRequest rawRequest = requests[i];
            methodInvocationHandler.handleMethodInvocation(new MethodRequest(methodRequest.getPublishedServiceName(), methodRequest.getObjectName(), rawRequest.getMethodSignature(), rawRequest.getArgs(), methodRequest.getReferenceID(), methodRequest.getSession()), connectionDetails);
        }

        return new MethodResponse();

    }


    /**
     * DO a lokkup request
     *
     * @param request The request
     * @return The reply
     */
    private Response doLookupRequest(Request request) {
        LookupRequest lr = (LookupRequest) request;

        try {
            authenticator.checkAuthority(lr.getAuthentication(), lr.getPublishedServiceName());
        } catch (AuthenticationException aae) {
            return new ExceptionResponse(aae);
        }

        //TODO a decent ref number for main?
        return new LookupResponse(new Long(0));
    }

    /**
     * Do a class request
     *
     * @param request The request
     * @return The reply
     */
    private Response doClassRequest(Request request) {
        ClassRequest cr = (ClassRequest) request;
        String publishedThing = cr.getPublishedServiceName() + "_" + cr.getObjectName();

        try {
            return new ClassResponse(classRetriever.getProxyClassBytes(publishedThing));
        } catch (ClassRetrievalException e) {
            return new ClassRetrievalFailedResponse(e.getMessage());
        }
    }

    /**
     * Do an OpenConnection request
     *
     * @return The reply.
     */
    private Response doOpenConnectionRequest(UID machineID) {
        if (machineID != null && machineID.equals(U_ID)) {
            return new SameVMResponse();
        } else {
            Long session = getNewSession();
            sessions.put(session, new Session(session));
            return new OpenConnectionResponse(authenticator.getTextToSign(), session);
        }
    }

    /**
     * Do a ListRequest
     *
     * @return The reply
     */
    private Response doListRequest() {
        //return the list of published objects to the server
        Iterator iterator = getIteratorOfPublishedObjects();
        Vector vecOfPublishedObjectNames = new Vector();

        while (iterator.hasNext()) {
            final String item = (String) iterator.next();

            if (item.endsWith("_Main")) {
                vecOfPublishedObjectNames.add(item.substring(0, item.lastIndexOf("_Main")));
            }
        }

        String[] listOfPublishedObjectNames = new String[vecOfPublishedObjectNames.size()];

        System.arraycopy(vecOfPublishedObjectNames.toArray(), 0, listOfPublishedObjectNames, 0, vecOfPublishedObjectNames.size());

        return new ListResponse(listOfPublishedObjectNames);
    }

    /**
     * Do a GarbageCollection Request
     *
     * @param request The request
     * @return The reply
     */
    private Response doGarbageCollectionRequest(Request request) {
        GarbageCollectionRequest gcr = (GarbageCollectionRequest) request;
        String publishedThing = gcr.getPublishedServiceName() + "_" + gcr.getObjectName();

        if (!isPublished(publishedThing)) {
            return new NotPublishedResponse();
        }

        Long session = gcr.getSession();
        if (!sessionExists(session)) {
            return new InvocationExceptionResponse("TODO - you dirty rat/hacker");
        }

        Session sess = (Session) sessions.get(session);
        if (sess == null) {
            System.err.println("DEBUG- GC on missing session - " + session);
        } else {
            if (gcr.getReferenceID() == null) {
                System.err.println("DEBUG- GC on missing referenceID -" + gcr.getReferenceID());
            } else {
                sess.removeBeanInUse(gcr.getReferenceID());
            }
        }

        MethodInvocationHandler methodInvocationHandler = getMethodInvocationHandler(publishedThing);

        return new GarbageCollectionResponse();
    }

    /**
     * Do a ListMethods Request
     *
     * @param request The request
     * @return The reply
     */
    private Response doListMethodsRequest(Request request) {
        ListMethodsRequest lReq = (ListMethodsRequest) request;
        String publishedThing = lReq.getPublishedName() + "_Main";

        if (!isPublished(publishedThing)) {
            //Should it throw an exception back?
            return new ListMethodsResponse(new String[0]);
        }

        DefaultMethodInvocationHandler methodInvocationHandler = (DefaultMethodInvocationHandler) getMethodInvocationHandler(publishedThing);

        return new ListMethodsResponse(methodInvocationHandler.getListOfMethods());
    }

    /**
     * Does a session exist
     *
     * @param session The session
     * @return true if it exists
     */
    private boolean sessionExists(Long session) {

        if (lastSession.equals(session)) {

            // buffer last session for performance.
            return true;
        } else {
            if (sessions.containsKey(session)) {
                lastSession = session;

                return true;
            }
        }

        return false;
    }


    /**
     * Get a new session ID
     *
     * @return The session
     */
    private Long getNewSession() {
        // approve everything and set session identifier.
        return new Long((++c_session << 16) + ((long) (Math.random() * 65536)));
    }

    /**
     * Suspend an service
     */
    public void suspend() {
        suspend = true;
    }

    /**
     * Resume an service
     */
    public void resume() {
        suspend = false;
    }

    public void setServerMonitor(ServerMonitor serverMonitor) {
        this.serverMonitor = serverMonitor;
    }

    public synchronized ServerMonitor getServerMonitor() {
        if (serverMonitor == null) {
            serverMonitor = new ConsoleServerMonitor();
        }
        return serverMonitor;
    }

}
