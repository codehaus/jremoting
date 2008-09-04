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

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.StreamConnection;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.requests.CloseConnection;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.JRemotingException;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class StreamTransport
 *
 * @author Paul Hammant
 *
 */
public abstract class StreamTransport extends StatefulTransport {

    private LinkedBlockingQueue<StreamConnection> streamConnections = new LinkedBlockingQueue<StreamConnection>();

    public StreamTransport(ClientMonitor clientMonitor, ScheduledExecutorService executorService,
                           ConnectionPinger connectionPinger, ClassLoader facadesClassLoader) {
        super(clientMonitor, executorService, connectionPinger, facadesClassLoader);
    }

    protected void addStreamEncoder(StreamConnection streamConnection) {
        try {
            streamConnections.put(streamConnection);
        } catch (InterruptedException e) {
        }
    }

    protected Response performInvocation(Request request) throws IOException, ClassNotFoundException {
        if (request instanceof CloseConnection) {
            return closeConnections(request);
        } else {
            return performUnderlyingInvocation(request);
        }
    }

    private Response closeConnections(Request request) throws ClassNotFoundException, IOException {
        Response resp = performUnderlyingInvocation(request);
        // close all connections
        while (streamConnections.peek() != null) {
            try {
                streamConnections.take().closeConnection();
            } catch (InterruptedException e) {
            }
        }
        return resp;
    }

    private Response performUnderlyingInvocation(Request request) throws ClassNotFoundException, IOException {
        StreamConnection se = null;
        boolean ioeCaught = false;
        try {
            //TODO there is a possibility that after IOE or IE, there are none left in the pool
            se =  streamConnections.take();
            return se.streamRequest(request);
        } catch (InterruptedException e) {
        } catch (IOException ioe) {
            ioeCaught = true;
            se.closeConnection();
            throw ioe;
        } finally {
            if (se != null && !ioeCaught) {
                this.addStreamEncoder(se);
            }
        }
        throw new JRemotingException("should never get here");
    }
}
