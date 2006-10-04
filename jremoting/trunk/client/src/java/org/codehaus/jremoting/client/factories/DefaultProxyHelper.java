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
package org.codehaus.jremoting.client.factories;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.codehaus.jremoting.client.*;
import org.codehaus.jremoting.requests.ServiceRequest;
import org.codehaus.jremoting.requests.CollectGarbage;
import org.codehaus.jremoting.requests.GroupedMethodRequest;
import org.codehaus.jremoting.requests.InvokeAsyncMethod;
import org.codehaus.jremoting.requests.InvokeFacadeMethod;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.responses.ConnectionClosed;
import org.codehaus.jremoting.responses.ExceptionThrown;
import org.codehaus.jremoting.responses.FacadeArrayMethodInvoked;
import org.codehaus.jremoting.responses.FacadeMethodInvoked;
import org.codehaus.jremoting.responses.GarbageCollected;
import org.codehaus.jremoting.responses.InvocationExceptionThrown;
import org.codehaus.jremoting.responses.NoSuchReference;
import org.codehaus.jremoting.responses.NoSuchSession;
import org.codehaus.jremoting.responses.SimpleMethodInvoked;
import org.codehaus.jremoting.ConnectionException;

/**
 * Class DefaultProxyHelper
 *
 * @author Paul Hammant
 * @author Vinay Chandrasekharan <a href="mailto:vinayc77@yahoo.com">vinayc77@yahoo.com</a>
 * @version $Revision: 1.3 $
 */
public final class DefaultProxyHelper implements ProxyHelper {

    private final transient ProxyRegistry proxyRegistry;
    private final transient ClientInvocationHandler clientInvocationHandler;
    private final transient String publishedServiceName;
    private final transient String objectName;
    private final transient Long referenceID;
    private final transient Long session;
    private ContextFactory contextFactory;
    private ArrayList<GroupedMethodRequest> queuedAsyncRequests = new ArrayList<GroupedMethodRequest>();

    public DefaultProxyHelper(ProxyRegistry proxyRegistry, ClientInvocationHandler clientInvocationHandler,
                              ContextFactory contextFactory, String pubishedServiceName, String objectName,
                              Long referenceID, Long session) {
        this.contextFactory = contextFactory;

        this.proxyRegistry = proxyRegistry;
        this.clientInvocationHandler = clientInvocationHandler;
        publishedServiceName = pubishedServiceName;
        this.objectName = objectName;
        this.referenceID = referenceID;
        this.session = session;
        if (proxyRegistry == null) {
            throw new IllegalArgumentException("proxyRegistry cannot be null");
        }
        if (clientInvocationHandler == null) {
            throw new IllegalArgumentException("clientInvocationHandler cannot be null");
        }

    }

    public void registerImplObject(Object implBean) {
        proxyRegistry.registerReferenceObject(implBean, referenceID);
    }

    public Object processObjectRequestGettingFacade(Class returnClassType, String methodSignature, Object[] args, String objectName) throws Throwable {
        try {
            Object result;

            InvokeFacadeMethod request;

            if (objectName.endsWith("[]")) {
                request = new InvokeFacadeMethod(publishedServiceName, this.objectName, methodSignature, args, referenceID, objectName.substring(0, objectName.length() - 2), session);
            } else {
                request = new InvokeFacadeMethod(publishedServiceName, this.objectName, methodSignature, args, referenceID, objectName, session);
            }

            setContext(request);
            Response response = clientInvocationHandler.handleInvocation(request);

            if (response instanceof FacadeMethodInvoked) {
                result = facadeMethodInvoked(response);
            } else if (response instanceof FacadeArrayMethodInvoked) {
                result = facadeArrayMethodInvoked(response, returnClassType);
            } else {
                throw makeUnexpectedResponseThrowable(response);
            }
            return result;
        } catch (InvocationException ie) {
            clientInvocationHandler.getClientMonitor().invocationFailure(this.getClass(), publishedServiceName, objectName, methodSignature, ie);
            throw ie;
        }
    }

    private Object facadeArrayMethodInvoked(Response response, Class returnClassType) {
        FacadeArrayMethodInvoked mfar = (FacadeArrayMethodInvoked) response;
        Long[] refs = mfar.getReferenceIDs();
        String[] objectNames = mfar.getObjectNames();
        Object[] implBeans = (Object[]) Array.newInstance(returnClassType, refs.length);

        for (int i = 0; i < refs.length; i++) {
            Long ref = refs[i];

            if (ref == null) {
                implBeans[i] = null;
            } else {
                Object o = proxyRegistry.getImplObj(ref);

                implBeans[i] = o;

                if (implBeans[i] == null) {
                    DefaultProxyHelper bo2 = new DefaultProxyHelper(proxyRegistry, clientInvocationHandler,
                            contextFactory, publishedServiceName, objectNames[i], refs[i], session);
                    Object retFacade = null;

                    try {
                        retFacade = proxyRegistry.getInstance(publishedServiceName, objectNames[i], bo2);
                    } catch (Exception e) {
                        System.out.println("objNameWithoutArray=" + returnClassType.getName());
                        System.out.flush();
                        e.printStackTrace();
                    }

                    bo2.registerImplObject(retFacade);

                    implBeans[i] = retFacade;
                }
            }
        }

        return implBeans;
    }

