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

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.client.transports.StatefulClientInvoker;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Class DirectClientInvoker
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public abstract class DirectClientInvoker extends StatefulClientInvoker {

    protected boolean methodLogging = false;


    public DirectClientInvoker(ClientMonitor clientMonitor, ScheduledExecutorService executorService,
                                           ConnectionPinger connectionPinger, ClassLoader facadesClassLoader) {
        super(clientMonitor, executorService, connectionPinger, facadesClassLoader);
    }

    protected boolean tryReconnect() {

        // blimey how do we reconnect this?
        throw new InvocationException("Direct connection broken, unable to reconnect.");
    }

}
