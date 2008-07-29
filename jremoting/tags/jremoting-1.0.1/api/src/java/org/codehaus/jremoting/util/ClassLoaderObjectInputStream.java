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
package org.codehaus.jremoting.util;

import java.io.*;

/**
 * A special ObjectInputStream to handle highly transient classes hosted
 * by containers that are juggling many classloaders.
 *
 * @author Paul Hammant
 */
public class ClassLoaderObjectInputStream extends ObjectInputStream {

    private ClassLoader facadesClassLoader;

    public ClassLoaderObjectInputStream(final ClassLoader facadesClassLoader, InputStream inputStream) throws IOException, StreamCorruptedException {

        super(inputStream);

        this.facadesClassLoader = facadesClassLoader;
    }

    protected Class resolveClass(final ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {

        Class clazz = null;

        try {
            clazz = facadesClassLoader.loadClass(objectStreamClass.getName());
        } catch (ClassNotFoundException cnfe) {
            // this may be OK, see below.
        }

        if (null != clazz) {
            return clazz;    // the classloader knows of the class
        } else {

            // classloader knows not of class, let the super classloader do it
            //printCLs();
            return super.resolveClass(objectStreamClass);
        }
    }
}
