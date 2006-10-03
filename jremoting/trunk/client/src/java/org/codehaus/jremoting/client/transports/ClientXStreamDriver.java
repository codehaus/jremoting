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
package org.codehaus.jremoting.client.transports;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.ClientStreamDriver;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Class ClientXStreamDriver
 *
 * @author Paul Hammant
 * @version $Revision: 1.3 $
 */
public class ClientXStreamDriver implements ClientStreamDriver {

    private LineNumberReader lineNumberReader;
    private PrintWriter printWriter;
    private ClassLoader facadesClassLoader;
    private XStream xStream;
    private BufferedOutputStream bufferedOutputStream;


    public ClientXStreamDriver(InputStream inputStream, OutputStream outputStream, ClassLoader facadesClassLoader) throws ConnectionException {

        bufferedOutputStream = new BufferedOutputStream(outputStream);
        printWriter = new PrintWriter(bufferedOutputStream);
        lineNumberReader = new LineNumberReader(new BufferedReader(new InputStreamReader(inputStream)));
        this.facadesClassLoader = facadesClassLoader;
        xStream = new XStream(new DomDriver());
    }

    public synchronized Response postRequest(Request request) throws IOException, ClassNotFoundException {

        writeRequest(request);

        Response r = readResponse();

        return r;
    }

    private void writeRequest(Request request) throws IOException {

        String xml = xStream.toXML(request);

        printWriter.write(xml + "\n");
        printWriter.flush();
        bufferedOutputStream.flush();
    }

    private Response readResponse() throws IOException {

        String xml = getXml();

        try {
            //TODO use facadesClassLoader
            return (Response) xStream.fromXML(xml);
        } catch (ConversionException e) {
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof ClassCastException) {
                throw (ClassCastException) cause;
            } else {
                throw e;
            }
        }
    }

    protected String getXml() throws IOException {
        StringBuffer obj = new StringBuffer();
        String line = lineNumberReader.readLine();
        obj.append(line).append("\n");
        if (!(line.endsWith("/>"))) {
            line = lineNumberReader.readLine();
            while (line != null) {
                obj.append(line).append("\n");
                if (!Character.isWhitespace(line.charAt(0))) {
                    line = null;
                } else {
                    line = lineNumberReader.readLine();
                }
            }

        }
        return obj.toString();
    }
}
