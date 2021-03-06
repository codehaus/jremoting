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
package org.codehaus.jremoting.itests;

import java.io.Serializable;

/**
 * Class TstObject
 *
 * @author Paul Hammant <a href="mailto:Paul_Hammant@yahoo.com">Paul_Hammant@yahoo.com</a>
 * @version $Revision: 1.2 $
 */
public class TstObject implements Serializable {

    private String mName;
    private static final long serialVersionUID = -3942117279584089420L;

    /**
     * Constructor TstObject
     *
     * @param name
     */
    public TstObject(String name) {
        mName = name;
    }

    /**
     * Method getName
     *
     * @return
     */
    public String getName() {
        return mName;
    }

    /**
     * Method setName
     *
     * @param name
     */
    public void setName(String name) {
        mName = name;
    }
}
