/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
 * Portions copyright 2001 - 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.codehaus.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.jremoting.client.transports.direct;

import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.commands.Request;
import org.codehaus.jremoting.commands.Response;
import org.codehaus.jremoting.server.ServerInvocationHandler;

import java.io.IOException;

/**
 * Class DirectInvocationHandler
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public final class DirectInvocationHandler extends AbstractDirectInvocationHandler {

    private ServerInvocationHandler invocationHandler;


    /**
     * Constructor DirectInvocationHandler
     *
     * @param threadPool
     * @param clientMonitor
     * @param connectionPinger
     * @param invocationHandler
     */
    public DirectInvocationHandler(ThreadPool threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger, ServerInvocationHandler invocationHandler) {
        super(threadPool, clientMonitor, connectionPinger);
        this.invocationHandler = invocationHandler;
    }

    protected Response performInvocation(Request request) throws IOException {
        return invocationHandler.handleInvocation(request, "");
    }

}
