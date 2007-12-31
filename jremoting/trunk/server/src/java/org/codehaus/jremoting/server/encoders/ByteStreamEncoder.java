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
package org.codehaus.jremoting.server.encoders;

import org.codehaus.jremoting.BadConnectionException;
import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.util.SerializationHelper;

import java.io.*;

/**
 * Class ByteStreamEncoder
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class ByteStreamEncoder extends AbstractStreamEncoder {

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public ByteStreamEncoder(ServerMonitor serverMonitor, DataInputStream dataInputStream,
                                    DataOutputStream dataOutputStream,
                                    ClassLoader facadesClassLoader, Object connectionDetails) {
        super(serverMonitor, dataInputStream, dataOutputStream, facadesClassLoader, connectionDetails);
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
    }

    /**
     * Write a response, and wait for a request
     *
     * @param response The response to send
     * @return The new request
     * @throws IOException            In an IO Exception
     * @throws ConnectionException    In an IO Exception
     * @throws ClassNotFoundException If a class not found during deserialization.
     */
    public synchronized Request writeResponseAndGetRequest(Response response) throws IOException, ClassNotFoundException, ConnectionException {

        if (response != null) {
            writeResponse(response);
        }

        return readRequest();
    }

    private void writeResponse(Response response) throws IOException {

        byte[] aBytes = SerializationHelper.getBytesFromInstance(response);

        dataOutputStream.writeInt(aBytes.length);
        dataOutputStream.write(aBytes);
        dataOutputStream.flush();
    }

    public void close() {
        try {
            dataInputStream.close();
        } catch (IOException e) {
        }
        try {
            dataOutputStream.close();
        } catch (IOException e) {
        }
        super.close();
    }

    public void initialize() {
    }

    private Request readRequest() throws IOException, ClassNotFoundException, ConnectionException {
        int byteArraySize = dataInputStream.readInt();
        int requestCode = dataInputStream.readInt();
        if (byteArraySize < 0) {
            throw new BadConnectionException("Transport mismatch, Unable to " + "read packet of data from ByteStream.");
        }
        byte[] byteArray = new byte[byteArraySize];
        int pos = 0;

        // Loop here until the entire array has been read in.
        while (pos < byteArraySize) {
            //TODO cater for DOS attack here.
            pos += dataInputStream.read(byteArray, pos, byteArraySize - pos);
        }

        return (Request) SerializationHelper.getInstanceFromBytes(byteArray, getFacadesClassLoader());
    }
}
