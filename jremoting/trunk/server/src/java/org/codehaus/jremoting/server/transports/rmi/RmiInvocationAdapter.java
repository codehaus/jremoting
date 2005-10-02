/* ====================================================================
 * Copyright 2005 JRemoting Committers
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

import org.codehaus.jremoting.commands.Response;
import org.codehaus.jremoting.commands.Request;
import org.codehaus.jremoting.api.RmiInvocationHandler;
import org.codehaus.jremoting.server.transports.AbstractServer;

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
    private AbstractServer abstractServer;

    /**
     * Constructor a RmiInvocationAdapter with an abstract server.
     *
     * @param abstractServer The abstract server
     */
    public RmiInvocationAdapter(AbstractServer abstractServer) {
        this.abstractServer = abstractServer;
    }

    /**
     * Handle an Invocation
     *
     * @param request The request
     * @return a reply object
     * @throws RemoteException if a problem during processing
     */
    public Response handleInvocation(Request request) throws RemoteException {
        return abstractServer.handleInvocation(request, "RMI-TODO");
    }
}
