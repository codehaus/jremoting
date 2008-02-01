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

import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.PublicationItem;
import org.codehaus.jremoting.server.Publisher;
import org.codehaus.jremoting.server.servicehandlers.ReflectionServiceHandler;
import org.codehaus.jremoting.server.servicehandlers.ServiceHandler;
import org.codehaus.jremoting.util.MethodNameHelper;
import org.codehaus.jremoting.util.StaticStubHelper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Class PublicationAdapter
 *
 * @author Paul Hammant
 */
public class PublicationAdapter implements Publisher {

    private final Publisher publicationDelegate;

    private Map<String, ServiceHandler> services = new HashMap<String, ServiceHandler>();
    private Map<String, String> redirected = new HashMap<String, String>();

    public PublicationAdapter(Publisher delegate) {
        this.publicationDelegate = delegate;
    }

    public boolean isPublished(String service) {
        return this.services.containsKey(service);
    }

    public Class getFacadeClass(String service) {
        return this.services.get(service).getFacadeClass();
    }

    public String[] getAdditionalFacades(String service) {
        return this.services.get(service).getAdditionalFacades();
    }

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

    public void publish(Object impl, String service, Class primaryFacade) throws PublicationException {
        publish(impl, service, new Publication(primaryFacade));
        if (publicationDelegate != null) {
            publicationDelegate.publish(impl, service, primaryFacade);
        }
    }

    public void publish(Object impl, String service, Publication publicationDescription) throws PublicationException {

        PublicationItem primaryFacade = publicationDescription.getPrimaryFacade();
        PublicationItem[] secondaryFacades = publicationDescription.getAdditionalFacades();

        if (services.containsKey(StaticStubHelper.formatServiceName(service))) {
            throw new PublicationException("Service '" + service + "' already published");
        }

        String interfaceName = primaryFacade.getFacadeClass().getName();

        // add method maps for main lookup-able service.
        Map<String, Method> mainMethodMap = new HashMap<String, Method>();
        ServiceHandler mainServiceHandler = makeServiceHandler(service + "_Main", publicationDescription, primaryFacade);
        mainServiceHandler.addInstance(new Long(0), impl);

        // as the main service is lookup-able, it has a prexisting impl.
        services.put(StaticStubHelper.formatServiceName(service), mainServiceHandler);

        // add method maps for all the additional facades.
        for (PublicationItem secondaryFacade : secondaryFacades) {
            String encodedClassName = MethodNameHelper.encodeClassName(secondaryFacade.getFacadeClass().getName());
            ServiceHandler serviceHandler = makeServiceHandler(service + "_" + encodedClassName,
                    publicationDescription, secondaryFacade);
            services.put(service + "_" + encodedClassName, serviceHandler);
        }

        if (publicationDelegate != null) {
            publicationDelegate.publish(impl, service, publicationDescription);
        }

    }

    protected ServiceHandler makeServiceHandler(String thing, Publication publicationDescription, PublicationItem item) {
        return new ReflectionServiceHandler(this, thing, publicationDescription, item.getFacadeClass());
    }

    public void redirect(String serviceName, String to) {
        redirected.put(serviceName,  to);
    }

    public Object getInstanceForReference(String objectName, Long reference) {
        ServiceHandler serviceHandler = getServiceHandler(objectName);
        return serviceHandler.getInstanceForReference(reference);
    }

    public String getRedirectedTo(String service) {
        return redirected.get(service);  

    }

    public boolean isRedirected(String service) {
        return redirected.containsKey(service);
    }


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

    public ServiceHandler getServiceHandler(String service) {
        return services.get(service);
    }
}
