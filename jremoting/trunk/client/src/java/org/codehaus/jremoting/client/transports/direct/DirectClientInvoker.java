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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.pingers.NeverConnectionPinger;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.ServerInvoker;

/**
 * Class DirectClientInvoker
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public final class DirectClientInvoker extends StatefulDirectClientInvoker {

    private ServerInvoker invoker;


    public DirectClientInvoker(ClientMonitor clientMonitor, ScheduledExecutorService executorService, ConnectionPinger connectionPinger,
                                   ServerInvoker invoker) {
        super(clientMonitor, executorService, connectionPinger, DirectClientInvoker.class.getClassLoader());
        this.invoker = invoker;
    }

    public DirectClientInvoker(ClientMonitor clientMonitor, ServerInvoker invoker) {
        this(clientMonitor, Executors.newScheduledThreadPool(10), new NeverConnectionPinger(), invoker);
        this.invoker = invoker;
    }

    protected Response performInvocation(Request request) {
        return invoker.invoke(request, "");
    }

}
