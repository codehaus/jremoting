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
package org.codehaus.jremoting.client.encoders;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.util.ClassLoaderObjectInputStream;
import org.codehaus.jremoting.client.StreamEncoder;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;

/**
 * Class ObjectStreamEncoder
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class ObjectStreamEncoder implements StreamEncoder {

    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;

    public ObjectStreamEncoder(InputStream inputStream, OutputStream outputStream, ClassLoader facadesClassLoader) throws ConnectionException {
        try {
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectInputStream = new ClassLoaderObjectInputStream(facadesClassLoader, new BufferedInputStream(inputStream));
        } catch (EOFException eofe) {
            throw new ConnectionException("Cannot connect to remote JRemoting server. Have we a mismatch on transports?");
        } catch (IOException ioe) {
            throw new ConnectionException("Some problem instantiating ObjectStream classes: " + ioe.getMessage());
        }
    }

    public synchronized Response postRequest(Request request) throws IOException, ClassNotFoundException {
        writeRequest(request);
        return readResponse();
    }

    private void writeRequest(Request request) throws IOException {
        objectOutputStream.writeObject(request);
        objectOutputStream.flush();
        objectOutputStream.reset();
    }

    private Response readResponse() throws IOException, ClassNotFoundException {
        return (Response) objectInputStream.readObject();
    }
}
