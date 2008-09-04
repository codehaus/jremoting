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

import org.codehaus.jremoting.Sessionable;
import org.codehaus.jremoting.authentication.AuthenticationChallenge;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Class ConnectionOpened
 *
 * @author Paul Hammant
 *
 */
public final class ConnectionOpened extends Response implements Sessionable {

    private AuthenticationChallenge authChallenge;
    private long session;
    private static final long serialVersionUID = 2162189349557278406L;

    public ConnectionOpened(AuthenticationChallenge authChallenge, long session) {
        this.authChallenge = authChallenge;
        this.session = session;
    }

    /**
     * Constructor ConnectionOpened for Externalization
     */
    public ConnectionOpened() {
    }

    public AuthenticationChallenge getAuthenticationChallenge() {
        return authChallenge;
    }

    public Long getSessionID() {
        return session;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(authChallenge);
        out.writeObject(session);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        authChallenge = (AuthenticationChallenge) in.readObject();
        session = (Long) in.readObject();
    }
}
