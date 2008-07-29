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
package org.codehaus.jremoting.server.streams;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.AnnotationProvider;
import com.thoughtworks.xstream.annotations.AnnotationReflectionConverter;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.core.JVM;
import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.util.SerializationHelper;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Class XStreamConnection
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class XStreamConnection extends AbstractStreamConnection {

    private LineNumberReader lineNumberReader;
    private PrintWriter printWriter;
    private XStream xStream;
    private BufferedOutputStream bufferedOutputStream;

    public XStreamConnection(ServerMonitor serverMonitor, ClassLoader facadesClassLoader, InputStream inputStream,
                               OutputStream outputStream, String connectionDetails, XStream xStream) {
        super(serverMonitor, inputStream, outputStream, facadesClassLoader, connectionDetails);
        this.xStream = xStream;
        xStream.registerConverter(new AnnotationReflectionConverter(
                xStream.getMapper(),
                new JVM().bestReflectionProvider(),
                new AnnotationProvider()) {
            public boolean canConvert(Class type) {
                return Request.class.isAssignableFrom(type) | Response.class.isAssignableFrom(type);
            }
        }, XStream.PRIORITY_LOW);

    }

    protected void writeResponse(Response response) throws IOException {
        String xml = xStream.toXML(response);
        printWriter.write(xml + "\n");
        printWriter.flush();
        bufferedOutputStream.flush();
    }

    public void closeConnection() {
        try {
            getInputStream().close();
        } catch (IOException e) {
        }
        try {
            lineNumberReader.close();
        } catch (IOException e) {
        }
        printWriter.close();
        super.closeConnection();
    }

    public void initialize() throws IOException {
        lineNumberReader = new LineNumberReader(new BufferedReader(new InputStreamReader(getInputStream())));
        bufferedOutputStream = new BufferedOutputStream(getOutputStream());
        printWriter = new PrintWriter(bufferedOutputStream);
    }

    protected Request readRequest() throws IOException, ClassNotFoundException, ConnectionException {
        String xml = SerializationHelper.getXml(lineNumberReader);
        try {
            Object o = xStream.fromXML(xml);
            return (Request) o;
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
