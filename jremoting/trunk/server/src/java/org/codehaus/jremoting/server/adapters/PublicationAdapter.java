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

import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.server.MethodInvoker;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.PublicationDescriptionItem;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.Publisher;
import org.codehaus.jremoting.server.transports.DefaultMethodInvoker;
import org.codehaus.jremoting.util.StubHelper;
import org.codehaus.jremoting.util.MethodNameHelper;

/**
 * Class PublicationAdapter
 *
 * @author Paul Hammant
 */
public class PublicationAdapter implements Publisher {

    /**
     * A map of published objects.
     */
    private Map<String, MethodInvoker> services = new HashMap<String, MethodInvoker>();

    /**
     * Is the service published
     *
     * @param service The service name
     * @return true if published.
     */
    public boolean isPublished(String service) {
        return this.services.containsKey(service);
    }

    /**
     * Get an iterator of published objects
     *
     * @return The iterator
     */
    public Iterator<String> getIteratorOfServices() {
        return services.keySet().iterator();
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
        publish(impl, service, new PublicationDescription(primaryFacade));
    }

    /**
     * Publish an object
     *
     * @param impl                   The implementaion to publish
     * @param service                 as this name.
     * @param publicationDescription a description of the publication.
     * @throws PublicationException if a problem during publication.
     */
    public void publish(Object impl, String service, PublicationDescription publicationDescription) throws PublicationException {

        PublicationDescriptionItem[] primaryFacades = publicationDescription.getPrimaryFacades();
        PublicationDescriptionItem[] additionalFacades = publicationDescription.getAdditionalFacades();

        if (services.containsKey(StubHelper.formatServiceName(service))) {
            throw new PublicationException("Service '" + service + "' already published");
        }

        String[] interfaceNames = new String[primaryFacades.length];

        for (int i = 0; i < primaryFacades.length; i++) {
            interfaceNames[i] = primaryFacades[i].getFacadeClass().getName();
        }

        // add method maps for main lookup-able service.
        Map<String, Method> mainMethodMap = new HashMap<String, Method>();
        DefaultMethodInvoker mainMethodInvoker = new DefaultMethodInvoker(this, service + "_Main", mainMethodMap, publicationDescription, primaryFacades[0].getFacadeClass());

        mainMethodInvoker.addImplementationBean(new Long(0), impl);

        for (PublicationDescriptionItem primaryFacade : primaryFacades) {
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
        }

        // as the main service is lookup-able, it has a prexisting impl.
        services.put(StubHelper.formatServiceName(service), mainMethodInvoker);

        // add method maps for all the additional facades.
        for (PublicationDescriptionItem additionalFacade : additionalFacades) {
            Class facadeClass = additionalFacade.getFacadeClass();
            String encodedClassName = MethodNameHelper.encodeClassName(additionalFacade.getFacadeClass().getName());
            HashMap<String, Method> methodMap = new HashMap<String, Method>();
            MethodInvoker methodInvoker = new DefaultMethodInvoker(this, service + "_" + encodedClassName, 
                    methodMap, publicationDescription, facadeClass);

            Method methods[] = null;
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

            services.put(service + "_" + encodedClassName, methodInvoker);
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

        String serviceName = StubHelper.formatServiceName(service);
        if (!services.containsKey(serviceName)) {
            throw new PublicationException("Service '" + service + "' not published");
        }
        services.remove(serviceName);
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

        String serviceName = StubHelper.formatServiceName(service);
        if (!services.containsKey(serviceName)) {
            throw new PublicationException("Service '" + service + "' not published");
        }

        MethodInvoker asih = services.get(serviceName);

        asih.replaceImplementationBean(oldImpl, withImpl);
    }

    /**
     * Get a Server's  MethodInvoker
     *
     * @param invokeMethod The method Request.
     * @param objectName   The object name.
     * @return the method invoation handler
     */
    public MethodInvoker getMethodInvoker(InvokeMethod invokeMethod, String objectName) {
        return services.get(invokeMethod.getService() + "_" + objectName);
    }

    public Class getFacadeClass(String publishedThing) {
        return services.get(publishedThing).getFacadeClass();
    }

    /**
     * Get a method's InvocationHandler
     *
     * @param service The name of a published object
     * @return the method invoation handler
     */
    public MethodInvoker getMethodInvoker(String service) {
        return services.get(service);
    }
}
