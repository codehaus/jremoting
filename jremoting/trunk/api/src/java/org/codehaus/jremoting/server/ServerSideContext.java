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
package org.codehaus.jremoting.server;

import org.codehaus.jremoting.client.Context;

public class ServerSideContext implements Context {
    private final long session;
    private final Context context;
    private static final long serialVersionUID = 2517644003152720983L;

    public ServerSideContext(long session, Context context) {
        this.session = session;
        this.context = context;
    }


    public int hashCode() {
        int result;
        result = (int) ((31 * session) + context.hashCode());
        return result;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerSideContext that = (ServerSideContext) o;

        if (!context.equals(that.context)) return false;
        if (session != that.session) return false;

        return true;
    }


    public String toString() {
        return "ServerSideContext[session=" + session + ", context=" + context + "]";
    }

}
