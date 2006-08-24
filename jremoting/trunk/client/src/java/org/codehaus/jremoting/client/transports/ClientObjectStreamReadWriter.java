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

import org.codehaus.jremoting.api.BadConnectionException;
import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.client.ClientStreamReadWriter;
import org.codehaus.jremoting.commands.Request;
import org.codehaus.jremoting.commands.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;

/**
 * Class ClientObjectStreamReadWriter
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class ClientObjectStreamReadWriter implements ClientStreamReadWriter {

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    /**
     * Constructor ClientObjectStreamReadWriter
     *
     * @param inputStream
     * @param outputStream
     * @throws ConnectionException
     */
    public ClientObjectStreamReadWriter(InputStream inputStream, OutputStream outputStream) throws ConnectionException {

        try {
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectInputStream = new ObjectInputStream(new BufferedInputStream(inputStream));
        } catch (EOFException eofe) {
            throw new BadConnectionException("Cannot connect to remote JRemoting server. Have we a mismatch on transports?");
        } catch (IOException ioe) {
            throw new ConnectionException("Some problem instantiating ObjectStream classes: " + ioe.getMessage());
        }
    }

    public synchronized Response postRequest(Request request) throws IOException, ClassNotFoundException {
        writeRequest(request);
        return readReply();
    }

    private void writeRequest(Request request) throws IOException {

        objectOutputStream.writeObject(request);
        objectOutputStream.flush();

        objectOutputStream.reset();
    }

    private Response readReply() throws IOException, ClassNotFoundException {
        return (Response) objectInputStream.readObject();
    }
}
