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
package org.codehaus.jremoting.server.transports;

import org.codehaus.jremoting.client.Context;

public class DefaultServerSideContext implements Context {
    private Long session;
    private Context context;
    private int hashCode;

    public DefaultServerSideContext(Long session, Context context) {
        this.session = session;
        this.context = context;
        if (session != null && context != null) {
            hashCode = session.hashCode() + context.hashCode();
        }
    }

    public int hashCode() {
        return hashCode;
    }

    public boolean equals(Object obj) {
        if (obj instanceof DefaultServerSideContext) {
            DefaultServerSideContext other = (DefaultServerSideContext) obj;
            if (!session.equals(other.session)) {
                return false;
            }
            if (context == null || !context.equals(other.context)) {
                return false;
            }
            return true;
        }
        return false;
    }

    public String toString() {
        return "DefaultServerSideContext[session=" + session + ", context=" + context + "]";
    }

}
