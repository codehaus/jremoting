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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Class NoSuchSession
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public final class NoSuchSession extends NotPublished {
//    static final long serialVersionUID = TODO;

    private Long sessionID;

    /**
     * Constructor NoSuchSession
     */
    public NoSuchSession() {
    }

    /**
     * Constructor NoSuchSession
     *
     * @param sessionID the reference identifier
     */
    public NoSuchSession(Long sessionID) {
        this.sessionID = sessionID;
    }

    /**
     * Gets number that represents type for this class.
     * This is quicker than instanceof for type checking.
     *
     * @return the representative code
     * @see org.codehaus.jremoting.responses.ResponseConstants
     */
    public int getResponseCode() {
        return ResponseConstants.NOSUCHSESSIONRESPONSE;
    }

    /**
     * Get the session ID.
     *
     * @return the session id
     */
    public Long getSessionID() {
        return sessionID;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(sessionID);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        super.readExternal(in);

        sessionID = (Long) in.readObject();
    }
}
