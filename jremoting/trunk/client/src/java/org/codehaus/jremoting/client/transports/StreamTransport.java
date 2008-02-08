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
import org.codehaus.jremoting.client.StreamEncoder;
import org.codehaus.jremoting.client.StreamEncoding;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class StreamTransport
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public abstract class StreamTransport extends StatefulTransport {

    protected final StreamEncoding streamEncoding;
    private LinkedBlockingQueue<StreamEncoder> encoders = new LinkedBlockingQueue<StreamEncoder>();

    public StreamTransport(ClientMonitor clientMonitor, ScheduledExecutorService executorService,
                                                 ConnectionPinger connectionPinger, ClassLoader facadesClassLoader,
                                                 StreamEncoding streamEncoding) {
        super(clientMonitor, executorService, connectionPinger, facadesClassLoader);
        this.streamEncoding = streamEncoding;

    }

    protected void addStreamEncoder(StreamEncoder streamEncoder) {
        try {
            encoders.put(streamEncoder);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected Response performInvocation(Request request) throws IOException, ClassNotFoundException {
        StreamEncoder se = null;
        try {
            se =  encoders.take();
            return se.postRequest(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (se != null) {
                try {
                    encoders.put(se);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


}
