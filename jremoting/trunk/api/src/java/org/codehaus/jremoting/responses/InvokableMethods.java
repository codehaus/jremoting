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

package org.codehaus.jremoting.responses;

import org.codehaus.jremoting.responses.ResponseConstants;
import org.codehaus.jremoting.responses.AbstractResponse;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Class PublishedObjectList
 *
 * @author Vinay Chandrasekharan
 * @version $Revision: 1.2 $
 */
public final class InvokableMethods extends AbstractResponse {

    static final long serialVersionUID = 420067307396614451L;
    /**
     * String array of the methods list of the request publishedObject
     */
    private String[] listOfMethods;


    /**
     * Constructor initialized with remote method list of the publishedObject
     *
     * @param listOfMethods : list of remote methods exposed by the published Obj
     */
    public InvokableMethods(String[] listOfMethods) {
        this.listOfMethods = listOfMethods;
    }

    /**
     * Default Constructor needed for Externalization
     */
    public InvokableMethods() {
    }

    //------Methods--------------//
    /**
     * Get the list of methods of the publishedObject.
     *
     * @return the list of methods
     */
    public String[] getListOfMethods() {
        return listOfMethods;
    }

    //-----AbstractResponse override---//
    /**
     * Gets number that represents type for this class.
     * This is quicker than instanceof for type checking.
     *
     * @return the representative code
     * @see org.codehaus.jremoting.responses.ResponseConstants
     */
    public int getResponseCode() {
        return ResponseConstants.LISTRESPONSE;
    }

    //----Externalizable Overrides--//
    /**
     * @see java.io.Externalizable#writeExternal(ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(listOfMethods);
    }

    /**
     * @see java.io.Externalizable#readExternal(ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        listOfMethods = (String[]) in.readObject();
    }
}
