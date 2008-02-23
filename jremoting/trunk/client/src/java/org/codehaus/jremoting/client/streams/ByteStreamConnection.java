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
package org.codehaus.jremoting.client.streams;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.StreamConnection;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.util.SerializationHelper;

import java.io.*;

/**
 * Class ByteStreamConnection
 *
 * @author Paul Hammant
 * @version $Revision: 1.3 $
 */
public class ByteStreamConnection implements StreamConnection {

    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private final ClassLoader facadesClassLoader;

    public ByteStreamConnection(DataInputStream dataInputStream, DataOutputStream dataOutputStream, ClassLoader facadesClassLoader) throws ConnectionException {
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.facadesClassLoader = facadesClassLoader;
    }

    public synchronized Response streamRequest(Request request) throws IOException, ClassNotFoundException {
        if (dataInputStream.available() != 0) {
            return readResponse();
        }
        writeRequest(request);
        return readResponse();
    }

    public void closeConnection() {
        try {
            dataInputStream.close();
        } catch (IOException e) {
        }
        try {
            dataOutputStream.close();
        } catch (IOException e) {
        }
    }

    private void writeRequest(Request request) throws IOException {
        byte[] aBytes = SerializationHelper.getBytesFromInstance(request);
        dataOutputStream.writeInt(aBytes.length);
        dataOutputStream.write(aBytes);
        dataOutputStream.flush();
    }

    private Response readResponse() throws IOException, ClassNotFoundException {

        int byteArraySize = dataInputStream.readInt();
        byte[] byteArray = new byte[byteArraySize];
        int pos = 0;
        while (pos < byteArraySize) {
            pos += dataInputStream.read(byteArray, pos, byteArraySize - pos);
        }
        return (Response) SerializationHelper.getInstanceFromBytes(byteArray, facadesClassLoader);
    }
}
