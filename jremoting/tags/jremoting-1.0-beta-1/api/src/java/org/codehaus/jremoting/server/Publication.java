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

    private PublicationItem primaryFacade;

    private ArrayList<PublicationItem> secondaryFacades = new ArrayList<PublicationItem>();


    public Publication(Class facade) {
        primaryFacade = new PublicationItem(facade);
    }

    public Publication(PublicationItem publicationItem) {
        primaryFacade = publicationItem;
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
            secondaryFacades.add(publicationDescriptionItem);
        }
        return this;
    }

    public PublicationItem getPrimaryFacade() {
        return primaryFacade;
    }

    public PublicationItem[] getAdditionalFacades() {
        PublicationItem[] items = new PublicationItem[secondaryFacades.size()];
        secondaryFacades.toArray(items);
        return items;
    }

    public String[] getAdditionalFacadeNames() {
        String[] items = new String[secondaryFacades.size()];
        for (int i = 0; i < secondaryFacades.size(); i++) {
            PublicationItem item = secondaryFacades.get(i);
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

        for (PublicationItem secondaryFacade : secondaryFacades) {
            Class facadeClass = secondaryFacade.getFacadeClass();

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
