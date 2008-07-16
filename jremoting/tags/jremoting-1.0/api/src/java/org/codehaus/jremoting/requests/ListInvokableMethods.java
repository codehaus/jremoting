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

import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.requests.RequestConstants;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Request the list of remote methods within the service.
 *
 * @author <a href="mailto:vinayc@apache.org">Vinay Chandrasekharan</a>
 */
public final class ListInvokableMethods extends Request {

    /**
     * Name of the service whose remote methods is sought
     */
    private String service;
    private static final long serialVersionUID = 2847045628663185514L;


    /**
     * Constructor.
     *
     * @param publishedName
     */
    public ListInvokableMethods(String publishedName) {
        this.service = publishedName;
    }

    /**
     * default constructor needed for externalization
     */
    public ListInvokableMethods() {
    }

    /**
     * Get the service name
     */
    public String getService() {
        return service;
    }

    /**
     * Gets number that represents type for this class.
     * This is quicker than instanceof for type checking.
     *
     * @return the representative code
     * @see org.codehaus.jremoting.requests.RequestConstants
     */
    public int getRequestCode() {
        return RequestConstants.LISTMETHODSREQUEST;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(service);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        service = (String) in.readObject();
    }
}
