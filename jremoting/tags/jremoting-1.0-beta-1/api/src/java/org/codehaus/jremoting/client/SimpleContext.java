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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SimpleContext implements Context, Externalizable {

    private long contextSeq;
    private int hashCode;
    private static final long serialVersionUID = 3979584504728387550L;

    public SimpleContext(long contextSeq) {
        this.contextSeq = contextSeq;
    }

    // for externalization.
    public SimpleContext() {
    }

    public int hashCode() {
        if (hashCode == 0) {
            hashCode = new Long(contextSeq).hashCode();
        }
        return hashCode;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SimpleContext)) {
            return false;
        } else {
            return contextSeq == ((SimpleContext) obj).contextSeq;
        }
    }

    public String toString() {
        return "SimpleContext:" + contextSeq;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(contextSeq);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        contextSeq = in.readLong();
    }
}
