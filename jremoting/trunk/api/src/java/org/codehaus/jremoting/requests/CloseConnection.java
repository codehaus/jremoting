/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
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

import org.codehaus.jremoting.Sessionable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class CloseConnection extends Request implements Sessionable {

    private Long session;

   /**
    * Constructor for Externalization
    */
    public CloseConnection() {
    }

    public CloseConnection(Long session) {
        this.session = session;
    }

    public int getRequestCode() {
        return RequestConstants.CLOSECONNECTIONREQUEST;
    }

    public Long getSessionID() {
        return session;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(session);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        session = in.readLong();
    }
}
