/* ====================================================================
 * Copyright 2005 JRemoting Committers
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

import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.api.RegistryHelper;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.client.ClientInvocationHandler;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;

public abstract class AbstractSameVmBindableHostContext extends AbstractHostContext {

    protected final ThreadPool threadPool;
    protected final ClientMonitor clientMonitor;
    protected final ConnectionPinger connectionPinger;

    public AbstractSameVmBindableHostContext(ThreadPool threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger, ClientInvocationHandler clientInvocationHandler) {
        super(clientInvocationHandler);
        this.threadPool = threadPool;
        this.clientMonitor = clientMonitor;
        this.connectionPinger = connectionPinger;
    }


    /**
     * Make a HostContext for this using SameVM connections nstead of socket based ones.
     *
     * @return the HostContext
     * @throws ConnectionException if a problem
     */
    public abstract AbstractHostContext makeSameVmHostContext() throws ConnectionException;

    protected Object getOptmization(String uniqueID) {
        return new RegistryHelper().get("/.codehausRemoting/optimizations/" + uniqueID);
    }

}
