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
package org.codehaus.jremoting.server.transports.socket;

import java.util.concurrent.ExecutorService;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;
import org.codehaus.jremoting.server.transports.AbstractServerStreamDriver;
import org.codehaus.jremoting.server.transports.ServerCustomStreamDriver;

/**
 * Class PartialSocketCustomStreamServer
 *
 * @author Paul Hammant
 * @author Peter Royal
 * @version $Revision: 1.3 $
 */
public class PartialSocketCustomStreamServer extends AbstractPartialSocketStreamServer {

    /**
     * Construct a PartialSocketCustomStreamServer
     *
     * @param serverMonitor            the monitor
     * @param invocationHandlerAdapter the handler
     */
    public PartialSocketCustomStreamServer(ServerMonitor serverMonitor, InvocationHandlerAdapter invocationHandlerAdapter, ExecutorService executor) {
        super(invocationHandlerAdapter, serverMonitor, executor);
    }


    /**
     * Create a Server Stream Driver.
     *
     * @return The Server Stream Driver.
     */
    protected AbstractServerStreamDriver createServerStreamDriver() {
        return new ServerCustomStreamDriver(serverMonitor, executor);
    }

}
