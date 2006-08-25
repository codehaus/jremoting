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

import org.codehaus.jremoting.client.ClientStreamDriver;
import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.requests.AbstractRequest;

import java.io.*;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Class ClientXStreamStreamDriver
 *
 * @author Paul Hammant
 * @version $Revision: 1.3 $
 */
public class ClientXStreamStreamDriver implements ClientStreamDriver {

    private LineNumberReader lineNumberReader;
    private PrintWriter printWriter;
    private ClassLoader interfacesClassLoader;
    private XStream xStream;



    public ClientXStreamStreamDriver(InputStream inputStream, OutputStream outputStream, ClassLoader interfacesClassLoader) throws ConnectionException {

        printWriter = new PrintWriter(new BufferedOutputStream(outputStream));
        lineNumberReader = new LineNumberReader(new InputStreamReader(inputStream));
        this.interfacesClassLoader = interfacesClassLoader;
        xStream = new XStream(new DomDriver());
    }

    public synchronized Response postRequest(AbstractRequest request) throws IOException, ClassNotFoundException {

        writeRequest(request);

        Response r = readReply();

        return r;
    }

    private void writeRequest(AbstractRequest request) {

        String xml = xStream.toXML(request);

        //System.out.println("--> client write>\n" + xml.replace(' ', '_'));

        printWriter.write(xml + "\n");
        printWriter.flush();
    }

    private Response readReply() throws IOException {

        StringBuffer res = new StringBuffer();
        String line = lineNumberReader.readLine();
        res.append(line);
        line = lineNumberReader.readLine();
        while (line != null) {
            res.append("\n").append(line);
            if (!Character.isWhitespace(line.charAt(0))) {
                line = null;
            } else {
                line = lineNumberReader.readLine();
            }
        }

        // todo ClassLoader magic ?  or use Reader with XStream direct ?
        String expected = res.toString() + "\n";

        if (expected.equals("null\n")) {
            // TODO weird bug - chase it down?
            return null;
        }

        try {
            return (Response) xStream.fromXML(expected);
        } catch (ConversionException e) {
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof ClassCastException) {
                throw (ClassCastException) cause;
            } else {
                throw e;
            }
        }

        //TODO use interfacesClassLoader
        //Object reply = SerializationHelper.getInstanceFromBytes(byteArray, interfacesClassLoader);
    }
}
