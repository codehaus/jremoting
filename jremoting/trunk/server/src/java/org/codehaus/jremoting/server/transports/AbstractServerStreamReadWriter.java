/* ====================================================================
 * Copyright 2005 JRemoting Committers
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

import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.api.ThreadPoolAware;
import org.codehaus.jremoting.commands.Request;
import org.codehaus.jremoting.commands.Response;
import org.codehaus.jremoting.server.ServerMonitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Class AbstractServerStreamReadWriter
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public abstract class AbstractServerStreamReadWriter implements ThreadPoolAware {


    /**
     * The input stream
     */
    private InputStream inputStream;

    /**
     * The output stream
     */
    private OutputStream outputStream;

    protected final ServerMonitor serverMonitor;
    protected final ThreadPool threadPool;
    private Object connectionDetails;

    public AbstractServerStreamReadWriter(ServerMonitor serverMonitor, ThreadPool threadPool) {
        this.serverMonitor = serverMonitor;
        this.threadPool = threadPool;
    }

    /**
     * Method setStreams
     *
     * @param inputStream  The input stream
     * @param outputStream the outpur stream
     */
    public final void setStreams(InputStream inputStream, OutputStream outputStream, Object connectionDetails) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.connectionDetails = connectionDetails;
    }

    public Object getConnectionDetails() {
        return connectionDetails;
    }


    /**
     * Initialize the Read Writer.
     *
     * @throws IOException if a problem during initialization.
     */
    protected abstract void initialize() throws IOException;

    /**
     * Write a Response, then Get a new Request over the stream.
     *
     * @param response The response to pass back to the client
     * @return The Request that is new and incoming
     * @throws IOException            if a problem during write & read.
     * @throws ConnectionException    if a problem during write & read.
     * @throws ClassNotFoundException If a Class is not found during serialization.
     */
    protected abstract Request writeReplyAndGetRequest(Response response) throws IOException, ConnectionException, ClassNotFoundException;

    /**
     * Close the stream.
     */
    protected void close() {
        try {
            inputStream.close();
        } catch (IOException e) {
            serverMonitor.closeError(this.getClass(), "AbstractServerStreamReadWriter.close(): Failed closing an JRemoting connection input stream: ", e);
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            serverMonitor.closeError(this.getClass(), "AbstractServerStreamReadWriter.close(): Failed closing an JRemoting connection output stream: ", e);
        }
    }

    /**
     * Get the Input stream
     *
     * @return The input stream
     */
    protected InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Get the Output stream
     *
     * @return The Output stream
     */
    protected OutputStream getOutputStream() {
        return outputStream;
    }
}
