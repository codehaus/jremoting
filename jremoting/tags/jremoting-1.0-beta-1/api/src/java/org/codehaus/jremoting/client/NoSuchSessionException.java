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


/**
 * Class NoSuchSessionException
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class NoSuchSessionException extends InvocationException {
    private static final long serialVersionUID = 1196918710216319726L;

    /**
     * Constructor NoSuchSessionException
     *
     * @param session the session id
     */
    public NoSuchSessionException(long session) {
        super("There is no instance on the server mapped to session " + session + ". The server may have been bounced since the client first did a lookup.");
    }
}
