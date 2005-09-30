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
package org.codehaus.jremoting.client.transports.direct;

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.factories.AbstractHostContext;
import org.codehaus.jremoting.client.pingers.NeverConnectionPinger;
import org.codehaus.jremoting.client.monitors.DumbClientMonitor;
import org.codehaus.jremoting.client.factories.AbstractHostContext;
import org.codehaus.jremoting.api.DefaultThreadPool;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.server.ServerMarshalledInvocationHandler;


/**
 * Class DirectMarshalledHostContext
 *
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class DirectMarshalledHostContext extends AbstractHostContext
{

    /**
     * Constructor DirectMarshalledHostContext
     *
     * @param threadPool
     * @param clientMonitor
     * @param connectionPinger
     * @param interfacesClassLoader
     * @param invocationHandler
     */
    public DirectMarshalledHostContext(ThreadPool threadPool,
                                       ClientMonitor clientMonitor,
                                       ConnectionPinger connectionPinger,
                                       ClassLoader interfacesClassLoader,
                                       ServerMarshalledInvocationHandler invocationHandler)
    {
        super(
                new DirectMarshalledInvocationHandler(
                        threadPool,
                        clientMonitor,
                        connectionPinger,
                        invocationHandler,
                        interfacesClassLoader)
        );
    }

    public static class WithSimpleDefaults extends DirectMarshalledHostContext
    {
        /**
         *
         * @param invocationHandler
         */
        public WithSimpleDefaults(ServerMarshalledInvocationHandler invocationHandler)
        {
            super(
                    new DefaultThreadPool(),
                    new DumbClientMonitor(),
                    new NeverConnectionPinger(),
                    DirectMarshalledHostContext.class.getClassLoader(),
                    invocationHandler
            );
        }
    }
}
