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
 * Class Publication
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public final class Publication {

    /**
     * An array of facades to expose.
     */
    private ArrayList<PublicationItem> primaryFacades = new ArrayList<PublicationItem>();
    /**
     * An array of additional facades.
     */
    private ArrayList<PublicationItem> additionalFacades = new ArrayList<PublicationItem>();


    public Publication() {
    }

    public Publication addPrimaryFacade(Class facade) {
        primaryFacades.add(new PublicationItem(facade));
        return this;
    }

    public Publication addPrimaryFacades(Class... facades) {
        for (Class facade : facades) {
            addPrimaryFacade(new PublicationItem(facade));
        }
        return this;
    }

    public Publication addPrimaryFacade(PublicationItem publicationDescriptionItem) {
        return addPrimaryFacades(new PublicationItem[]{publicationDescriptionItem});
    }

    public Publication addPrimaryFacades(PublicationItem... publicationDescriptionItems) {
        for (PublicationItem publicationDescriptionItem : publicationDescriptionItems) {
            if (publicationDescriptionItem == null) {
                throw new RuntimeException("'PubDescItem' cannot be null");
            }
            if (publicationDescriptionItem.getFacadeClass() == null) {
                throw new RuntimeException("'Class' cannot be null");
            }
            primaryFacades.add(publicationDescriptionItem);
        }
        return this;
    }

    public Publication addAdditionalFacades(Class... facades) {
        for (Class facade : facades) {
            addAdditionalFacade(new PublicationItem(facade));
        }
        return this;
    }

    public Publication addAdditionalFacade(PublicationItem publicationDescriptionItem) {
        return addAdditionalFacade(new PublicationItem[]{publicationDescriptionItem});
    }

    public Publication addAdditionalFacade(PublicationItem... publicationDescriptionItems) {
        for (PublicationItem publicationDescriptionItem : publicationDescriptionItems) {
            if (publicationDescriptionItem == null) {
                throw new RuntimeException("'PubDescItem' cannot be null");
            }
            if (publicationDescriptionItem.getFacadeClass() == null) {
                throw new RuntimeException("'Class' cannot be null");
            }
            additionalFacades.add(publicationDescriptionItem);
        }
        return this;
    }

    /**
     * Get the principal facades to expose.
     *
     * @return an array of those interfaces.
     */
    public PublicationItem[] getPrimaryFacades() {
        PublicationItem[] items = new PublicationItem[primaryFacades.size()];
        primaryFacades.toArray(items);
        return items;
    }

    /**
     * Get the additional facades.
     *
     * @return an array of those facades.
     */
    public PublicationItem[] getAdditionalFacades() {
        PublicationItem[] items = new PublicationItem[additionalFacades.size()];
        additionalFacades.toArray(items);
        return items;
    }

    public String[] getAdditionalFacadeNames() {
        String[] items = new String[additionalFacades.size()];
        for (int i = 0; i < additionalFacades.size(); i++) {
            PublicationItem item = additionalFacades.get(i);
            items[i] = item.getFacadeClass().getName();
        }
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

        for (PublicationItem additionalFacade : additionalFacades) {
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
