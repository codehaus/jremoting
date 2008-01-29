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
package org.codehaus.jremoting.server.transports;

import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.responses.BadServerSideEvent;
import org.codehaus.jremoting.responses.ExceptionThrown;
import org.codehaus.jremoting.responses.MethodInvoked;
import org.codehaus.jremoting.responses.NoSuchReference;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.MethodInvocationMonitor;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.Publisher;
import org.codehaus.jremoting.server.monitors.NullMethodInvocationMonitor;
import org.codehaus.jremoting.util.FacadeRefHolder;
import org.codehaus.jremoting.util.MethodNameHelper;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Class ServiceHandler
 *
 * @author Paul Hammant
 * @author Vinay Chandrasekharan <a href="mailto:vinayc77@yahoo.com">vinayc77@yahoo.com</a>
 * @version $Revision: 1.2 $
 */
public class ServiceHandler {

    private WeakHashMap<Long, WeakReference<Object>> instancesByRefID = new WeakHashMap<Long, WeakReference<Object>>();
    private WeakHashMap<Object, Long> ReferencesForInstances = new WeakHashMap<Object, Long>();
    private Map<String, Method> methodMap = new HashMap<String, Method>();
    private static int c_nextReference = 0;
    private String publishedThing;
    private Object mainInstance;
    private final Publication publicationDescription;
    private final Class facadeClass;
    private MethodInvocationMonitor methodInvocationMonitor = new NullMethodInvocationMonitor();
    private Publisher publisher;

    private final Long zero = new Long(0);

    public ServiceHandler(Publisher publisher,
            String publishedThing, Publication publicationDescription, Class facadeClass) {
        this.publisher = publisher;
        this.publishedThing = publishedThing;
        this.publicationDescription = publicationDescription;
        this.facadeClass = facadeClass;
        populateMethods();
    }

    private void populateMethods() {
        Method[] methods = null;
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
    }


    public String toString() {
        return "ServiceHandler:" + publishedThing;
    }

    public void addInstance(Long reference, Object instance) {

        if (reference.equals(zero)) {
            this.mainInstance = instance;
        }

        instancesByRefID.put(reference, new WeakReference(instance));
        ReferencesForInstances.put(instance, reference);
    }

    public void replaceInstance(Object oldInstance, Object withInstance) {

        Long ref = ReferencesForInstances.get(oldInstance);

        instancesByRefID.put(ref, new WeakReference<Object>(withInstance));
        ReferencesForInstances.remove(oldInstance);
        ReferencesForInstances.put(withInstance, ref);

        if (mainInstance == oldInstance) {
            mainInstance = withInstance;
        }
    }

    public Long getOrMakeReferenceIDForInstance(Object instance) {

        Long ref = ReferencesForInstances.get(instance);

        if (ref == null) {
            ref = getNewReference();
            addInstance(ref, instance);
        }

        return ref;
    }

    public Response handleMethodInvocation(InvokeMethod request, Object connectionDetails) {

        String methodSignature = request.getMethodSignature();

        if (!methodMap.containsKey(methodSignature)) {

            methodInvocationMonitor.missingMethod(methodSignature, connectionDetails);
            return new BadServerSideEvent("Method '" + methodSignature + "' not present in impl");
        }

        Method method = methodMap.get(methodSignature);

        Object instance = null;

        try {
            WeakReference wr = instancesByRefID.get(request.getReference());

            if (wr == null) {
                methodInvocationMonitor.invalidReference(methodSignature, connectionDetails);
                return new NoSuchReference(request.getReference());
            }

            instance = wr.get();

            if (instance == null) {
                methodInvocationMonitor.invalidReference(methodSignature, connectionDetails);
                return new NoSuchReference(request.getReference());
            }

            replaceSecondaryFacadesInArgList(request.getArgs());
            methodInvocationMonitor.methodInvoked(instance.getClass(), methodSignature, connectionDetails);
            return new MethodInvoked(method.invoke(instance, request.getArgs()));
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();

            methodInvocationMonitor.invocationTargetException(instance.getClass(), methodSignature, ite, connectionDetails);
            if (t instanceof Serializable) {

                // NOTE Sever side stack traces will appear on the client side
                return new ExceptionThrown(t);
            } else {
                return new BadServerSideEvent("Exception was not serializable :" + t.getClass().getName());
            }
        } catch (Throwable t) {
            methodInvocationMonitor.invocationException(instance == null ? null : instance.getClass(), methodSignature, t, connectionDetails);
            t.printStackTrace();
            return new BadServerSideEvent("Some ServerSide exception problem :" + t.getClass().getName() + " message:" + t.getMessage());
        }
    }

    private void replaceSecondaryFacadesInArgList(Object[] args) {

        for (int i = 0; i < args.length; i++) {

            // TODO find a faster way to do this....
            if (args[i] instanceof FacadeRefHolder) {
                FacadeRefHolder frh = (FacadeRefHolder) args[i];
                args[i] = publisher.getInstanceForReference(frh.getObjectName(), frh.getReference());
            }
        }
    }

    public Class getMostDerivedType(Object instance) {
        return publicationDescription.getMostDerivedType(instance);
    }

    public Class getFacadeClass() {
        return facadeClass;
    }

    public String[] getAdditionalFacades() {
        return publicationDescription.getAdditionalFacadeNames();
    }


    public Object getInstanceForReference(Long reference) {
        return instancesByRefID.get(reference).get();
    }


    public String encodeClassName(String className) {
        return className.replace('.', '$');
    }

    public String[] getListOfMethods() {
        String[] methodNames = (String[]) methodMap.keySet().toArray(new String[0]);
        return methodNames;
    }

    private Long getNewReference() {
        // approve everything and set session identifier.
        return new Long((++c_nextReference << 16) + ((long) (Math.random() * 65536)));
    }

    public void setMethodInvocationMonitor(MethodInvocationMonitor monitor) {
        methodInvocationMonitor = monitor;
    }

}
