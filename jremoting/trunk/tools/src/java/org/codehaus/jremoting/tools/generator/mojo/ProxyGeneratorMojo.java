/* ====================================================================
 * Copyright 2005 JRemoting Committers
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
package org.codehaus.jremoting.tools.generator.mojo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.jremoting.server.ProxyGenerationException;
import org.codehaus.jremoting.server.ProxyGenerator;
import org.codehaus.jremoting.server.PublicationDescriptionItem;


/**
 * Mojo to generate proxies
 *
 * @author Paul Hammant
 * @author Mauro Talevi
 * @goal generate
 * @phase compile
 * @requiresDependencyResolution test-compile
 */
public class ProxyGeneratorMojo
    extends AbstractMojo
{

    private static final String COMMA = ",";

    private String generatorClass = "org.codehaus.jremoting.tools.generator.JavacProxyGenerator";

    /**
     * Compile classpath.
     *
     * @parameter expression="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    protected List classpathElements;

    /**
     * Whether to give verbose output
     * @parameter
     */
    protected boolean verbose;

    /**
     * The name of the service used to generate stub class names
     * @parameter
     * @required
     */
    protected String genName;

    /**
     * The principal facade that is being published
     * @parameter
     * @required
     */
    protected String interfaces;

    /**
     * The directory to use for temporary source
     * @parameter
     * @required
     */
    protected File srcGenDir;

    /**
     * The directory to put generated classes into
     * @parameter
     * @required
     */
    protected File classGenDir;

    /**
     * Additional Facades. When encounted in an object tree, they are passed by ref not value to the client
     * @parameter
     */
    protected String additionalFacades;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().debug( "Generating JRemoting Stubs.");


        if (genName == null) {
            throw new MojoExecutionException(
                    "Specify the name to use for lookup");
        }

        if (interfaces == null) {
            throw new MojoExecutionException(
                    "Specify at least one interface to expose");
        }

        if (srcGenDir == null) {
            throw new MojoExecutionException(
                    "Specify the directory to generate Java source in");
        }

        if (classGenDir == null) {
            throw new MojoExecutionException(
                    "Specify the directory to generate Java classes in");
        }

        ProxyGenerator proxyGenerator;

        try {
            proxyGenerator = (ProxyGenerator)Class.forName(generatorClass).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Failed to create ProxyGenerator "+generatorClass, e);
        }

        try {
            proxyGenerator.setGenName(genName);
            proxyGenerator.setSrcGenDir(srcGenDir.getAbsolutePath());
            proxyGenerator.setClassGenDir(classGenDir.getAbsolutePath());
            proxyGenerator.verbose(Boolean.valueOf(verbose).booleanValue());
            String classpath = toCSV(classpathElements);
            proxyGenerator.setClasspath(classpath);
            getLog().debug("ProxyGenerator classpath: " + classpath);

            String[] interfacesToExposeArray = fromCSV(interfaces);
            String[] additionalFacadesArray = fromCSV(additionalFacades);

            PublicationDescriptionItem[] interfacesToExpose = new PublicationDescriptionItem[interfacesToExposeArray.length];
            ClassLoader classLoader = createClassLoader(classpathElements);
            
            for (int i = 0; i < interfacesToExposeArray.length; i++) {
                String cn = interfacesToExposeArray[i];
                interfacesToExpose[i] = new PublicationDescriptionItem(
                        classLoader.loadClass(cn));
            }

            proxyGenerator.setInterfacesToExpose(interfacesToExpose);

            if (additionalFacades != null) {
                PublicationDescriptionItem[] additionalFacades = new PublicationDescriptionItem[additionalFacadesArray.length];

                for (int i = 0; i < additionalFacadesArray.length; i++) {
                    String cn = additionalFacadesArray[i];

                    additionalFacades[i] = new PublicationDescriptionItem(
                            classLoader.loadClass(cn));
                }

                proxyGenerator.setAdditionalFacades(additionalFacades);
            }

            ClassLoader classLoader2 = this.getClass().getClassLoader();

            proxyGenerator.generateSrc(classLoader2);
            proxyGenerator.generateClass(classLoader2);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Class not found: "
                    + e.getMessage(), e);
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Malformed classpath URLs: "
                    + e.getMessage(), e);
        } catch (ProxyGenerationException e) {
            throw new MojoExecutionException("Proxy generation error: "
                    + e.getMessage(), e);
        }
    }    
    
    private ClassLoader createClassLoader(List classpathElements) throws MalformedURLException {
        return new URLClassLoader(toClasspathURLs(classpathElements));
    }

    protected static URL[] toClasspathURLs(List classpathElements)
            throws MalformedURLException {
        List urls = new ArrayList();
        if (classpathElements != null) {
            for (Iterator i = classpathElements.iterator(); i.hasNext();) {
                String classpathElement = (String) i.next();
                urls.add(new File(classpathElement).toURL());
            }
        }
        return (URL[]) urls.toArray(new URL[urls.size()]);
    }

    private String[] fromCSV(String csv) {
        if ( csv == null ) {
            return new String[0];
        }
        return csv.split(COMMA);
    }

    private String toCSV(List classpathElements) {
        StringBuffer sb = new StringBuffer();
        for (Iterator i = classpathElements.iterator(); i.hasNext(); ){
            String path = (String)i.next();
            sb.append(path);
            if ( i.hasNext() ){
                sb.append(COMMA);
            }
        }        
        return sb.toString();
    }

}
