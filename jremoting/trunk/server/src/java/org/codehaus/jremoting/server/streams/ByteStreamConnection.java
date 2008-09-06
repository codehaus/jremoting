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

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.ConnectionClosed;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.StreamConnection;
import org.codehaus.jremoting.util.SerializationHelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Class ByteStreamConnection
 *
 * @author Paul Hammant
 *
 */
public class ByteStreamConnection extends StreamConnection {

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public ByteStreamConnection(ServerMonitor serverMonitor, DataInputStream dataInputStream,
                                    DataOutputStream dataOutputStream,
                                    ClassLoader facadesClassLoader, String connectionDetails) {
        super(serverMonitor, dataInputStream, dataOutputStream, facadesClassLoader, connectionDetails);
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
    }

    /**
     * {@inheritDoc}
     */
    protected void writeResponse(Response response) throws IOException {
        byte[] aBytes = SerializationHelper.getBytesFromInstance(response);
        dataOutputStream.writeInt(aBytes.length);
        dataOutputStream.write(aBytes);
        dataOutputStream.flush();
    }

    /**
     * {@inheritDoc}
     */
    public void closeConnection() {
        try {
            writeResponse(new ConnectionClosed());
        } catch (IOException e) {
        }
        closeCloseable(dataInputStream, "data input stream");
        closeCloseable(dataOutputStream, "data output stream");
        super.closeConnection();
    }

    /**
     * {@inheritDoc}
     */
    protected Request readRequest() throws IOException, ClassNotFoundException {
        int byteArraySize = dataInputStream.readInt();
        if (byteArraySize < 0) {
            throw new ConnectionException("Transport mismatch, Unable to " + "read packet of data from ByteStream.");
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
