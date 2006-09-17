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
package org.codehaus.jremoting.client.transports.rmi;

import org.codehaus.jremoting.api.ConnectionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.factories.AbstractHostContext;
import org.codehaus.jremoting.client.monitors.SimpleRetryingClientMonitor;
import org.codehaus.jremoting.client.pingers.PerpetualConnectionPinger;

/**
 * Class RmiHostContext
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class RmiHostContext extends AbstractHostContext {

    //TODO constr with classloader

    /**
     * Constructor RmiHostContext
     *
     * @param host
     * @param port
     * @throws ConnectionException
     */
    public RmiHostContext(ExecutorService threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger, String host, int port) throws ConnectionException {
        super(new RmiClientInvocationHandler(threadPool, clientMonitor, connectionPinger, host, port));
    }

    public RmiHostContext(String host, int port) throws ConnectionException {
        this(Executors.newCachedThreadPool(), new SimpleRetryingClientMonitor(), new PerpetualConnectionPinger(), host, port);

    }
}
