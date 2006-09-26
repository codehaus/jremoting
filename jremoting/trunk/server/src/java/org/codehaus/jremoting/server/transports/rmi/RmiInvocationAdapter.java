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
package org.codehaus.jremoting.server.transports.rmi;

import org.codehaus.jremoting.api.RmiInvocationHandler;
import org.codehaus.jremoting.requests.AbstractRequest;
import org.codehaus.jremoting.responses.AbstractResponse;
import org.codehaus.jremoting.server.transports.ConnectingServer;

import java.rmi.RemoteException;

/**
 * Class RmiinvocationAdapter for 'over RMI' invocation adaptation.
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class RmiInvocationAdapter implements RmiInvocationHandler {
    /**
     * The abstract server
     */
    private ConnectingServer connectingServer;

    /**
     * Constructor a RmiInvocationAdapter with an abstract server.
     *
     * @param connectingServer The abstract server
     */
    public RmiInvocationAdapter(ConnectingServer connectingServer) {
        this.connectingServer = connectingServer;
    }

    /**
     * Handle an Invocation
     *
     * @param request The request
     * @return a reply object
     * @throws RemoteException if a problem during processing
     */
    public AbstractResponse handleInvocation(AbstractRequest request) throws RemoteException {
        return connectingServer.handleInvocation(request, "RMI-TODO");
    }
}
