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

import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.api.SerializationHelper;
import org.codehaus.jremoting.client.ClientStreamReadWriter;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class ClientCustomStreamReadWriter
 *
 * @author Paul Hammant
 * @version $Revision: 1.3 $
 */
public class ClientCustomStreamReadWriter implements ClientStreamReadWriter {

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private ClassLoader interfacesClassLoader;


    /**
     * Constructor ClientCustomStreamReadWriter
     *
     * @param inputStream
     * @param outputStream
     * @param interfacesClassLoader
     * @throws ConnectionException
     */
    public ClientCustomStreamReadWriter(InputStream inputStream, OutputStream outputStream, ClassLoader interfacesClassLoader) throws ConnectionException {

        dataOutputStream = new DataOutputStream(new BufferedOutputStream(outputStream));
        dataInputStream = new DataInputStream(inputStream);
        this.interfacesClassLoader = interfacesClassLoader;
    }

    public synchronized Response postRequest(Request request) throws IOException, ClassNotFoundException {

        writeRequest(request);

        Response r = readReply();

        return r;
    }

    private void writeRequest(Request request) throws IOException {

        byte[] aBytes = SerializationHelper.getBytesFromInstance(request);

        dataOutputStream.writeInt(aBytes.length);
        dataOutputStream.write(aBytes);
        dataOutputStream.flush();
    }

    private Response readReply() throws IOException, ClassNotFoundException {

        int byteArraySize = dataInputStream.readInt();
        byte[] byteArray = new byte[byteArraySize];
        int pos = 0;
        int cnt = 0;
        // Loop here until the entire array has been read in.
        while (pos < byteArraySize) {
            int read = dataInputStream.read(byteArray, pos, byteArraySize - pos);
            pos += read;
            cnt++;
        }
        Object reply = SerializationHelper.getInstanceFromBytes(byteArray, interfacesClassLoader);
        return (Response) reply;
    }
}
