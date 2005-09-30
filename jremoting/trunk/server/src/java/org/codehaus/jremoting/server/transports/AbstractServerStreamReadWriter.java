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

import org.codehaus.jremoting.commands.Response;
import org.codehaus.jremoting.commands.Request;
import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.api.ThreadPoolAware;
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
    private InputStream m_inputStream;

    /**
     * The output stream
     */
    private OutputStream m_outputStream;

    protected final ServerMonitor m_serverMonitor;
    protected final ThreadPool m_threadPool;
    private Object m_connectionDetails;

    public AbstractServerStreamReadWriter(ServerMonitor serverMonitor, ThreadPool threadPool) {
        m_serverMonitor = serverMonitor;
        m_threadPool = threadPool;
    }

    /**
     * Method setStreams
     *
     * @param inputStream  The input stream
     * @param outputStream the outpur stream
     */
    public final void setStreams(InputStream inputStream, OutputStream outputStream, Object connectionDetails) {
        m_inputStream = inputStream;
        m_outputStream = outputStream;
        m_connectionDetails = connectionDetails;
    }

    public Object getConnectionDetails() {
        return m_connectionDetails;
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
            m_inputStream.close();
        } catch (IOException e) {
            m_serverMonitor.closeError(this.getClass(), "AbstractServerStreamReadWriter.close(): Failed closing an JRemoting Remoting connection input stream: ", e);
        }

        try {
            m_outputStream.close();
        } catch (IOException e) {
            m_serverMonitor.closeError(this.getClass(), "AbstractServerStreamReadWriter.close(): Failed closing an JRemoting Remoting connection output stream: ", e);
        }
    }

    /**
     * Get the Input stream
     *
     * @return The input stream
     */
    protected InputStream getInputStream() {
        return m_inputStream;
    }

    /**
     * Get the Output stream
     *
     * @return The Output stream
     */
    protected OutputStream getOutputStream() {
        return m_outputStream;
    }
}
