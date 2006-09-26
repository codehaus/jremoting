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
package org.codehaus.jremoting.client.factories;

import java.util.concurrent.ExecutorService;
import org.codehaus.jremoting.client.ClientInvocationHandler;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;

public abstract class AbstractSocketStreamHostContext extends AbstractHostContext {

    protected final ExecutorService executorService;
    protected final ClientMonitor clientMonitor;
    protected final ConnectionPinger connectionPinger;

    public AbstractSocketStreamHostContext(ExecutorService executorService, ClientMonitor clientMonitor, ConnectionPinger connectionPinger, ClientInvocationHandler clientInvocationHandler) {
        super(clientInvocationHandler);
        this.executorService = executorService;
        this.clientMonitor = clientMonitor;
        this.connectionPinger = connectionPinger;
    }

}
