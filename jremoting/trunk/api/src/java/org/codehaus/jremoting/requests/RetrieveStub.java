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
package org.codehaus.jremoting.requests;

import org.codehaus.jremoting.requests.Servicable;
import org.codehaus.jremoting.requests.RequestConstants;


/**
 * Class RetrieveStub
 *
 * @author Paul Hammant
 *
 */
public final class RetrieveStub extends Servicable {
    private static final long serialVersionUID = -4942430631067275194L;

    /**
     * Constructor RetrieveStub
     *
     * @param publishedServiceName The service name that was originally looked up.
     * @param objectName           the object name of the node within that.
     */
    public RetrieveStub(String publishedServiceName, String objectName) {
        super(publishedServiceName, objectName);
    }

    /**
     * Constructor for Externalization.
     */
    public RetrieveStub() {
    }

    /**
     * Gets number that represents type for this class.
     * This is quicker than instanceof for type checking.
     *
     * @return the representative code
     * @see org.codehaus.jremoting.requests.RequestConstants
     */
    public int getRequestCode() {
        return RequestConstants.CLASSREQUEST;
    }
}
