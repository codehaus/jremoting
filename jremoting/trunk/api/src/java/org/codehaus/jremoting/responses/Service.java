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

import org.codehaus.jremoting.responses.Response;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Class Service
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public final class Service extends Response {

    private Long reference;
    private String facadeName;
    private static final long serialVersionUID = 3438191178503573497L;

    /**
     * Constructor Service
     *
     * @param reference the reference ID
     */
    public Service(Long reference, String facadeName) {
        this.reference = reference;
        this.facadeName = facadeName;
    }

    /**
     * Constructor Service for Externalization
     */
    public Service() {
    }

    public String getFacadeName() {
        return facadeName;
    }

    public Long getReference() {
        return reference;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(reference);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        reference = (Long) in.readObject();
    }
}
