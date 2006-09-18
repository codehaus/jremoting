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
package org.codehaus.jremoting.server.transports;

import java.util.concurrent.ExecutorService;
import org.codehaus.jremoting.requests.AbstractRequest;
import org.codehaus.jremoting.responses.AbstractResponse;
import org.codehaus.jremoting.server.ServerMonitor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Class ServerObjectStreamDriver
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class ServerObjectStreamDriver extends AbstractServerStreamDriver {

    /**
     * The Object Input Stream
     */
    private ObjectInputStream objectInputStream;

    /**
     * The Object Output Stream
     */
    private ObjectOutputStream objectOutputStream;

    /**
     * Constructor ServerObjectStreamDriver
     */
    public ServerObjectStreamDriver(ServerMonitor serverMonitor, ExecutorService executor) {
        super(serverMonitor, executor);
    }

    /**
     * Initialize
     *
     * @throws IOException if an IO Excpetion
     */
    protected void initialize() throws IOException {
        objectInputStream = new ObjectInputStream(new BufferedInputStream(getInputStream()));
        objectOutputStream = new ObjectOutputStream(getOutputStream());
    }

    /**
     * Write a response, and wait for a request
     *
     * @param response The response to send
     * @return The new request
     * @throws IOException            In an IO Exception
     * @throws ClassNotFoundException If a class not found during deserialization.
     */
    protected synchronized AbstractRequest writeReplyAndGetRequest(AbstractResponse response) throws IOException, ClassNotFoundException {

        if (response != null) {
            writeReply(response);
        }

        return readRequest();
    }

    /**
     * Write a rpely.
     *
     * @param response The response to write
     * @throws IOException If and IO Exception
     */
    private void writeReply(AbstractResponse response) throws IOException {

        objectOutputStream.writeObject(response);
        objectOutputStream.flush();

        objectOutputStream.reset();
    }

    protected void close() {
        try {
            objectInputStream.close();
        } catch (IOException e) {
        }
        try {
            objectOutputStream.close();
        } catch (IOException e) {
        }
        super.close();
    }

    /**
     * Read a request
     *
     * @return The request
     * @throws IOException            If an IO Exception
     * @throws ClassNotFoundException If a class not found during deserialization.
     */
    private AbstractRequest readRequest() throws IOException, ClassNotFoundException {
        AbstractRequest request = (AbstractRequest) objectInputStream.readObject();
        return request;
    }
}
