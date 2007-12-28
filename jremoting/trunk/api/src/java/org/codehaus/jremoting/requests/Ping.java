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

import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.Serializable;


/**
 * Class Ping
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public final class Ping extends Request implements Serializable {
    private long session;
    private static final long serialVersionUID = -4110992956178784345L;

    /**
     * Gets number that represents type for this class.
     * This is quicker than instanceof for type checking.
     *
     * @return the representative code
     * @see org.codehaus.jremoting.requests.RequestConstants
     */
    public int getRequestCode() {
        return RequestConstants.PINGREQUEST;
    }

    public void setSession(long session) {
        this.session = session;
    }

    public Long getSession() {
        return session;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(session);
        super.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        session = in.readLong();
        super.readExternal(in);
    }
}
