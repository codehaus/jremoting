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
package org.codehaus.jremoting.client.transports;

import org.codehaus.jremoting.JRemotingException;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.transports.StatefulTransport;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.MarshalledInvocationHandler;
import org.codehaus.jremoting.util.SerializationHelper;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Class DirectMarshalledTransport
 *
 * @author Paul Hammant
 */
public final class DirectMarshalledTransport extends StatefulTransport {

    private final MarshalledInvocationHandler invoker;

    public DirectMarshalledTransport(ClientMonitor clientMonitor, ScheduledExecutorService executorService,
                                             ConnectionPinger connectionPinger, MarshalledInvocationHandler invoker,
                                             ClassLoader facadesClassLoader) {
        super(clientMonitor, executorService, connectionPinger, facadesClassLoader);
        this.invoker = invoker;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean tryReconnect() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    protected Response performInvocation(Request request) {

        byte[] serRequest = SerializationHelper.getBytesFromInstance(request);
        try {
            byte[] serResponse = invoker.invoke(serRequest, null);
            return (Response) SerializationHelper.getInstanceFromBytes(serResponse, getFacadesClassLoader());
        } catch (ClassNotFoundException cnfe) {
            String msg = "Some ClassNotFoundException on client side";
            clientMonitor.classNotFound(DirectMarshalledTransport.class, msg, cnfe);
            throw new JRemotingException(msg, cnfe);
        }
    }

}
