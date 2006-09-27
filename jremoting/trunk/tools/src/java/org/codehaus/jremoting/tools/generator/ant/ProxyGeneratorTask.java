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
package org.codehaus.jremoting.tools.generator.ant;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.codehaus.jremoting.server.ProxyGenerator;
import org.codehaus.jremoting.server.PublicationDescriptionItem;

import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Ant task to generate proxies
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class ProxyGeneratorTask extends Task {

    protected String[] primaryFacades;
    protected String[] additionalFacades;
    protected File classGenDir;
    protected String genName;
    protected Path classpath;
    private String generatorClass = "org.codehaus.jremoting.tools.generator.BcelProxyGenerator";

    /**
     * Constructor ProxyGeneratorTask
     */
    public ProxyGeneratorTask() {
    }

    /**
     * Method setInterfaces
     *
     * @param primaryFacades
     */
    public void setInterfaces(String primaryFacades) {

        StringTokenizer st = new StringTokenizer(primaryFacades, ",");
        Vector strings = new Vector();

        while (st.hasMoreTokens()) {
            strings.add(st.nextToken().trim());
        }

        this.primaryFacades = new String[strings.size()];

        strings.copyInto(this.primaryFacades);
    }

    /**
     * Method setAdditionalfacades
     *
     * @param additionalfacades
     */
    public void setAdditionalfacades(String additionalfacades) {

        StringTokenizer st = new StringTokenizer(additionalfacades, ",");
        Vector strings = new Vector();

        while (st.hasMoreTokens()) {
            strings.add(st.nextToken().trim());
        }

        additionalFacades = new String[strings.size()];

        strings.copyInto(additionalFacades);
    }


    /**
     * Method setClassgendir
     *
     * @param classGenDir
     */
    public void setClassgendir(File classGenDir) {
        this.classGenDir = classGenDir;
    }

    /**
     * Method setGenname
     *
     * @param genName
     */
    public void setGenname(String genName) {
        this.genName = genName;
    }

    /**
     * Method setClasspath
     *
     * @param classpath
     */
    public void setClasspath(Path classpath) {

        if (this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
    }

    /**
     * Method createClasspath
     *
     * @return path
     */
    public Path createClasspath() {

        if (classpath == null) {
            classpath = new Path(project);
        }

        return classpath.createPath();
    }

    /**
     * Method setClasspathRef
     *
     * @param r
     */
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }


    /**
     * Sets the GeneratorClass
     *
     * @param generatorClass The Generator Class to set.
     */
    public void setGeneratorClass(String generatorClass) {
        this.generatorClass = generatorClass;
    }

    /**
     * Method execute
     *
     * @throws BuildException
     */
    public void execute() throws BuildException {

        if (primaryFacades == null) {
            throw new BuildException("Specify at least one interface to expose");
        }

        if (classGenDir == null) {
            throw new BuildException("Specify the directory to generate Java classes in");
        }

        if (genName == null) {
            throw new BuildException("Specify the name to use for lookup");
        }

        ProxyGenerator proxyGenerator;

        try {
            Class proxyGenClass = Class.forName(generatorClass);
            proxyGenerator = (ProxyGenerator) proxyGenClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException("PrimaryGenerator Impl jar not in classpath");
        }

        try {
            proxyGenerator.setClassGenDir(classGenDir.getAbsolutePath());
            proxyGenerator.setGenName(genName);
            proxyGenerator.setClasspath(classpath.concatSystemClasspath("ignore").toString());

            PublicationDescriptionItem[] primaryFacades = new PublicationDescriptionItem[this.primaryFacades.length];
            ClassLoader classLoader = new AntClassLoader(getProject(), classpath);

            for (int i = 0; i < this.primaryFacades.length; i++) {
                String cn = this.primaryFacades[i];
                primaryFacades[i] = new PublicationDescriptionItem(classLoader.loadClass(cn));
            }

            proxyGenerator.setPrimaryFacades(primaryFacades);

            if (additionalFacades != null) {
                PublicationDescriptionItem[] additionalFacades = new PublicationDescriptionItem[this.additionalFacades.length];

                for (int i = 0; i < this.additionalFacades.length; i++) {
                    String cn = this.additionalFacades[i];

                    additionalFacades[i] = new PublicationDescriptionItem(classLoader.loadClass(cn));
                }

                proxyGenerator.setAdditionalFacades(additionalFacades);
            }

            ClassLoader classLoader2 = null;

            if (classpath != null) {
                classLoader2 = new AntClassLoader(project, classpath);
            } else {
                classLoader2 = this.getClass().getClassLoader();
            }

            proxyGenerator.generateSrc(classLoader2);
            proxyGenerator.generateClass(classLoader2);
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();

            throw new BuildException("Class not found : " + cnfe.getMessage());
        }
    }


}
