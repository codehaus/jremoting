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
import java.util.*;

import org.codehaus.jremoting.server.*;
import org.codehaus.jremoting.util.StubHelper;
import org.codehaus.jremoting.requests.InvokeMethod;


/**
 * Class DynamicStubRetriever
 *
 * @author Paul Hammant
 */
public class DynamicStubRetriever implements DynamicStubGenerator, StubRetriever, Publisher {

    private String classpath;
    private String classGenDir = ".";
    private Class<?> generatorClass;
    private final ClassLoader classLoader;
    private Map facadeClasses = new HashMap();

    /**
     * @param classLoader        the classloader in which the proxy generater will be found.
     * @param generatorClassName the name of the proxy gen class
     */
    public  DynamicStubRetriever(ClassLoader classLoader, String generatorClassName) {
        this.classLoader = classLoader;
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
     * @param publishedName the name to publish as
     * @return the byte array for the proxy class
     * @throws StubRetrievalException if the class cannot be retrieved.
     */
    public final byte[] getStubClassBytes(String publishedName) throws StubRetrievalException {
        String name = StubHelper.formatStubClassName(publishedName);
        try {
            return getThingBytes(name);
        } catch (StubRetrievalException e) {
            String serviceName = StubHelper.getServiceName(publishedName);
            Class facadeClass = (Class) facadeClasses.get(serviceName);
            if (facadeClass == null) {
                throw new StubRetrievalException("unable to find facade class for service: "+ facadeClass);
            }
            try {
                generate(serviceName, facadeClass, classLoader);
                return getThingBytes(name);
            } catch (PublicationException e1) {
                throw new StubRetrievalException("unable to dynamically create stub: "+ e.getMessage());
            }
        }
    }

    /**
     * @param thingName the thing name
     * @return the byte array of the thing.
     * @throws org.codehaus.jremoting.server.StubRetrievalException
     *          if getting the bytes was a problem.
     */
    protected byte[] getThingBytes(String thingName) throws StubRetrievalException {

        thingName = thingName.replace('.', File.separatorChar) + ".class";

        FileInputStream fis;

        try {
            String cd = new File(classGenDir).getCanonicalPath();
            String file = new File(cd, thingName).getAbsolutePath();
            fis = new FileInputStream(file);
        } catch (Exception e) {
            String canonicalPath = null;
            try {
                canonicalPath = new File(".").getCanonicalPath();
            } catch (IOException e1) {
            }
            throw new StubRetrievalException("Generated class not found in classloader specified : '" + e.getMessage() + "', current directory is '" + canonicalPath + "'");
        }

        return getBytes(fis);
    }

    private byte[] getBytes(FileInputStream fis) throws StubRetrievalException {
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

    public void publish(Object impl, String service, Class primaryFacade) throws PublicationException {
        facadeClasses.put(service, primaryFacade);
    }

    public void publish(Object impl, String service, PublicationDescription publicationDescription) throws PublicationException {
        facadeClasses.put(service, publicationDescription.getPrimaryFacades()[0]);
        PublicationDescriptionItem[] additionalFacades = publicationDescription.getAdditionalFacades();
        for (int i = 0; i < additionalFacades.length; i++) {
            PublicationDescriptionItem additionalFacade = additionalFacades[i];
            facadeClasses.put(service + "_" + additionalFacade.getFacadeClass().getName(), additionalFacade.getFacadeClass());
        }
    }

    public void unPublish(Object impl, String service) throws PublicationException {
        facadeClasses.remove(service);
        Set facades = facadeClasses.keySet();
        for (Iterator iterator = facades.iterator(); iterator.hasNext();) {
            String aService = (String) iterator.next();
            if (aService.startsWith(service)) {
                facadeClasses.remove(aService);
            }

        }
    }

    public void replacePublished(Object oldImpl, String service, Object withImpl) throws PublicationException {
    }

    public boolean isPublished(String service) {
        return facadeClasses.containsKey(service);
    }
}
