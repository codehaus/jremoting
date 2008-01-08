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
package org.codehaus.jremoting.client.encoders;

import org.codehaus.jremoting.client.StreamEncoder;
import org.codehaus.jremoting.client.StreamEncoding;
import org.codehaus.jremoting.ConnectionException;

import java.io.InputStream;
import java.io.OutputStream;

import com.thoughtworks.xstream.XStream;

public class XStreamEncoding implements StreamEncoding {

    private final XStream xStream;

    public XStreamEncoding(XStream xStream) {
        this.xStream = xStream;
    }

    public XStreamEncoding() {
        this(new XStream());
    }

    public StreamEncoder makeStreamEncoder(InputStream inputStream, OutputStream outputStream, ClassLoader facadesClassLoader) throws ConnectionException {
        return new XStreamEncoder(inputStream, outputStream, facadesClassLoader, xStream);
    }
}
