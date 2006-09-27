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

import org.codehaus.jremoting.BadConnectionException;
import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.ClientStreamDriver;
import org.codehaus.jremoting.requests.AbstractRequest;
import org.codehaus.jremoting.responses.AbstractResponse;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;

/**
 * Class ClientObjectStreamDriver
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class ClientObjectStreamDriver implements ClientStreamDriver {

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private ClassLoader facadesClassLoader;

    /**
     * Constructor ClientObjectStreamDriver
     *
     * @param in
     * @param out
     * @throws ConnectionException
     */
    public ClientObjectStreamDriver(InputStream in, OutputStream out) throws ConnectionException {
        this(in, out, null);
    }


    /**
     * Constructor ClientObjectStreamDriver
     *
     * @param inputStream
     * @param outputStream
     * @param facadesClassLoader
     * @throws ConnectionException
     */
    public ClientObjectStreamDriver(InputStream inputStream, OutputStream outputStream, ClassLoader facadesClassLoader) throws ConnectionException {
        try {
            objectOutputStream = new ObjectOutputStream(outputStream);
            //TODO use that magix classloader that uses the facades classlaoader
            this.facadesClassLoader = facadesClassLoader;
            objectInputStream = new ObjectInputStream(new BufferedInputStream(inputStream));
        } catch (EOFException eofe) {
            throw new BadConnectionException("Cannot connect to remote JRemoting server. Have we a mismatch on transports?");
        } catch (IOException ioe) {
            throw new ConnectionException("Some problem instantiating ObjectStream classes: " + ioe.getMessage());
        }
    }

    public synchronized AbstractResponse postRequest(AbstractRequest request) throws IOException, ClassNotFoundException {
        writeRequest(request);
        return readResponse();
    }

    private void writeRequest(AbstractRequest request) throws IOException {
        objectOutputStream.writeObject(request);
        objectOutputStream.flush();
        objectOutputStream.reset();
    }

    private AbstractResponse readResponse() throws IOException, ClassNotFoundException {
        return (AbstractResponse) objectInputStream.readObject();
    }
}
