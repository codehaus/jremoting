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

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.util.Date;

/**
 * Class TstInterface3Impl
 *
 * @author Paul Hammant <a href="mailto:Paul_Hammant@yahoo.com">Paul_Hammant@yahoo.com</a>
 * @version * $Revision: 1.2 $
 */
public class TstFacade3Impl extends TstFacade2Impl implements TestFacade3, Externalizable {

    private Date dob;

    public TstFacade3Impl(Date dob, String name) {
        super(name);

        this.dob = dob;
    }

    public void setDOB(Date dob) {

        this.dob = dob;

    }

    public Date getDOB() {
        return dob;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        throw new RuntimeException("not allowed");
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        throw new RuntimeException("not allowed");
    }
}
