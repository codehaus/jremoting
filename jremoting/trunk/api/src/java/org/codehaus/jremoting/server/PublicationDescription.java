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
package org.codehaus.jremoting.server;

import java.util.ArrayList;

/**
 * Class PublicationDescription
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public final class PublicationDescription {

    /**
     * An array of facades to expose.
     */
    private ArrayList<PublicationDescriptionItem> primaryFacades = new ArrayList<PublicationDescriptionItem>();
    /**
     * An array of additional facades.
     */
    private ArrayList<PublicationDescriptionItem> additionalFacades = new ArrayList<PublicationDescriptionItem>();

    /**
     * Construct a publication description.
     *
     * @param primaryFacade the principal interface implemented by the lookupable instance.
     */
    public PublicationDescription(Class primaryFacade) {
        this(new Class[]{primaryFacade}, new Class[0]);
    }

    /**
     * Construct a publication description.
     *
     * @param primaryFacade the principal interface implemented by the lookupable instance.
     * @param additionalFacade  additional facade implemented by other instance that
     *                          would otherwise be serialized and treated as pass-by-value objects.
     */
    public PublicationDescription(Class primaryFacade, Class additionalFacade) {
        this(new Class[]{primaryFacade}, new Class[]{additionalFacade});
    }

    /**
     * Construct a publication description.
     *
     * @param primaryFacade the principal interface implemented by the lookupable instance.
     * @param additionalFacades additional facades implemented by other instance that
     *                          would otherwise be serialized and treated as pass-by-value objects.
     */
    public PublicationDescription(Class primaryFacade, Class[] additionalFacades) {
        this(new Class[]{primaryFacade}, additionalFacades);
    }

    /**
     * Construct a publication description.
     *
     * @param primaryFacades the principal interfaces implemented by the lookupable instance.
     */
    public PublicationDescription(Class[] primaryFacades) {
        this(primaryFacades, new Class[0]);
    }

    /**
     * Construct a publication description.
     *
     * @param primaryFacades the principal interfaces implemented by the lookupable instance.
     * @param additionalFacades  assitional facades implemented by other instance that
     *                           would otherwise be serialized and treated as pass-by-value objects.
     */
    public PublicationDescription(Class[] primaryFacades, Class[] additionalFacades) {
        addPrimaryFacades(primaryFacades);
        addAdditionalFacadesToExpose(additionalFacades);

    }

    /**
     * Construct a publication description.
     *
     * @param primaryFacade the principal interface implemented by the lookupable instance.
     * @param facadesClassLoader       the classloader containing the classdefs (special cases)
     * @throws PublicationException if there is a problem publishing
     */
    public PublicationDescription(String primaryFacade, ClassLoader facadesClassLoader) throws PublicationException {
        this(makeClasses(new String[]{primaryFacade}, facadesClassLoader), new Class[0]);
    }

    /**
     * Construct a publication description.
     *
     * @param primaryFacade the principal interface implemented by the lookupable instance.
     * @param additionalFacade  assitional facade implemented by other instance that
     *                          would otherwise be serialized and treated as pass-by-value objects.
     * @param facadesClassLoader       the classloader containing the classdefs (special cases)
     * @throws PublicationException if there is a problem publishing
     */
    public PublicationDescription(String primaryFacade, String additionalFacade, ClassLoader facadesClassLoader) throws PublicationException {
        this(makeClasses(new String[]{primaryFacade}, facadesClassLoader), makeClasses(new String[]{additionalFacade}, facadesClassLoader));
    }

    /**
     * Construct a publication description.
     *
     * @param primaryFacade the principal interface implemented by the lookupable instance.
     * @param additionalFacades assitional facades implemented by other instance that
     *                          would otherwise be serialized and treated as pass-by-value objects.
     * @param facadesClassLoader       the classloader containing the classdefs (special cases)
     * @throws PublicationException if there is a problem publishing
     */
    public PublicationDescription(String primaryFacade, String[] additionalFacades, ClassLoader facadesClassLoader) throws PublicationException {
        this(makeClasses(new String[]{primaryFacade}, facadesClassLoader), makeClasses(additionalFacades, facadesClassLoader));
    }

    /**
     * Construct a publication description.
     *
     * @param primaryFacades the principal interfaces implemented by the lookupable instance.
     * @param facadesClassLoader        the classloader containing the classdefs (special cases)
     * @throws PublicationException if there is a problem publishing
     */
    public PublicationDescription(String[] primaryFacades, ClassLoader facadesClassLoader) throws PublicationException {
        this(makeClasses(primaryFacades, facadesClassLoader), new Class[0]);
    }

    /**
     * Construct a publication description.
     *
     * @param primaryFacades the principal interfaces implemented by the lookupable instance.
     * @param additionalFacades  assitional facades implemented by other instances that
     *                           would otherwise be serialized and treated as pass-by-value objects.
     * @param facadesClassLoader        the classloader containing the classdefs (special cases)
     * @throws PublicationException if there is a problem publishing
     */
    public PublicationDescription(String[] primaryFacades, String[] additionalFacades, ClassLoader facadesClassLoader) throws PublicationException {
        this(makeClasses(primaryFacades, facadesClassLoader), makeClasses(additionalFacades, facadesClassLoader));
    }

    public PublicationDescription() {
    }

    private static Class[] makeClasses(String[] classNames, ClassLoader facadesClassLoader) throws PublicationException {

        try {
            Class[] classes = new Class[classNames.length];

            for (int i = 0; i < classNames.length; i++) {
                String clsNam = classNames[i];

                classes[i] = facadesClassLoader.loadClass(clsNam);
            }

            return classes;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();

            throw new PublicationException("Class not found during publication:" + e.getMessage() + " " + e.getException().getMessage());
        }
    }

    public void addPrimaryFacades(Class[] primaryFacades) {
        for (Class primaryFacade : primaryFacades) {
            addPrimaryFacade(new PublicationDescriptionItem(primaryFacade));
        }
    }

    public void addPrimaryFacade(PublicationDescriptionItem publicationDescriptionItem) {
        addPrimaryFacades(new PublicationDescriptionItem[]{publicationDescriptionItem});
    }

    public void addPrimaryFacades(PublicationDescriptionItem[] publicationDescriptionItems) {
        for (PublicationDescriptionItem publicationDescriptionItem : publicationDescriptionItems) {
            if (publicationDescriptionItem == null) {
                throw new RuntimeException("'PubDescItem' cannot be null");
            }
            if (publicationDescriptionItem.getFacadeClass() == null) {
                throw new RuntimeException("'Class' cannot be null");
            }
            primaryFacades.add(publicationDescriptionItem);
        }
    }

    public void addAdditionalFacadesToExpose(Class[] additionalFacades) {
        for (Class additionalFacade : additionalFacades) {
            addAdditionalFacadeToExpose(new PublicationDescriptionItem(additionalFacade));
        }
    }

    public void addAdditionalFacadeToExpose(PublicationDescriptionItem publicationDescriptionItem) {
        addAdditionalFacadesToExpose(new PublicationDescriptionItem[]{publicationDescriptionItem});
    }

    public void addAdditionalFacadesToExpose(PublicationDescriptionItem[] publicationDescriptionItems) {
        for (PublicationDescriptionItem publicationDescriptionItem : publicationDescriptionItems) {
            if (publicationDescriptionItem == null) {
                throw new RuntimeException("'PubDescItem' cannot be null");
            }
            if (publicationDescriptionItem.getFacadeClass() == null) {
                throw new RuntimeException("'Class' cannot be null");
            }
            additionalFacades.add(publicationDescriptionItem);
        }
    }


    /**
     * Get the principal facades to expose.
     *
     * @return an array of those interfaces.
     */
    public PublicationDescriptionItem[] getPrimaryFacades() {
        PublicationDescriptionItem[] items = new PublicationDescriptionItem[primaryFacades.size()];
        primaryFacades.toArray(items);
        return items;
    }

    /**
     * Get the additional facades.
     *
     * @return an array of those facades.
     */
    public PublicationDescriptionItem[] getAdditionalFacades() {
        PublicationDescriptionItem[] items = new PublicationDescriptionItem[additionalFacades.size()];
        additionalFacades.toArray(items);
        return items;

    }


    /**
     * Get the most derived type for a instance.
     *
     * @param instance the implementation
     * @return an interface that is the most derived type.
     */
    public Class getMostDerivedType(Object instance) {

        //TODO relies of an order leadin to most derived type being last?

        Class facadeRetVal = null;

        for (PublicationDescriptionItem additionalFacade : additionalFacades) {
            Class facadeClass = additionalFacade.getFacadeClass();

            if (facadeClass.isAssignableFrom(instance.getClass())) {
                if (facadeRetVal == null) {
                    facadeRetVal = facadeClass;
                } else if (facadeRetVal.isAssignableFrom(facadeClass)) {
                    facadeRetVal = facadeClass;
                }
            }
        }

        return facadeRetVal;
    }
}
