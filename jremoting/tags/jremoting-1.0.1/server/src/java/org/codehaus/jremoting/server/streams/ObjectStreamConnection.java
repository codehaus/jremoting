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
package org.codehaus.jremoting.server.streams;

import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.util.ClassLoaderObjectInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


/**
 * Class ObjectStreamConnection
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class ObjectStreamConnection extends AbstractStreamConnection {

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public ObjectStreamConnection(ServerMonitor serverMonitor, ClassLoader facadesClassLoader, InputStream inputStream,
                                    OutputStream outputStream, String connectionDetails) {
        super(serverMonitor, inputStream, outputStream, facadesClassLoader, connectionDetails);
    }

    protected void writeResponse(Response response) throws IOException {
        objectOutputStream.writeObject(response);
        objectOutputStream.flush();
        objectOutputStream.reset();
    }

    public void closeConnection() {
        try {
            if (objectInputStream != null) {
                objectInputStream.close();
            }
        } catch (IOException e) {
        }
        try {
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
        } catch (IOException e) {
        }
        super.closeConnection();
    }

    public void initialize() throws IOException {
        objectInputStream = new ClassLoaderObjectInputStream(getFacadesClassLoader(), new BufferedInputStream(getInputStream()));
        objectOutputStream = new ObjectOutputStream(getOutputStream());
    }

    protected Request readRequest() throws IOException, ClassNotFoundException {
        return (Request) objectInputStream.readObject();
    }
}
