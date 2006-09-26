/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.requests.AbstractRequest;
import org.codehaus.jremoting.responses.AbstractResponse;
import org.codehaus.jremoting.server.ServerMonitor;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Class ServerXStreamDriver
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class ServerXStreamDriver extends AbstractServerStreamDriver {

    private LineNumberReader lineNumberReader;
    private PrintWriter printWriter;
    private XStream xStream;
    private BufferedOutputStream bufferedOutputStream;

    public ServerXStreamDriver(ServerMonitor serverMonitor, ExecutorService executorService, InputStream inputStream,
                               OutputStream outputStream, Object connectionDetails) {
        super(serverMonitor, executorService, inputStream, outputStream, connectionDetails);
        xStream = new XStream(new DomDriver());
    }

    public synchronized AbstractRequest writeResponseAndGetRequest(AbstractResponse response) throws IOException, ClassNotFoundException, ConnectionException {

        if (response != null) {
            writeResponse(response);
        }

        return readRequest();
    }

    private void writeResponse(AbstractResponse response) throws IOException {


        String xml = xStream.toXML(response);

        printWriter.write(xml + "\n");
        printWriter.flush();
        bufferedOutputStream.flush();
    }

    public void close() {
        try {
            getInputStream().close();
        } catch (IOException e) {
        }
        try {
            lineNumberReader.close();
        } catch (IOException e) {
        }
        printWriter.close();
        super.close();
    }

    public void initialize() throws IOException {
        lineNumberReader = new LineNumberReader(new BufferedReader(new InputStreamReader(getInputStream())));
        bufferedOutputStream = new BufferedOutputStream(getOutputStream());
        printWriter = new PrintWriter(bufferedOutputStream);
    }

    private AbstractRequest readRequest() throws IOException, ClassNotFoundException, ConnectionException {
        StringBuffer req = new StringBuffer();
        String line = lineNumberReader.readLine();
        req.append(line).append("\n");
        if (!line.endsWith("/>")) {
            line = lineNumberReader.readLine();
            while (line != null) {
                req.append(line).append("\n");
                if (!Character.isWhitespace(line.charAt(0))) {
                    line = null;
                } else {
                    line = lineNumberReader.readLine();
                }
            }
        }

        // todo ClassLoader magic ?  or use Reader with XStream direct ?
        String r = req.toString();

        try {
            Object o = xStream.fromXML(r);
            return (AbstractRequest) o;
        } catch (ConversionException e) {
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof ClassCastException) {
                throw (ClassCastException) cause;
            } else {
                throw e;
            }
        }
    }
}
