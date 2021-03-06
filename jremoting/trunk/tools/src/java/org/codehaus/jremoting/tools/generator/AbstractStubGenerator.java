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
package org.codehaus.jremoting.tools.generator;

import org.codehaus.jremoting.server.StubGenerator;
import org.codehaus.jremoting.server.PublicationItem;

import java.lang.reflect.Method;
import java.io.File;


/**
 * Abstract parent for Stub Generators
 *
 * @author Paul Hammant
 **
 */

public abstract class AbstractStubGenerator implements StubGenerator {

    private String classGenDir;
    private String genName;
    private String classpath;
    private PublicationItem[] secondaryFacades;
    private PublicationItem primaryFacades;

    /**
     * Get the directory name of the class generation directory.
     *
     * @return the dir name.
     */
    public String getClassGenDir() {
        return classGenDir;
    }

    /**
     * Get the generation name of the class
     *
     * @return the name.
     */

    public String getGenName() {
        return genName;
    }


    /**
     * Get the classpath used during creation
     *
     * @return classpath
     */

    public String getClasspath() {
        return classpath;
    }


    /**
     * Get the additional facades
     *
     * @return the additional facades
     */
    public PublicationItem[] getAdditionalFacades() {
        return secondaryFacades;
    }

    /**
     * Get the facades to expose.
     *
     * @return the facades
     */
    public PublicationItem getPrimaryFacade() {
        return primaryFacades;
    }


    /**
     * Set the facades to expose.
     *
     * @param primaryFacade
     */
    public void setPrimaryFacade(PublicationItem primaryFacade) {
        this.primaryFacades = primaryFacade;
    }


    /**
     * Set the additional facades
     *
     * @param secondaryFacades the facades.
     */
    public void setAdditionalFacades(PublicationItem[] secondaryFacades) {
        this.secondaryFacades = secondaryFacades;
    }

    /**
     * Set the clas generation dorectory
     *
     * @param classGenDir the dir.
     */
    public void setClassGenDir(String classGenDir) {
        this.classGenDir = classGenDir;
    }

    /**
     * Set the generation name
     *
     * @param genName the name
     */
    public void setGenName(String genName) {
        this.genName = genName;
    }

    /**
     * Set the classpath to generate with
     *
     * @param classpath the classpath.
     */
    public void setClasspath(String classpath) {
        if (classpath != null) {
            this.classpath = classpath.replace(',', File.pathSeparatorChar);
        } else {
            this.classpath = classpath;
        }
    }

    /**
     * Is the param one of the additional facades?
     *
     * @param clazz the class
     * @return true if the class is one of the designated facades
     */
    protected boolean isAdditionalFacade(Class clazz) {

        if (secondaryFacades == null) {
            return false;
        }

        for (PublicationItem secondaryFacade : secondaryFacades) {
            if (clazz.getName().equals(secondaryFacade.getFacadeClass().getName())) {
                return true;
            } else if (clazz.getName().equals("[L" + secondaryFacade.getFacadeClass().getName() + ";")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Creates the Java-Source type of the string.
     *
     * @param rClass the class to get a type for
     * @return the class type
     */
    protected String getClassType(Class rClass) {

        String cn = rClass.getName();

        if (rClass.getName().startsWith("[L")) {
            return cn.substring(2, cn.length() - 1) + "[]";
        } else {
            return cn;
        }
    }


    /**
     * @param publicationDescriptionItems
     * @return needs asyn behavior
     */
    protected boolean needsAsyncBehavior(PublicationItem[] publicationDescriptionItems) {
        for (PublicationItem publicationDescriptionItem : publicationDescriptionItems) {
            if (publicationDescriptionItem.hasAsyncBehavior()) {
                return true;
            }
        }
        return false;
    }

    protected Method[] getGeneratableMethods(Class clazz) {

        Method[] methods = null;
        try {
            Method ts = Object.class.getMethod("toString", new Class[0]);
            Method hc = Object.class.getMethod("hashCode", new Class[0]);
            Method[] interfaceMethods = clazz.getMethods();
            methods = new Method[interfaceMethods.length + 2];
            System.arraycopy(interfaceMethods, 0, methods, 0, interfaceMethods.length);
            methods[interfaceMethods.length] = ts;
            methods[interfaceMethods.length + 1] = hc;
        } catch (NoSuchMethodException e) {
            // never!
        }
        return methods;

    }


}
