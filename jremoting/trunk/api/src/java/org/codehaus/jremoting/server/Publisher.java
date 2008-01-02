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

/**
 * Interface Server
 *
 * @author Paul Hammant
 * @author Vinay Chandrasekharan <a href="mailto:vinayc77@yahoo.com">vinayc77@yahoo.com</a>
 * @version * $Revision: 1.2 $
 */
public interface Publisher {

    /**
     * Publish a object for subsequent lookup.
     *
     * @param impl              the object implementing the principle interface.
     * @param service            the lookup name of the published object
     * @param primaryFacade the principal interface being published
     * @throws PublicationException if there is a problem publishing
     */
    void publish(Object impl, String service, Class primaryFacade) throws PublicationException;

    /**
     * Publish a object for subsequent lookup.
     *
     * @param impl                   the object implementing the principle interface.
     * @param service                 the lookup name of the published object
     * @param publicationDescription describing complex publishing cases.
     * @throws PublicationException if there is a problem publishing
     */
    void publish(Object impl, String service, Publication publicationDescription) throws PublicationException;

    /**
     * UnPublish a previously published object.
     *
     * @param impl          the object implementing the principle interface.
     * @param service the lookup name of the published object
     * @throws org.codehaus.jremoting.server.PublicationException
     *          if there is a problem publishing
     */
    void unPublish(Object impl, String service) throws PublicationException;

    /**
     * Replace Published object with another.
     *
     * @param oldImpl       the old object implementing the principle interface.
     * @param service the lookup name of the published object
     * @param withImpl      the new object implementing the principle interface.
     * @throws PublicationException if there is a problem publishing
     */
    void replacePublished(Object oldImpl, String service, Object withImpl) throws PublicationException;

    /**
     * Is a service published
     * @param service the service
     * @return it is or not
     */
    boolean isPublished(String service);

}
