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

import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.responses.Response;

/**
 * Class ServiceHandler
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public interface ServiceHandler {

    /**
     * Handle a method invocation.
     *
     * @param request the method request.
     * @return the response for the request.
     */
    Response handleMethodInvocation(InvokeMethod request, Object connectionDetails);

    /**
     * Add an instance by reference ID
     *
     * @param reference the ref id for the instance.
     * @param instance    the instance.
     */
    void addInstance(Long reference, Object instance);

    /**
     * Replace an instance
     *
     * @param instance     the instance
     * @param withInstance the new instance
     */
    void replaceInstance(Object instance, Object withInstance);

    /**
     * Get or make a reference ID for an instance.
     *
     * @param instance the instance
     * @return the reference ID
     */
    Long getOrMakeReferenceIDForInstance(Object instance);

    /**
     * Get the most derived type for an instance
     *
     * @param instance the instance
     * @return the most derived class type.
     */
    Class getMostDerivedType(Object instance);

    void setMethodInvocationMonitor(MethodInvocationMonitor monitor);

    Class getFacadeClass();

    String[] getAdditionalFacades();

    Object getInstanceForReference(Long reference);


}
