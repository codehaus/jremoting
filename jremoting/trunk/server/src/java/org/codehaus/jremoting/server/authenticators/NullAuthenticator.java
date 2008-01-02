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
package org.codehaus.jremoting.server.authenticators;

import org.codehaus.jremoting.authentications.Authentication;
import org.codehaus.jremoting.server.AuthenticationChallenge;
import org.codehaus.jremoting.server.Authenticator;

/**
 * Check the Authority of a client to a service.
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class NullAuthenticator implements Authenticator {

    public boolean checkAuthority(Authentication authentication, String publishedService) {
        return authentication == null;
    }

    public AuthenticationChallenge getAuthenticationChallenge() {
        return null;
    }
}
