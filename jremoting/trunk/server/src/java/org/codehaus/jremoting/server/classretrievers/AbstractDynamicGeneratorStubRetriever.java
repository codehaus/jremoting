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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.codehaus.jremoting.server.DynamicProxyGenerator;
import org.codehaus.jremoting.server.ProxyGenerator;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.PublicationDescriptionItem;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.StubRetrievalException;
import org.codehaus.jremoting.server.StubRetriever;


/**
 * Class JarFileStubRetriever
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class AbstractDynamicGeneratorStubRetriever implements DynamicProxyGenerator, StubRetriever {

    private String classpath;
    private String classGenDir = ".";
    private Class generatorClass;

    /**
     * @param classLoader        the classloader in which the proxy generater will be found.
     * @param generatorClassName the name of the proxy gen class
     */
    public AbstractDynamicGeneratorStubRetriever(ClassLoader classLoader, String generatorClassName) {
        try {
            generatorClass = classLoader.loadClass(generatorClassName);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(generatorClassName);
        }
    }

    /**
     * Method generate
     *
     * @param service            the name to generate as
     * @param interfaceToExpose the interfaces to expose.
     * @param classLoader       the classloader to use during generation.
     * @throws PublicationException if the generation failed.
     */
    public void generate(String service, Class interfaceToExpose, ClassLoader classLoader) throws PublicationException {
        generateProxy(service, new PublicationDescription(interfaceToExpose), classLoader, false);
    }

    /**
     * Method generate
     *
     * @param service                 the name to generate as
     * @param publicationDescription the description of the publication
     * @param classLoader            the class loader to use.
     * @throws PublicationException if the generation failed.
     */
    public void generate(String service, PublicationDescription publicationDescription, ClassLoader classLoader) throws PublicationException {
        generateProxy(service, publicationDescription, classLoader, false);
    }

    /**
     * Method deferredGenerate
     *
     * @param service                 the name of the clas to generate
     * @param publicationDescription the description of the publication
     * @param classLoader            the class loader to use.
     * @throws PublicationException if the generation failed.
     */
    public void deferredGenerate(String service, PublicationDescription publicationDescription, ClassLoader classLoader) throws PublicationException {
        generateProxy(service, publicationDescription, classLoader, true);
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
        return getThingBytes("JRemotingGenerated" + service);
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

    private void generateProxy(String service, PublicationDescription publicationDescription, ClassLoader classLoader, boolean deferred) throws PublicationException {

        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }

        PublicationDescriptionItem[] interfacesToExpose = new PublicationDescriptionItem[0];
        PublicationDescriptionItem[] addInfs = new PublicationDescriptionItem[0];

        interfacesToExpose = publicationDescription.getInterfacesToExpose();
        addInfs = publicationDescription.getAdditionalFacades();

        org.codehaus.jremoting.server.ProxyGenerator proxyGenerator;

        try {
            proxyGenerator = (org.codehaus.jremoting.server.ProxyGenerator) generatorClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("ProxyGenerator cannot be instantiated.");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("ProxyGenerator was illegally accessed");
        }

        proxyGenerator.setClassGenDir(classGenDir);
        proxyGenerator.setGenName(service);
        proxyGenerator.setClasspath(classpath);
        proxyGenerator.setInterfacesToExpose(interfacesToExpose);
        proxyGenerator.setAdditionalFacades(addInfs);

        try {
            proxyGenerator.generateSrc(classLoader);
        } catch (Throwable t) {
            System.err.println("******");
            System.err.println("** Exception while making source : ");
            System.err.flush();
            t.printStackTrace();
            System.err.println("** Name=" + service);
            System.err.println("** Classes/Interfaces to Expose..");

            for (int i = 0; i < interfacesToExpose.length; i++) {
                String aString = interfacesToExpose[i].getFacadeClass().getName();

                System.err.println("** .." + aString);
            }

            System.err.println("******");
            System.err.flush();
        }

        if (!deferred) {
            try {
                //proxyGenerator.setClasspath(AbstractRequest.class.getProtectionDomain().getCodeSource().getLocation().getFile());
                proxyGenerator.generateClass(classLoader);
            } catch (Throwable t) {

                System.err.println("******");
                System.err.println("** Exception while making String : ");
                System.err.flush();
                t.printStackTrace();
                System.err.println("** ClassDir=" + classGenDir);
                System.err.println("** Name=" + service);
                System.err.println("** CLasspath=" + classpath);
                System.err.println("** Classes/Interfaces to Expose..");

                for (int i = 0; i < interfacesToExpose.length; i++) {
                    String aString = interfacesToExpose[i].getFacadeClass().getName();

                    System.err.println("** .." + aString);
                }

                System.err.println("******");
                System.err.flush();
            }
        }
    }

    /**
     * Method generateDeferred
     *
     * @param classLoader the classloader to use.
     */
    public void generateDeferred(ClassLoader classLoader) {

        org.codehaus.jremoting.server.ProxyGenerator proxyGenerator;

        try {
            proxyGenerator = (ProxyGenerator) generatorClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("ProxyGenerator cannot be instantiated.");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("ProxyGenerator was illegally accessed");
        }
        proxyGenerator.generateDeferredClasses();
    }
}
