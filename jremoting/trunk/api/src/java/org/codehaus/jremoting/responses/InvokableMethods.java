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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Class InvokableMethods
 *
 * @author Vinay Chandrasekharan
 * @version $Revision: 1.2 $
 */
public final class InvokableMethods extends AbstractResponse {

    static final long serialVersionUID = 420067307396614451L;

    private String[] listOfMethods;


    /**
     * Constructor initialized with remote method list of the service
     *
     * @param listOfMethods : list of remote methods exposed by the service
     */
    public InvokableMethods(String[] listOfMethods) {
        this.listOfMethods = listOfMethods;
    }

    /**
     * Default Constructor needed for Externalization
     */
    public InvokableMethods() {
    }

    public String[] getListOfMethods() {
        return listOfMethods;
    }

    public int getResponseCode() {
        return ResponseConstants.LISTRESPONSE;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(listOfMethods);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        listOfMethods = (String[]) in.readObject();
    }
}
