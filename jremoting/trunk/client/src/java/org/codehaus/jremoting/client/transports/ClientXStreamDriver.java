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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.AnnotationProvider;
import com.thoughtworks.xstream.annotations.AnnotationReflectionConverter;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.ClientStreamDriver;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;
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
 * Class ClientXStreamDriver
 *
 * @author Paul Hammant
 * @version $Revision: 1.3 $
 */
public class ClientXStreamDriver implements ClientStreamDriver {

    private final LineNumberReader lineNumberReader;
    private final PrintWriter printWriter;
    private final XStream xStream;
    private final BufferedOutputStream bufferedOutputStream;

    public ClientXStreamDriver(InputStream inputStream, OutputStream outputStream, ClassLoader facadesClassLoader) throws ConnectionException {
        bufferedOutputStream = new BufferedOutputStream(outputStream);
        printWriter = new PrintWriter(bufferedOutputStream);
        lineNumberReader = new LineNumberReader(new BufferedReader(new InputStreamReader(inputStream)));
        xStream = new XStream(new DomDriver());
        xStream.setClassLoader(facadesClassLoader);

        xStream.registerConverter(new AnnotationReflectionConverter(
                xStream.getMapper(),
                new JVM().bestReflectionProvider(),
                new AnnotationProvider()) {
            public boolean canConvert(Class type) {
                return Request.class.isAssignableFrom(type) | Response.class.isAssignableFrom(type);
            }
        }, XStream.PRIORITY_LOW);


    }

    public synchronized Response postRequest(Request request) throws IOException, ClassNotFoundException {
        writeRequest(request);
        return readResponse();
    }

    private void writeRequest(Request request) throws IOException {
        String xml = xStream.toXML(request);
        printWriter.write(xml + "\n");
        printWriter.flush();
        bufferedOutputStream.flush();
    }

    private Response readResponse() throws IOException {
        String xml = SerializationHelper.getXml(lineNumberReader);
        try {
            return (Response) xStream.fromXML(xml);
        } catch (ConversionException e) {
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof ClassCastException) {
                throw(ClassCastException) cause;
            } else {
                throw e;
            }
        }
    }

}
