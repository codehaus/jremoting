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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.server.ServiceHandler;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.PublicationItem;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.Publisher;
import org.codehaus.jremoting.server.ServiceHandlerAccessor;
import org.codehaus.jremoting.server.transports.DefaultServiceHandler;
import org.codehaus.jremoting.util.StaticStubHelper;
import org.codehaus.jremoting.util.MethodNameHelper;

/**
 * Class PublicationAdapter
 *
 * @author Paul Hammant
 */
public class PublicationAdapter implements ServiceHandlerAccessor {

    private final Publisher publicationDelegate;

    /**
     * A map of published objects.
     */
    private Map<String, ServiceHandler> services = new HashMap<String, ServiceHandler>();


    public PublicationAdapter(Publisher delegate) {
        this.publicationDelegate = delegate;
    }

    /**
     * Is the service published
     *
     * @param service The service name
     * @return true if published.
     */
    public boolean isPublished(String service) {
        return this.services.containsKey(service);
    }

    public Class getFacadeClass(String service) {
        return this.services.get(service).getFacadeClass();
    }

    public String[] getAdditionalFacades(String service) {
        return this.services.get(service).getAdditionalFacades();
    }

    /**
     * Get an iterator of published objects
     *
     * @return The iterator
     */
    public String[] getPublishedServices() {
        Iterator iterator = services.keySet().iterator();
        Vector<String> vecOfServices = new Vector<String>();

        while (iterator.hasNext()) {
            final String item = (String) iterator.next();

            if (StaticStubHelper.isService(item)) {
                vecOfServices.add(StaticStubHelper.getServiceName(item));
            }
        }

        String[] listOfServices = new String[vecOfServices.size()];

        System.arraycopy(vecOfServices.toArray(), 0, listOfServices, 0, vecOfServices.size());
        return listOfServices;

    }

    /**
     * Publish an Object
     *
     * @param impl              The implementaion to publish
     * @param service            as this name.
     * @param primaryFacade The interface to expose.
     * @throws org.codehaus.jremoting.server.PublicationException
     *          if a problem during publication.
     */
    public void publish(Object impl, String service, Class primaryFacade) throws PublicationException {
        publish(impl, service, new Publication(primaryFacade));
        if (publicationDelegate != null) {
            publicationDelegate.publish(impl, service, primaryFacade);
        }
    }

    /**
     * Publish an object
     *
     * @param impl                   The implementaion to publish
     * @param service                 as this name.
     * @param publicationDescription a description of the publication.
     * @throws PublicationException if a problem during publication.
     */
    public void publish(Object impl, String service, Publication publicationDescription) throws PublicationException {

        PublicationItem primaryFacade = publicationDescription.getPrimaryFacade();
        PublicationItem[] secondaryFacades = publicationDescription.getAdditionalFacades();

        if (services.containsKey(StaticStubHelper.formatServiceName(service))) {
            throw new PublicationException("Service '" + service + "' already published");
        }

        String interfaceName = primaryFacade.getFacadeClass().getName();

        // add method maps for main lookup-able service.
        Map<String, Method> mainMethodMap = new HashMap<String, Method>();
        DefaultServiceHandler mainServiceHandler = new DefaultServiceHandler(this, service + "_Main", mainMethodMap, publicationDescription, primaryFacade.getFacadeClass());

        mainServiceHandler.addInstance(new Long(0), impl);

        Class clazz = primaryFacade.getFacadeClass();

        Method methods[] = null;
        try {
            Method ts = Object.class.getMethod("toString", new Class[0]);
            Method hc = Object.class.getMethod("hashCode", new Class[0]);
            Method eq = Object.class.getMethod("equals", Object.class);
            Method[] interfaceMethods = clazz.getMethods();
            methods = new Method[interfaceMethods.length + 3];
            System.arraycopy(interfaceMethods, 0, methods, 0, interfaceMethods.length);
            methods[interfaceMethods.length] = ts;
            methods[interfaceMethods.length + 1] = hc;
            methods[interfaceMethods.length + 2] = eq;
        } catch (NoSuchMethodException e) {
            // never!
        }

        for (Method method : methods) {
            String methodSignature = MethodNameHelper.getMethodSignature(method);

            if (!mainMethodMap.containsKey(methodSignature)) {
                mainMethodMap.put(methodSignature, method);
            }
        }

        // as the main service is lookup-able, it has a prexisting impl.
        services.put(StaticStubHelper.formatServiceName(service), mainServiceHandler);

        // add method maps for all the additional facades.
        for (PublicationItem secondaryFacade : secondaryFacades) {
            Class facadeClass = secondaryFacade.getFacadeClass();
            String encodedClassName = MethodNameHelper.encodeClassName(secondaryFacade.getFacadeClass().getName());
            HashMap<String, Method> methodMap = new HashMap<String, Method>();
            ServiceHandler serviceHandler = new DefaultServiceHandler(this, service + "_" + encodedClassName,
                    methodMap, publicationDescription, facadeClass);

            methods = null;
            try {
                Method ts = Object.class.getMethod("toString", new Class[0]);
                Method hc = Object.class.getMethod("hashCode", new Class[0]);
                Method eq = Object.class.getMethod("equals", new Class[]{Object.class});
                Method[] interfaceMethods = facadeClass.getMethods();
                methods = new Method[interfaceMethods.length + 3];
                System.arraycopy(interfaceMethods, 0, methods, 0, interfaceMethods.length);
                methods[interfaceMethods.length] = ts;
                methods[interfaceMethods.length + 1] = hc;
                methods[interfaceMethods.length + 2] = eq;
            } catch (NoSuchMethodException e) {
                // never!
            }


            for (Method method : methods) {
                String methodSignature = MethodNameHelper.getMethodSignature(method);

                if (!methodMap.containsKey(methodSignature)) {
                    methodMap.put(methodSignature, method);
                }
            }

            services.put(service + "_" + encodedClassName, serviceHandler);
        }

        if (publicationDelegate != null) {
            publicationDelegate.publish(impl, service, publicationDescription);
        }

    }

    /**
     * UnPublish an object
     *
     * @param impl          the object to unpublish
     * @param service The name it was published as
     * @throws PublicationException if a problem during publication.
     */
    public void unPublish(Object impl, String service) throws PublicationException {

        String serviceName = StaticStubHelper.formatServiceName(service);
        if (!services.containsKey(serviceName)) {
            throw new PublicationException("Service '" + service + "' not published");
        }
        services.remove(serviceName);
        if (publicationDelegate != null) {
            publicationDelegate.unPublish(impl, service);
        }
    }

    /**
     * Replace a published impl
     *
     * @param oldImpl       the old published object
     * @param service The name it was published as
     * @param withImpl      The new published impl
     * @throws PublicationException if a problem during publication.
     */
    public void replacePublished(Object oldImpl, String service, Object withImpl) throws PublicationException {

        String serviceName = StaticStubHelper.formatServiceName(service);
        if (!services.containsKey(serviceName)) {
            throw new PublicationException("Service '" + service + "' not published");
        }

        ServiceHandler serviceHandler = services.get(serviceName);

        serviceHandler.replaceInstance(oldImpl, withImpl);
        if (publicationDelegate != null) {
            publicationDelegate.replacePublished(oldImpl, service, withImpl);
        }
    }

    /**
     * Get a method's InvocationHandler
     *
     * @param service The name of a published object
     * @return the method invoation handler
     */
    public ServiceHandler getServiceHandler(String service) {
        return services.get(service);
    }
}
