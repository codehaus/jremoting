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
package org.codehaus.jremoting.client.transports.socket;

import org.codehaus.jremoting.api.ConnectionException;
import java.util.concurrent.ExecutorService;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.Factory;
import org.codehaus.jremoting.client.HostContext;
import org.codehaus.jremoting.client.InterfaceLookup;
import org.codehaus.jremoting.client.factories.AbstractFactoryHelper;

/**
 * Class SocketObjectStreamFactoryHelper
 * <p/>
 * "SocketObjectStream:abcde.com:1234"
 * 0         :  1      : 2
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class SocketObjectStreamFactoryHelper extends AbstractFactoryHelper {
    private ExecutorService executor;
    private ClientMonitor clientMonitor;
    private ConnectionPinger connectionPinger;

    public SocketObjectStreamFactoryHelper(ExecutorService executor, ClientMonitor clientMonitor, ConnectionPinger connectionPinger) {
        this.executor = executor;
        this.clientMonitor = clientMonitor;
        this.connectionPinger = connectionPinger;
    }

    /**
     * Method getInterfaceLookup
     *
     * @param factoryString
     * @param interfacesClassLoader
     * @return
     */
    public InterfaceLookup getInterfaceLookup(String factoryString, ClassLoader interfacesClassLoader, boolean optimize) throws ConnectionException {
        // TODO maybe we should cache these.  Or the abstract parent class should.
        String[] terms = processFactoryString(factoryString);
        HostContext hc = new SocketObjectStreamHostContext(executor, clientMonitor, connectionPinger, interfacesClassLoader, terms[1], Integer.parseInt(terms[2]));
        Factory af = createFactory(terms[3], hc, optimize);

        return af;
    }
}
