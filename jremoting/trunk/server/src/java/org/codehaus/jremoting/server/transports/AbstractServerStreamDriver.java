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
package org.codehaus.jremoting.server.transports;

import java.util.concurrent.ExecutorService;
import org.codehaus.jremoting.server.ServerMonitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Class AbstractServerStreamDriver
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public abstract class AbstractServerStreamDriver implements ServerStreamDriver {

    private InputStream inputStream;

    private OutputStream outputStream;

    protected final ServerMonitor serverMonitor;
    protected final ExecutorService executorService;
    private Object connectionDetails;

    public AbstractServerStreamDriver(ServerMonitor serverMonitor, ExecutorService executorService) {
        this.serverMonitor = serverMonitor;
        this.executorService = executorService;
    }


    public final void setStreams(InputStream inputStream, OutputStream outputStream, Object connectionDetails) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.connectionDetails = connectionDetails;
    }

    public Object getConnectionDetails() {
        return connectionDetails;
    }

    /**
     * Close the stream.
     */
    public void close() {
        try {
            inputStream.close();
        } catch (IOException e) {
            serverMonitor.closeError(this.getClass(), "AbstractServerStreamDriver.close(): Failed closing an JRemoting connection input stream: ", e);
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            serverMonitor.closeError(this.getClass(), "AbstractServerStreamDriver.close(): Failed closing an JRemoting connection output stream: ", e);
        }
    }

    protected InputStream getInputStream() {
        return inputStream;
    }

    protected OutputStream getOutputStream() {
        return outputStream;
    }
}
