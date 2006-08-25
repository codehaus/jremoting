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

import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.requests.AbstractRequest;
import org.codehaus.jremoting.responses.Response;

import java.io.*;

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


    public ServerXStreamDriver(ServerMonitor serverMonitor, ThreadPool threadPool) {
        super(serverMonitor, threadPool);
        xStream = new XStream(new DomDriver());
    }


    protected void initialize() throws IOException {
        lineNumberReader = new LineNumberReader(new InputStreamReader(getInputStream()));
        printWriter = new PrintWriter(new BufferedOutputStream(getOutputStream()));
    }

    protected synchronized AbstractRequest writeReplyAndGetRequest(Response response) throws IOException, ClassNotFoundException, ConnectionException {

        if (response != null) {
            writeReply(response);
        }

        return readRequest();
    }

    private void writeReply(Response response) throws IOException {


        String xml = xStream.toXML(response);

        printWriter.write(xml + "\n");
        printWriter.flush();
    }

    protected void close() {
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

    private AbstractRequest readRequest() throws IOException, ClassNotFoundException, ConnectionException {
        long start = System.currentTimeMillis();
        StringBuffer req = new StringBuffer();

        String line = lineNumberReader.readLine();
        req.append(line);
        line = lineNumberReader.readLine();
        while (line != null) {
            req.append("\n").append(line);
            if (!Character.isWhitespace(line.charAt(0))) {
                line = null;
            } else {
                line = lineNumberReader.readLine();
            }
        }

        // todo ClassLoader magic ?  or use Reader with XStream direct ?
        String r = req.toString() + "\n";

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
