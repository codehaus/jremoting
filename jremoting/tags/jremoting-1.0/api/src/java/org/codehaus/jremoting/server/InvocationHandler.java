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

import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;

/**
 * Interface InvocationHandler
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public interface InvocationHandler {

    /**
     * Handle a method invocation
     *
     * @param request           The request to handle
     * @param connectionDetails Some details of the connection.
     * @return the response that is a consequence of the request
     */
    Response invoke(Request request, String connectionDetails);

}
