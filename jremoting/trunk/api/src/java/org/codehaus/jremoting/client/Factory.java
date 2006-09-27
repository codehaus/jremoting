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
package org.codehaus.jremoting.client;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.authentications.Authentication;


/**
 * Interface Factory
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public interface Factory {

    /**
     * Lookup a name by which the remote service is
     * published by the server.
     * Usage:
     * <code>
     * InterfaceLookup lookupService= . . . . ;
     * RemoteInterface remoteInterface = lookupService.lookup("Published-Name-Of-The-Remote-Server");
     * </code>
     *
     * @param publishedServiceName
     * @return proxy to the Remote service.
     * @throws org.codehaus.jremoting.ConnectionException
     */
    Object lookupService(String publishedServiceName) throws ConnectionException;

    /**
     * Lookup a name by which the remote service is
     * published by the server within the context of
     * the Authentication credentials supplied.
     *
     * @param publishedServiceName
     * @param authentication
     * @return
     * @throws ConnectionException
     */
    Object lookupService(String publishedServiceName, Authentication authentication) throws ConnectionException;

    String[] listServices();

    String getTextToSignForAuthentication();

    void close();

    boolean hasService(String publishedServiceName);


}
