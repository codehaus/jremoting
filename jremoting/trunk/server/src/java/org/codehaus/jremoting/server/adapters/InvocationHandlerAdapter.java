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


import org.codehaus.jremoting.api.*;
import org.codehaus.jremoting.requests.*;
import org.codehaus.jremoting.responses.*;
import org.codehaus.jremoting.responses.Ping;
import org.codehaus.jremoting.server.*;
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
    private final HashMap sessions = new HashMap();
    private boolean suspend = false;
    private final StubRetriever stubRetriever;
    private final Authenticator authenticator;
    private final ServerMonitor serverMonitor;

    private final ServerSideClientContextFactory clientContextFactory;

    public InvocationHandlerAdapter(StubRetriever stubRetriever, Authenticator authenticator, ServerMonitor serverMonitor, ServerSideClientContextFactory clientContextFactory) {
        this.stubRetriever = stubRetriever;
        this.authenticator = authenticator;
        this.serverMonitor = serverMonitor != null ? serverMonitor : new ConsoleServerMonitor();
        this.clientContextFactory = clientContextFactory != null ? clientContextFactory : new DefaultServerSideClientContextFactory();
    }

    /**
     * Handle an invocation
     *
     * @param request The request
     * @return The reply.
     */
    public AbstractResponse handleInvocation(AbstractRequest request, Object connectionDetails) {

        try {
            if (suspend == true) {
                return new ServiceSuspended();
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
                OpenConnection openConnection = (OpenConnection) request;
                return doOpenConnectionRequest(openConnection.getMachineID());

            } else if (request.getRequestCode() == RequestConstants.CLOSECONNECTIONREQUEST) {
                CloseConnection closeConnection = (CloseConnection) request;
                return doCloseConnectionRequest(closeConnection.getSessionID());

            } else if (request.getRequestCode() == RequestConstants.PINGREQUEST) {

                // we could communicate back useful state info in this transaction.
                return new Ping();
            } else if (request.getRequestCode() == RequestConstants.LISTSERVICESREQUEST) {
                return doServiceListRequest();
            } else if (request.getRequestCode() == RequestConstants.LISTMETHODSREQUEST) {
                return doListMethodsRequest(request);
            } else {
                return new RequestFailed("Unknown request :" + request.getClass().getName());
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            if (request instanceof InvokeMethod) {
                String methd = ((InvokeMethod) request).getMethodSignature();
                getServerMonitor().unexpectedException(InvocationHandlerAdapter.class, "InvocationHandlerAdapter.handleInvocation() NPE processing method " + methd, npe);
                throw new NullPointerException("Null pointer exception, processing method " + methd);
            } else {
                getServerMonitor().unexpectedException(InvocationHandlerAdapter.class, "InvocationHandlerAdapter.handleInvocation() NPE", npe);
                throw npe;
            }
        }
    }


    protected synchronized ServerSideClientContextFactory getClientContextFactory() {
        return clientContextFactory;
    }

    private void setClientContext(Contextualizable request) {
        Long session = request.getSessionID();
        ClientContext clientSideClientContext = request.getContext();

        // *always* happens before method invocations.
        getClientContextFactory().set(session, clientSideClientContext);

    }


    /**
     * Do a Method Facade AbstractRequest
     *
     * @param facadeRequest the request
     * @return The reply
     */
    private AbstractResponse doMethodFacadeRequest(InvokeFacadeMethod facadeRequest, Object connectionDetails) {

        if (!sessionExists(facadeRequest.getSessionID()) && (connectionDetails == null || !connectionDetails.equals("callback")))
        {
            return new NoSuchSession(facadeRequest.getSessionID());
        }

        String publishedThing = facadeRequest.getService() + "_" + facadeRequest.getObjectName();

        if (!isPublished(publishedThing)) {
            return new NotPublished();
        }

        //if( !sessionExists( facadeRequest.getSession() ) )
        //{
        //    return new ExceptionThrown(
        //        new InvocationException( "TODO - you dirty rat/hacker" ) );
        //}

        MethodInvocationHandler methodInvocationHandler = getMethodInvocationHandler(publishedThing);
        AbstractResponse ar = methodInvocationHandler.handleMethodInvocation(facadeRequest, connectionDetails);

        if (ar.getResponseCode() == ResponseConstants.EXCEPTIONRESPONSE) {
            return ar;
        } else if (ar.getResponseCode() >= ResponseConstants.PROBLEMRESPONSE) {
            return ar;
        } else if (ar.getResponseCode() == ResponseConstants.METHODRESPONSE) {
            Object methodResponse = ((SimpleMethodInvoked) ar).getResponseObject();

            if (methodResponse == null) {
                return new FacadeMethodInvoked(null, null);    // null passing
            } else if (!methodResponse.getClass().isArray()) {
                return doMethodFacadeRequestNonArray(methodResponse, facadeRequest);
            } else {
                return doMethodFacadeRequestArray(methodResponse, facadeRequest);

            }
        } else {
            // unknown reply type from
            return new RequestFailed("TODO");
        }
    }

    /**
     * Do a method facade request, returning an array
     *
     * @param methodResponse        The array to process.
     * @param invokeFacadeMethod The request
     * @return The reply
     */
    private AbstractResponse doMethodFacadeRequestArray(Object methodResponse, InvokeFacadeMethod invokeFacadeMethod) {
        Object[] beanImpls = (Object[]) methodResponse;
        Long[] refs = new Long[beanImpls.length];
        String[] objectNames = new String[beanImpls.length];

        if (!sessionExists(invokeFacadeMethod.getSessionID())) {
            return new NoSuchSession(invokeFacadeMethod.getSessionID());
        }

        for (int i = 0; i < beanImpls.length; i++) {
            Object impl = beanImpls[i];
            MethodInvocationHandler mainMethodInvocationHandler = getMethodInvocationHandler(invokeFacadeMethod.getService() + "_Main");

            objectNames[i] = MethodNameHelper.encodeClassName(mainMethodInvocationHandler.getMostDerivedType(beanImpls[i]).getName());

            MethodInvocationHandler methodInvocationHandler2 = getMethodInvocationHandler(invokeFacadeMethod.getService() + "_" + objectNames[i]);

            if (methodInvocationHandler2 == null) {
                return new NotPublished();
            }

            //TODO a decent ref number for main?
            if (beanImpls[i] == null) {
                refs[i] = null;
            } else {
                refs[i] = methodInvocationHandler2.getOrMakeReferenceIDForBean(beanImpls[i]);

                Session sess = (Session) sessions.get(invokeFacadeMethod.getSessionID());

                sess.addBeanInUse(refs[i], beanImpls[i]);
            }
        }

        return new FacadeArrayMethodInvoked(refs, objectNames);
    }

    /**
     * Do a method facade request, returning things other that an array
     *
     * @param beanImpl           The returned object to process.
     * @param invokeFacadeMethod The request
     * @return The reply
     */
    private AbstractResponse doMethodFacadeRequestNonArray(Object beanImpl, InvokeFacadeMethod invokeFacadeMethod) {

        if (!sessionExists(invokeFacadeMethod.getSessionID())) {
            return new NoSuchSession(invokeFacadeMethod.getSessionID());
        }

        MethodInvocationHandler mainMethodInvocationHandler = getMethodInvocationHandler(invokeFacadeMethod.getService() + "_Main");

        String objectName = MethodNameHelper.encodeClassName(mainMethodInvocationHandler.getMostDerivedType(beanImpl).getName());

        MethodInvocationHandler methodInvocationHandler = getMethodInvocationHandler(invokeFacadeMethod.getService() + "_" + objectName);

        if (methodInvocationHandler == null) {
            return new NotPublished();
        }

        //if( !sessionExists( invokeFacadeMethod.getSession() ) )
        //{
        //    return new ExceptionThrown(
        //        new InvocationException( "TODO - you dirty rat/hacker" ) );
        //}

        //TODO a decent ref number for main?
        Long newRef = methodInvocationHandler.getOrMakeReferenceIDForBean(beanImpl);

        // make sure the bean is not garbage collected.
        Session sess = (Session) sessions.get(invokeFacadeMethod.getSessionID());

        sess.addBeanInUse(newRef, beanImpl);

        //long newRef2 = asih2.getOrMakeReferenceIDForBean(beanImpl);
        return new FacadeMethodInvoked(newRef, objectName);
    }

    /**
     * Do a method request
     *
     * @param invokeMethod The request
     * @return The reply
     */
    private AbstractResponse doMethodRequest(InvokeMethod invokeMethod, Object connectionDetails) {

        if (!sessionExists(invokeMethod.getSessionID()) && (connectionDetails == null || !connectionDetails.equals("callback")))
        {
            return new NoSuchSession(invokeMethod.getSessionID());
        }

        String publishedThing = invokeMethod.getService() + "_" + invokeMethod.getObjectName();

        if (!isPublished(publishedThing)) {
            return new NotPublished();
        }

        MethodInvocationHandler methodInvocationHandler = getMethodInvocationHandler(publishedThing);

        return methodInvocationHandler.handleMethodInvocation(invokeMethod, connectionDetails);
    }

    private AbstractResponse doMethodAsyncRequest(InvokeAsyncMethod methodRequest, Object connectionDetails) {

        if (!sessionExists(methodRequest.getSessionID())) {
            return new NoSuchSession(methodRequest.getSessionID());
        }

        String publishedThing = methodRequest.getService() + "_" + methodRequest.getObjectName();

        if (!isPublished(publishedThing)) {
            return new NotPublished();
        }

        MethodInvocationHandler methodInvocationHandler = getMethodInvocationHandler(publishedThing);

        GroupedMethodRequest[] requests = methodRequest.getGroupedRequests();
        for (int i = 0; i < requests.length; i++) {
            GroupedMethodRequest rawRequest = requests[i];
            methodInvocationHandler.handleMethodInvocation(new InvokeMethod(methodRequest.getService(), methodRequest.getObjectName(), rawRequest.getMethodSignature(), rawRequest.getArgs(), methodRequest.getReferenceID(), methodRequest.getSessionID()), connectionDetails);
        }

        return new SimpleMethodInvoked();

    }


    /**
     * DO a lokkup request
     *
     * @param request The request
     * @return The reply
     */
    private AbstractResponse doLookupRequest(AbstractRequest request) {
        LookupService lr = (LookupService) request;
        String publishedServiceName = lr.getService();

        try {
            authenticator.checkAuthority(lr.getAuthentication(), publishedServiceName);
        } catch (AuthenticationException aae) {
            return new ExceptionThrown(aae);
        }

        if (!isPublished(publishedServiceName + "_Main")) {
            return new NotPublished();
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
    private AbstractResponse doClassRequest(AbstractRequest request) {
        RetrieveClass cr = (RetrieveClass) request;
        String publishedThing = cr.getService() + "_" + cr.getObjectName();

        try {
            return new StubClass(stubRetriever.getStubClassBytes(publishedThing));
        } catch (StubRetrievalException e) {
            return new StubRetrievalFailed(e.getMessage());
        }
    }

    /**
     * Do an OpenConnection request
     *
     * @return The reply.
     */
    private AbstractResponse doOpenConnectionRequest(UID machineID) {
        if (machineID != null && machineID.equals(U_ID)) {
            return new SameVMResponse();
        } else {
            Long session = getNewSession();
            sessions.put(session, new Session(session));
            String textToSign = authenticator == null ? "" : authenticator.getTextToSign();
            return new ConnectionOpened(textToSign, session);
        }
    }

    private AbstractResponse doCloseConnectionRequest(Long sessionID) {
        if (!sessions.containsKey(sessionID)) {
            return new NoSuchSession(sessionID);
        } else {
            sessions.remove(sessionID);
            return new ConnectionClosed(sessionID);
        }
    }


    /**
     * Do a ListServices
     *
     * @return The reply
     */
    private AbstractResponse doServiceListRequest() {
        Iterator iterator = getIteratorOfServices();
        Vector vecOfServices = new Vector();

        while (iterator.hasNext()) {
            final String item = (String) iterator.next();

            if (item.endsWith("_Main")) {
                vecOfServices.add(item.substring(0, item.lastIndexOf("_Main")));
            }
        }

        String[] listOfServices = new String[vecOfServices.size()];

        System.arraycopy(vecOfServices.toArray(), 0, listOfServices, 0, vecOfServices.size());

        return new ServicesList(listOfServices);
    }

    /**
     * Do a GarbageCollection AbstractRequest
     *
     * @param request The request
     * @return The reply
     */
    private AbstractResponse doGarbageCollectionRequest(AbstractRequest request) {
        CollectGarbage gcr = (CollectGarbage) request;
        String publishedThing = gcr.getService() + "_" + gcr.getObjectName();

        if (!isPublished(publishedThing)) {
            return new NotPublished();
        }

        Long sessionID = gcr.getSessionID();
        if (!sessionExists(sessionID)) {
            return new InvocationExceptionThrown("TODO - you dirty rat/hacker");
        }

        Session sess = (Session) sessions.get(sessionID);
        if (sess == null) {
            return new ConnectionClosed(sessionID);
        } else {
            if (gcr.getReferenceID() == null) {
                System.err.println("DEBUG- GC on missing referenceID -" + gcr.getReferenceID());
            } else {
                sess.removeBeanInUse(gcr.getReferenceID());
            }
        }

        MethodInvocationHandler methodInvocationHandler = getMethodInvocationHandler(publishedThing);

        return new GarbageCollected();
    }

    /**
     * Do a ListMethods AbstractRequest
     *
     * @param request The request
     * @return The reply
     */
    private AbstractResponse doListMethodsRequest(AbstractRequest request) {
        ListInvokableMethods lReq = (ListInvokableMethods) request;
        String publishedThing = lReq.getService() + "_Main";

        if (!isPublished(publishedThing)) {
            //Should it throw an exception back?
            return new InvokableMethods(new String[0]);
        }

        DefaultMethodInvocationHandler methodInvocationHandler = (DefaultMethodInvocationHandler) getMethodInvocationHandler(publishedThing);

        return new InvokableMethods(methodInvocationHandler.getListOfMethods());
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

    public synchronized ServerMonitor getServerMonitor() {
        return serverMonitor;
    }

}
