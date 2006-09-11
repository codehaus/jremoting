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
package org.codehaus.jremoting.server.classretrievers;

import org.codehaus.jremoting.server.StubRetrievalException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class PlainStubRetriever
 *
 * @author Paul Hammant
 * @author Vinay Chandrasekharan <a href="mailto:vinay_chandran@users.sourceforge.net">
 *         vinay_chandran@users.sourceforge.net</a>
 * @version $Revision: 1.2 $
 */
public class PlainStubRetriever extends AbstractStubRetriever {

    private ClassLoader classLoader;

    /**
     * Constructor PlainStubRetriever
     */
    public PlainStubRetriever() {
        classLoader = this.getClass().getClassLoader();
    }

    /**
     * Create a plain clasretriever from a classloader.
     *
     * @param classLoader the classloader.
     */
    public PlainStubRetriever(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Get the classfile byte array for the thing name
     *
     * @param thingName the things name
     * @return the byte array of the thing.
     * @throws StubRetrievalException if the bytes are not available.
     */
    protected byte[] getThingBytes(String thingName) throws StubRetrievalException {

        InputStream is = null;

        is = classLoader.getResourceAsStream(thingName + ".class");

        if (is == null) {
            throw new StubRetrievalException("Generated class for " + thingName + " not found in specified classloader ");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = 0;

        try {
            while (-1 != (i = is.read())) {
                baos.write(i);
            }

            is.close();
        } catch (IOException e) {
            throw new StubRetrievalException("Error retrieving generated class bytes : " + e.getMessage());
        }

        return baos.toByteArray();
    }
}
