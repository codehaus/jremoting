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
import java.util.concurrent.Executors;

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.transports.socket.SocketObjectStreamFactoryHelper;
import org.codehaus.jremoting.client.transports.socket.SocketCustomStreamFactoryHelper;
import org.codehaus.jremoting.client.transports.rmi.RmiFactoryHelper;
import org.codehaus.jremoting.client.monitors.NullClientMonitor;
import org.codehaus.jremoting.client.pingers.DefaultConnectionPinger;

/**
 * Class DefaultInterfaceLookupFactory
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class DefaultInterfaceLookupFactory extends AbstractInterfaceLookupFactory {

    public static final String[] SUPPORTEDSTREAMS = new String[]{"SocketObjectStream", "SocketCustomStream", "RMI"};

    public DefaultInterfaceLookupFactory() {
        this(Executors.newCachedThreadPool(), new NullClientMonitor(), new DefaultConnectionPinger());
    }

    public DefaultInterfaceLookupFactory(ExecutorService threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger) {

        addFactory("SocketObjectStream:", new SocketObjectStreamFactoryHelper(threadPool, clientMonitor, connectionPinger));

        addFactory("SocketCustomStream:", new SocketCustomStreamFactoryHelper(threadPool, clientMonitor, connectionPinger));

        addFactory("RMI:", new RmiFactoryHelper(threadPool, clientMonitor, connectionPinger));

        // TODO - add the rest.
    }

}