    private Object facadeMethodInvoked(Response response) throws ConnectionException {
        FacadeMethodInvoked mfr = (FacadeMethodInvoked) response;
        Long ref = mfr.getReferenceID();

        // it might be that the return value was intended to be null.
        if (ref == null) {
            return null;
        }

        Object implBean = proxyRegistry.getImplObj(ref);

        if (implBean == null) {
            DefaultProxyHelper pHelper = new DefaultProxyHelper(proxyRegistry, clientInvocationHandler,
                    contextFactory, publishedServiceName, mfr.getObjectName(), ref, session);
            Object retFacade = proxyRegistry.getInstance(publishedServiceName, mfr.getObjectName(), pHelper);

            pHelper.registerImplObject(retFacade);

            return retFacade;
        } else {
            return implBean;
        }
    }

    public Object processObjectRequest(String methodSignature, Object[] args, Class[] argClasses) throws Throwable {

        try {
            proxyRegistry.marshallCorrection(publishedServiceName, methodSignature, args, argClasses);

            InvokeMethod request = new InvokeMethod(publishedServiceName, objectName, methodSignature, args, referenceID, session);
            setContext(request);
            Response response = clientInvocationHandler.handleInvocation(request);

            if (response instanceof SimpleMethodInvoked) {
                SimpleMethodInvoked or = (SimpleMethodInvoked) response;

                return or.getResponseObject();
            } else {
                throw makeUnexpectedResponseThrowable(response);
            }
        } catch (InvocationException ie) {
            clientInvocationHandler.getClientMonitor().invocationFailure(this.getClass(), publishedServiceName, objectName, methodSignature, ie);
            throw ie;
        }
    }


    public void processVoidRequest(String methodSignature, Object[] args, Class[] argClasses) throws Throwable {

        try {
            proxyRegistry.marshallCorrection(publishedServiceName, methodSignature, args, argClasses);

            InvokeMethod request = new InvokeMethod(publishedServiceName, objectName, methodSignature, args, referenceID, session);

            //debug(args);
            setContext(request);
            Response response = clientInvocationHandler.handleInvocation(request);

            if (response instanceof SimpleMethodInvoked) {
                SimpleMethodInvoked or = (SimpleMethodInvoked) response;

                return;
            } else {
                throw makeUnexpectedResponseThrowable(response);
            }
        } catch (InvocationException ie) {
            clientInvocationHandler.getClientMonitor().invocationFailure(this.getClass(), publishedServiceName, objectName, methodSignature, ie);
            throw ie;
        }
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
                InvokeAsyncMethod request = new InvokeAsyncMethod(publishedServiceName, objectName, rawRequests, referenceID, session);

                //debug(args);
                setContext(request);
                Response response = clientInvocationHandler.handleInvocation(request);

                if (response instanceof SimpleMethodInvoked) {
                    SimpleMethodInvoked or = (SimpleMethodInvoked) response;
                    return;
                } else {
                    throw makeUnexpectedResponseThrowable(response);
                }
            } catch (InvocationException ie) {
                clientInvocationHandler.getClientMonitor().invocationFailure(this.getClass(), publishedServiceName, objectName, "<various-async-grouped>", ie);
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
            if (args[i] instanceof Proxy) {

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
            return new NoSuchSessionException(nssr.getSessionID());
        }
        //TODO remove some of these if clover indicates they are not used?
        else if (response instanceof NoSuchReference) {
            NoSuchReference nsrr = (NoSuchReference) response;
            return new NoSuchReferenceException(nsrr.getReferenceID());
        } else if (response instanceof InvocationExceptionThrown) {
            InvocationExceptionThrown ier = (InvocationExceptionThrown) response;
            return new InvocationException(ier.getMessage());
        } else {
            return new InvocationException("Internal Error : Unknown response type :" + response.getClass().getName());
        }
    }

    public Long getReferenceID(Object proxyRegistry) {

        // this checks the factory because reference IDs should not be
        // given out to any requester.  It should be privileged information.
        if (proxyRegistry == proxyRegistry) {
            return referenceID;
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
            return ((Boolean) retVal).booleanValue();
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

    protected void finalize() throws Throwable {

        synchronized (proxyRegistry) {
            Response response = clientInvocationHandler.handleInvocation(new CollectGarbage(publishedServiceName, objectName, session, referenceID));
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

    private synchronized void setContext(ServiceRequest request) {

        if (contextFactory == null) {
            contextFactory = new SimpleContextFactory();
        }
        request.setContext(contextFactory.getClientContext());

    }

}
