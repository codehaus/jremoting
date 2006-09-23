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
import org.codehaus.jremoting.server.transports.ServerObjectStreamDriver;


/**
 * Class PartialSocketObjectStreamServer
 *
 * @author Paul Hammant
 * @author Peter Royal
 * @version $Revision: 1.2 $
 */
public class PartialSocketObjectStreamServer extends AbstractPartialSocketStreamServer {


    /*
     * Construct a PartialSocketObjectStreamServer
     *
     * @param invocationHandlerAdapter the handler
     * @param serverMonitor the monitor
     * @param port ther port
     */
    public PartialSocketObjectStreamServer(InvocationHandlerAdapter invocationHandlerAdapter, ServerMonitor serverMonitor, ExecutorService executor) {
        super(invocationHandlerAdapter, serverMonitor, executor);
    }

    /**
     * Stop
     */
    public void stop() {
        super.stop();
    }

    /**
     * Create a Server Stream Driver.
     *
     * @return The Server Stream Driver.
     */
    protected AbstractServerStreamDriver createServerStreamDriver() {
        return new ServerObjectStreamDriver(serverMonitor, executor);
    }

}
