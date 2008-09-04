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
import java.net.SocketTimeoutException;

import org.codehaus.jremoting.JRemotingException;

/**
 * Class SerializationHelper
 *
 * @author Paul Hammant
 *
 */
public class SerializationHelper {

    /**
     * Get bytes from instance
     *
     * @param instance the object to turn in to serialized byte array
     * @return the byte array
     */
    public static byte[] getBytesFromInstance(Object instance) {

        ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
        ObjectOutputStream oOs;

        byte[] aBytes = new byte[0];
        try {
            oOs = new ObjectOutputStream(bAOS);

            oOs.writeObject(instance);
            oOs.flush();

            aBytes = bAOS.toByteArray();

            oOs.close();
            bAOS.close();
        } catch (IOException e) {
            throw new JRemotingException("Really out of the ordinary IOException", e);
        }

        return aBytes;
    }
    
    /**
     * Get instance from bytes.
     *
     * @param byteArray   to turn into an instance
     * @param facadesClassLoader the classloader that can resolve the class-def
     * @return the instance
     * @throws ClassNotFoundException if the class-def can't be resolved.
     */
    public static Object getInstanceFromBytes(byte[] byteArray, ClassLoader facadesClassLoader) throws ClassNotFoundException {

        try {
            ObjectInputStream oIs = new ClassLoaderObjectInputStream(facadesClassLoader, new ByteArrayInputStream(byteArray));
            Object obj = oIs.readObject();
            oIs.close();
            return obj;

        } catch (InvalidClassException ice) {
            throw new RuntimeException("java.io.InvalidClassException", ice);
        } catch (IOException ioe) {
            if (ioe.getCause() instanceof RuntimeException) {
                throw (RuntimeException) ioe.getCause();
            }
            throw new RuntimeException("unexpected deserialization", ioe);
        }
    }

    public static String getXml(LineNumberReader lineNumberReader) throws IOException {
        StringBuffer doc = new StringBuffer();
        try {
            String line = lineNumberReader.readLine();
            doc.append(line).append("\n");
            if (!(line.endsWith("/>"))) {
                line = lineNumberReader.readLine();
                while (line != null) {
                    doc.append(line).append("\n");
                    if (!Character.isWhitespace(line.charAt(0))) {
                        line = null;
                    } else {
                        line = lineNumberReader.readLine();
                    }
                }
            }
            return doc.toString();
        } catch (SocketTimeoutException e) {
            //System.out.println(">>>> SocketTimeOut");
            //e.printStackTrace();
            //System.out.println("<<<< SocketTimeOut");
            return doc.toString();
        }
    }



}
