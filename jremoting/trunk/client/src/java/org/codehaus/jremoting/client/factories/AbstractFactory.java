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

import org.codehaus.jremoting.Authentication;
import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.api.FacadeRefHolder;
import org.codehaus.jremoting.client.ClientInvocationHandler;
import org.codehaus.jremoting.client.Factory;
import org.codehaus.jremoting.client.HostContext;
import org.codehaus.jremoting.client.Proxy;
import org.codehaus.jremoting.responses.ExceptionResponse;
import org.codehaus.jremoting.requests.ListPublishedObjects;
import org.codehaus.jremoting.responses.PublishedObjectList;
import org.codehaus.jremoting.requests.LookupPublishedObject;
import org.codehaus.jremoting.responses.LookupResponse;
import org.codehaus.jremoting.requests.OpenConnection;
import org.codehaus.jremoting.responses.*;

import java.lang.ref.WeakReference;
import java.rmi.server.UID;
import java.util.HashMap;


/**
 * Class AbstractFactory
 *
 * @author Paul Hammant
 * @author Peter Royal <a href="mailto:proyal@managingpartners.com">proyal@managingpartners.com</a>
 * @version $Revision: 1.3 $
 */
public abstract class AbstractFactory implements Factory {

    private static final UID U_ID = new UID((short) 20729);
    private static final int STEM_LEN = "JRemotingGenerated".length();
    protected final HostContext hostContext;
    protected ClientInvocationHandler clientInvocationHandler;
    protected final HashMap refObjs = new HashMap();
    private transient String textToSign;
    protected Long session;


    public AbstractFactory(HostContext hostContext, boolean allowOptimize) throws ConnectionException {
        this.hostContext = hostContext;
        clientInvocationHandler = this.hostContext.getInvocationHandler();
        clientInvocationHandler.initialize();

        UID machineID = allowOptimize ? U_ID : null;

        if (!(this.hostContext instanceof AbstractSocketStreamHostContext)) {
            machineID = null;
        }

        Response response = clientInvocationHandler.handleInvocation(new OpenConnection(machineID));

        if (response instanceof ConnectionOpened) {
            textToSign = ((ConnectionOpened) response).getTextToSign();
            session = ((ConnectionOpened) response).getSession();
        } else {

            throw new ConnectionException("Setting of host context blocked for reasons of unknown, server-side response: (" + response.getClass().getName() + ")");
        }

    }

    /**
     * Method lookup
     *
     * @param publishedServiceName
     * @param authentication
     * @return
     * @throws ConnectionException
     */
    public Object lookup(String publishedServiceName, Authentication authentication) throws ConnectionException {

        Response ar = clientInvocationHandler.handleInvocation(new LookupPublishedObject(publishedServiceName, authentication, session));

        if (ar.getResponseCode() >= ResponseConstants.PROBLEMRESPONSE) {
            if (ar instanceof NotPublished) {
                throw new ConnectionException("Service " + publishedServiceName + " not published");
            } else if (ar instanceof ExceptionResponse) {
                ExceptionResponse er = (ExceptionResponse) ar;

                throw (ConnectionException) er.getResponseException();
            } else {
                throw new ConnectionException("Problem doing lookup on service");
            }
        } else if (ar instanceof ExceptionResponse) {
            ExceptionResponse er = (ExceptionResponse) ar;
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
        } else if (!(ar instanceof LookupResponse)) {
            throw new UnsupportedOperationException("Unexpected reply to lookup [reply: " + ar + "]");
        }

        LookupResponse lr = (LookupResponse) ar;
        DefaultProxyHelper baseObj = new DefaultProxyHelper(this, clientInvocationHandler, publishedServiceName, "Main", lr.getReferenceID(), session);
        Object retVal = getInstance(publishedServiceName, "Main", baseObj);

        baseObj.registerImplObject(retVal);

        return retVal;
    }

    protected abstract Class getFacadeClass(String publishedServiceName, String objectName) throws ConnectionException, ClassNotFoundException;

    protected abstract Object getInstance(String publishedServiceName, String objectName, DefaultProxyHelper proxyHelper) throws ConnectionException;

    /**
     * Method registerReferenceObject
     *
     * @param obj
     * @param referenceID
     */
    public final void registerReferenceObject(Object obj, Long referenceID) {

        synchronized (this) {
            refObjs.put(referenceID, new WeakReference(obj));
        }

        //Object o = refObjs.get(referenceID);
    }

    /**
     * Method getReferenceID
     *
     * @param obj
     * @return
     */
    public final Long getReferenceID(Proxy obj) {
        return obj.codehausRemotingGetReferenceID(this);
    }

    /**
     * Method getImplObj
     *
     * @param referenceID
     * @return
     */
    public final Object getImplObj(Long referenceID) {

        WeakReference wr = null;

        synchronized (this) {
            wr = (WeakReference) refObjs.get(referenceID);
        }

        if (wr == null) {
            return null;
        }

        Object obj = wr.get();

        if (obj == null) {
            refObjs.remove(referenceID);
        }

        return obj;
    }

    /**
     * Method lookup
     *
     * @param publishedServiceName
     * @return
     * @throws ConnectionException
     */
    public final Object lookup(String publishedServiceName) throws ConnectionException {
        return lookup(publishedServiceName, null);
    }

    /**
     * Method getTextToSignForAuthentication
     *
     * @return
     */
    public String getTextToSignForAuthentication() {
        return textToSign;
    }

    /**
     * Method list
     */
    public String[] list() {

        Response ar = clientInvocationHandler.handleInvocation(new ListPublishedObjects());

        if (ar instanceof PublishedObjectList) {
            return ((PublishedObjectList) ar).getListOfPublishedObjects();
        } else {
            return new String[]{};
        }
    }


    /**
     * Is the service published.
     *
     * @param publishedServiceName
     * @return
     */
    public boolean hasService(String publishedServiceName) {
        String[] services = list();
        for (int i = 0; i < services.length; i++) {
            String service = services[i];
            if (service.equals(publishedServiceName)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Wraps the reference to the remote obj within the FacadeRefHolder obj.
     *
     * @param obj
     * @param objectName
     * @return
     */
    private FacadeRefHolder makeFacadeRefHolder(Proxy obj, String objectName) {

        Long refID = getReferenceID(obj);

        return new FacadeRefHolder(refID, objectName);
    }


    public void marshallCorrection(String remoteObjName, String methodSignature, Object[] args, Class[] argClasses) {

        for (int i = 0; i < args.length; i++) {
            Class argClass = argClasses[i];
            if (argClass == null) {
                continue;
            }
            //All remote references implement Proxy interface
            if (args[i] instanceof Proxy) {
                Proxy proxy = (Proxy) args[i];

                if (getReferenceID(proxy) != null) {
                    //The stripping "JRemotingGenerated" from the proxy names generated by ProxyGenerator
                    String objName = args[i].getClass().getName().substring(STEM_LEN);

                    args[i] = makeFacadeRefHolder(proxy, objName);
                }
            } else // Let the specific InvocationHandlers be given the last chance to modify the arguments.
            {
                args[i] = clientInvocationHandler.resolveArgument(remoteObjName, methodSignature, argClasses[i], args[i]);
            }
        }
    }

}
