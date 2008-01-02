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


    public PublicationDescription() {
    }

    public PublicationDescription addPrimaryFacade(Class facade) {
        primaryFacades.add(new PublicationDescriptionItem(facade));
        return this;
    }

    public PublicationDescription addPrimaryFacades(Class... facades) {
        for (Class facade : facades) {
            addPrimaryFacade(new PublicationDescriptionItem(facade));
        }
        return this;
    }

    public PublicationDescription addPrimaryFacade(PublicationDescriptionItem publicationDescriptionItem) {
        return addPrimaryFacades(new PublicationDescriptionItem[]{publicationDescriptionItem});
    }

    public PublicationDescription addPrimaryFacades(PublicationDescriptionItem... publicationDescriptionItems) {
        for (PublicationDescriptionItem publicationDescriptionItem : publicationDescriptionItems) {
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

    public PublicationDescription addAdditionalFacades(Class... facades) {
        for (Class facade : facades) {
            addAdditionalFacade(new PublicationDescriptionItem(facade));
        }
        return this;
    }

    public PublicationDescription addAdditionalFacade(PublicationDescriptionItem publicationDescriptionItem) {
        return addAdditionalFacade(new PublicationDescriptionItem[]{publicationDescriptionItem});
    }

    public PublicationDescription addAdditionalFacade(PublicationDescriptionItem... publicationDescriptionItems) {
        for (PublicationDescriptionItem publicationDescriptionItem : publicationDescriptionItems) {
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

    public String[] getAdditionalFacadeNames() {
        String[] items = new String[additionalFacades.size()];
        for (int i = 0; i < additionalFacades.size(); i++) {
            PublicationDescriptionItem item = additionalFacades.get(i);
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
