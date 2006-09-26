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
package org.codehaus.jremoting;

import java.io.Serializable;

/**
 * Class FacadeRefHolder
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public final class FacadeRefHolder implements Serializable {
    static final long serialVersionUID = 4517499953558154280L;

    private Long referenceID;
    private String objectName;

    /**
     * Constructor FacadeRefHolder
     *
     * @param referenceID the reference ID
     * @param objectName  the object name
     */
    public FacadeRefHolder(Long referenceID, String objectName) {
        this.referenceID = referenceID;
        this.objectName = objectName;
    }

    /**
     * Get the reference ID
     *
     * @return the reference ID
     */
    public Long getReferenceID() {
        return referenceID;
    }

    /**
     * Get the object name.
     *
     * @return the object name
     */
    public String getObjectName() {
        return objectName;
    }
}
