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

import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.StreamConnection;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Class AbstractStreamConnection
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public abstract class AbstractStreamConnection implements StreamConnection {

    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final ServerMonitor serverMonitor;
    private final ClassLoader facadesClassLoader;
    private final String connectionDetails;

    public AbstractStreamConnection(ServerMonitor serverMonitor,
                                      InputStream inputStream, OutputStream outputStream,
                                      ClassLoader facadesClassLoader, String connectionDetails) {
        this.serverMonitor = serverMonitor;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.facadesClassLoader = facadesClassLoader;
        this.connectionDetails = connectionDetails;
    }

    public final synchronized Request writeResponseAndGetRequest(Response response) throws IOException, ClassNotFoundException {
        if (response != null) {
            writeResponse(response);
        }
        return readRequest();
    }

    protected abstract Request readRequest() throws IOException, ClassNotFoundException;
    protected abstract void writeResponse(Response response) throws IOException;

    public String getConnectionDetails() {
        return connectionDetails;
    }

    public void closeConnection() {
        try {
            inputStream.close();
        } catch (IOException e) {
            serverMonitor.closeError(this.getClass(), "AbstractStreamConnection.closeConnection(): Failed closing an JRemoting connection input stream: ", e);
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            serverMonitor.closeError(this.getClass(), "AbstractStreamConnection.closeConnection(): Failed closing an JRemoting connection output stream: ", e);
        }
    }

    protected InputStream getInputStream() {
        return inputStream;
    }

    protected OutputStream getOutputStream() {
        return outputStream;
    }

    public ClassLoader getFacadesClassLoader() {
        return facadesClassLoader;
    }
}
