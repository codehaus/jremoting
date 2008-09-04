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
 * Class NoSuchSession
 *
 * @author Paul Hammant
 *
 */
public final class NoSuchSession extends NotPublished implements ProblemResponse{

    private long session;
    private static final long serialVersionUID = 4366234682624306556L;

    /**
     * Constructor NoSuchSession
     */
    public NoSuchSession() {
    }

    /**
     * Constructor NoSuchSession
     *
     * @param session the reference identifier
     */
    public NoSuchSession(long session) {
        this.session = session;
    }

    /**
     * Get the session ID.
     *
     * @return the session id
     */
    public Long getSessionID() {
        return session;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(session);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        super.readExternal(in);

        session = (Long) in.readObject();
    }
}
