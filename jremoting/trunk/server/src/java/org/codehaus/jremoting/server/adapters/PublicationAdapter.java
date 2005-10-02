/* ====================================================================
 * Copyright 2005 JRemoting Committers
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

import org.codehaus.jremoting.commands.MethodRequest;
import org.codehaus.jremoting.api.MethodNameHelper;
import org.codehaus.jremoting.server.MethodInvocationHandler;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.PublicationDescriptionItem;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.Publisher;
import org.codehaus.jremoting.server.transports.DefaultMethodInvocationHandler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class PublicationAdapter
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class PublicationAdapter implements Publisher {

    /**
     * A map of published objects.
     */
    protected HashMap publishedObjects = new HashMap();

    /**
     * Is the object published
     *
     * @param publishedObjectName The published object name
     * @return true if published.
     */
    public boolean isPublished(String publishedObjectName) {
        return publishedObjects.containsKey(publishedObjectName);
    }

    /**
     * Get an iterator of published objects
     *
     * @return The iterator
     */
    public Iterator getIteratorOfPublishedObjects() {
        return publishedObjects.keySet().iterator();
    }

    /**
     * Publish an Object
     *
     * @param impl              The implementaion to publish
     * @param asName            as this name.
     * @param interfaceToExpose The interface to expose.
     * @throws org.codehaus.jremoting.server.PublicationException
     *          if a problem during publication.
     */
    public void publish(Object impl, String asName, Class interfaceToExpose) throws PublicationException {
        publish(impl, asName, new PublicationDescription(interfaceToExpose));
    }

    /**
     * Publish an object
     *
     * @param impl                   The implementaion to publish
     * @param asName                 as this name.
     * @param publicationDescription a description of the publication.
     * @throws PublicationException if a problem during publication.
     */
    public void publish(Object impl, String asName, PublicationDescription publicationDescription) throws PublicationException {

        PublicationDescriptionItem[] interfacesToExpose = publicationDescription.getInterfacesToExpose();
        PublicationDescriptionItem[] additionalFacades = publicationDescription.getAdditionalFacades();

        if (publishedObjects.containsKey(asName + "_Main")) {
            throw new PublicationException("Service '" + asName + "' already published");
        }

        String[] interfaceNames = new String[interfacesToExpose.length];

        for (int i = 0; i < interfacesToExpose.length; i++) {
            interfaceNames[i] = interfacesToExpose[i].getFacadeClass().getName();
        }

        // add method maps for main lookup-able service.
        HashMap mainMethodMap = new HashMap();
        DefaultMethodInvocationHandler mainMethodInvocationHandler = new DefaultMethodInvocationHandler(this, asName + "_Main", mainMethodMap, publicationDescription);

        mainMethodInvocationHandler.addImplementationBean(new Long(0), impl);

        for (int x = 0; x < interfacesToExpose.length; x++) {
            Class clazz = interfacesToExpose[x].getFacadeClass();

            Method methods[] = null;
            try {
                Method ts = Object.class.getMethod("toString", new Class[0]);
                Method hc = Object.class.getMethod("hashCode", new Class[0]);
                Method eq = Object.class.getMethod("equals", new Class[]{Object.class});
                Method[] interfaceMethods = clazz.getMethods();
                methods = new Method[interfaceMethods.length + 3];
                System.arraycopy(interfaceMethods, 0, methods, 0, interfaceMethods.length);
                methods[interfaceMethods.length] = ts;
                methods[interfaceMethods.length + 1] = hc;
                methods[interfaceMethods.length + 2] = eq;
            } catch (NoSuchMethodException e) {
                // never!
            }

            for (int y = 0; y < methods.length; y++) {
                Method method = methods[y];
                String methodSignature = MethodNameHelper.getMethodSignature(method);

                if (!mainMethodMap.containsKey(methodSignature.toString())) {
                    mainMethodMap.put(methodSignature.toString(), methods[y]);
                }
            }
        }

        // as the main service is lookup-able, it has a prexisting impl.
        publishedObjects.put(asName + "_Main", mainMethodInvocationHandler);

        // add method maps for all the additional facades.
        for (int x = 0; x < additionalFacades.length; x++) {
            Class facadeClass = additionalFacades[x].getFacadeClass();
            String encodedClassName = MethodNameHelper.encodeClassName(additionalFacades[x].getFacadeClass().getName());
            HashMap methodMap = new HashMap();
            MethodInvocationHandler methodInvocationHandler = new DefaultMethodInvocationHandler(this, asName + "_" + encodedClassName, methodMap, publicationDescription);

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


            for (int y = 0; y < methods.length; y++) {
                Method method = methods[y];
                String methodSignature = MethodNameHelper.getMethodSignature(method);

                if (!methodMap.containsKey(methodSignature.toString())) {
                    methodMap.put(methodSignature.toString(), methods[y]);
                }
            }

            publishedObjects.put(asName + "_" + encodedClassName, methodInvocationHandler);
        }
    }

    /**
     * UnPublish an object
     *
     * @param impl          the object to unpublish
     * @param publishedName The name it was published as
     * @throws PublicationException if a problem during publication.
     */
    public void unPublish(Object impl, String publishedName) throws PublicationException {

        if (!publishedObjects.containsKey(publishedName + "_Main")) {
            throw new PublicationException("Service '" + publishedName + "' not published");
        }

        publishedObjects.remove(publishedName + "_Main");
    }

    /**
     * Replace a published impl
     *
     * @param oldImpl       the old published object
     * @param publishedName The name it was published as
     * @param withImpl      The new published impl
     * @throws PublicationException if a problem during publication.
     */
    public void replacePublished(Object oldImpl, String publishedName, Object withImpl) throws PublicationException {

        if (!publishedObjects.containsKey(publishedName + "_Main")) {
            throw new PublicationException("Service '" + publishedName + "' not published");
        }

        MethodInvocationHandler asih = (MethodInvocationHandler) publishedObjects.get(publishedName + "_Main");

        asih.replaceImplementationBean(oldImpl, withImpl);
    }

    /**
     * Get a Server's  InvocationHandler
     *
     * @param methodRequest The method Request.
     * @param objectName    The object name.
     * @return the method invoation handler
     */
    public MethodInvocationHandler getMethodInvocationHandler(MethodRequest methodRequest, String objectName) {
        return (MethodInvocationHandler) publishedObjects.get(methodRequest.getPublishedServiceName() + "_" + objectName);
    }

    /**
     * Get a method's InvocationHandler
     *
     * @param publishedName The name of a published object
     * @return the method invoation handler
     */
    public MethodInvocationHandler getMethodInvocationHandler(String publishedName) {
        return (MethodInvocationHandler) publishedObjects.get(publishedName);
    }
}
