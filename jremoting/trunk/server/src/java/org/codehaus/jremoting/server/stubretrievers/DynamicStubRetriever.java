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
package org.codehaus.jremoting.server.stubretrievers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.codehaus.jremoting.server.DynamicStubGenerator;
import org.codehaus.jremoting.server.StubGenerator;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.PublicationDescriptionItem;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.StubRetrievalException;
import org.codehaus.jremoting.server.StubRetriever;
import org.codehaus.jremoting.util.StubHelper;


/**
 * Class DynamicStubRetriever
 *
 * @author Paul Hammant
 */
public class DynamicStubRetriever implements DynamicStubGenerator, StubRetriever {

    private String classpath;
    private String classGenDir = ".";
    private Class generatorClass;

    /**
     * @param classLoader        the classloader in which the proxy generater will be found.
     * @param generatorClassName the name of the proxy gen class
     */
    public DynamicStubRetriever(ClassLoader classLoader, String generatorClassName) {
        try {
            generatorClass = classLoader.loadClass(generatorClassName);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(generatorClassName);
        }
    }

    public void generate(String service, Class primaryFacade, ClassLoader classLoader) throws PublicationException {
        generate(service, new PublicationDescription(primaryFacade), classLoader);
    }


    /**
     * Use this classpath during retrieval.
     *
     * @param classpath the classpath
     */
    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    /**
     * Method addToClasspath
     *
     * @param classpathElement an element for the classpath
     */
    public void addToClasspath(String classpathElement) {
        classpath = classpath + File.pathSeparator + classpathElement;
    }

    /**
     * Method setClassGenDir
     *
     * @param classGenDir the class generation directory
     */
    public void setClassGenDir(String classGenDir) {
        this.classGenDir = classGenDir;
    }

    /**
     * Method getProxyClassBytes
     *
     * @param service the name to publish as
     * @return the byte array for the proxy class
     * @throws StubRetrievalException if the class cannot be retrieved.
     */
    public final byte[] getStubClassBytes(String service) throws StubRetrievalException {
        return getThingBytes(StubHelper.formatStubClassName(service));
    }

    /**
     * @param thingName the thing name
     * @return the byte array of the thing.
     * @throws org.codehaus.jremoting.server.StubRetrievalException
     *          if getting the bytes was a problem.
     */
    protected byte[] getThingBytes(String thingName) throws StubRetrievalException {

        thingName = thingName.replace('.', '\\') + ".class";

        FileInputStream fis;

        try {
            fis = new FileInputStream(new File(classGenDir, thingName));
        } catch (Exception e) {
            e.printStackTrace();

            throw new StubRetrievalException("Generated class not found in classloader specified : '" + e.getMessage() + "', current directory is '" + new File(".").getAbsolutePath() + "'");
        }

        if (fis == null) {
            throw new StubRetrievalException("Generated class not found in classloader specified.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = 0;

        try {
            while (-1 != (i = fis.read())) {
                baos.write(i);
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();

            throw new StubRetrievalException("Error retrieving generated class bytes : " + e.getMessage());
        }

        return baos.toByteArray();
    }

    public void generate(String service, PublicationDescription publicationDescription, ClassLoader classLoader) throws PublicationException {

        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }

        PublicationDescriptionItem[] primaryFacades = new PublicationDescriptionItem[0];
        PublicationDescriptionItem[] additionalFacades = new PublicationDescriptionItem[0];

        primaryFacades = publicationDescription.getPrimaryFacades();
        additionalFacades = publicationDescription.getAdditionalFacades();

        org.codehaus.jremoting.server.StubGenerator proxyGenerator;

        try {
            proxyGenerator = (StubGenerator) generatorClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("StubGenerator cannot be instantiated.");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("StubGenerator was illegally accessed");
        }

        proxyGenerator.setClassGenDir(classGenDir);
        proxyGenerator.setGenName(service);
        proxyGenerator.setClasspath(classpath);
        proxyGenerator.setPrimaryFacades(primaryFacades);
        proxyGenerator.setAdditionalFacades(additionalFacades);

        try {
            //proxyGenerator.setClasspath(Request.class.getProtectionDomain().getCodeSource().getLocation().getFile());
            proxyGenerator.generateClass(classLoader);
        } catch (Throwable t) {

            System.err.println("******");
            System.err.println("** Exception while making String : ");
            System.err.flush();
            t.printStackTrace();
            System.err.println("** ClassDir=" + classGenDir);
            System.err.println("** Name=" + service);
            System.err.println("** CLasspath=" + classpath);
            System.err.println("** Classes/Facades to Expose..");

            for (PublicationDescriptionItem primaryFacade : primaryFacades) {
                String aString = primaryFacade.getFacadeClass().getName();

                System.err.println("** .." + aString);
            }

            System.err.println("******");
            System.err.flush();
        }
    }
}
