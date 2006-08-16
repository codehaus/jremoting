package org.codehaus.jremoting.tools.generator.mojo;

import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.jremoting.server.ProxyGenerator;
import org.codehaus.jremoting.server.PublicationDescriptionItem;
import org.codehaus.jremoting.server.ProxyGenerationException;


/**
 * Mojo to generate parameter names via ParanamerGenerator
 *
 * @author Paul Hammant / Mauro Talevi
 * @goal generate
 * @phase compile
 * @requiresDependencyResolution compile
 */
public class ProxyGenerationMojo
    extends AbstractMojo
{

    private String generatorClass = "org.codehaus.jremoting.tools.generator.JavacProxyGenerator";

    /**
     * Whether to give verbose output
     * @parameter
     * @required
     */
    protected boolean verbose;

    /**
     * The comma separated classpath to use while making stubs
     * @parameter
     * @required
     */
    protected String classpath;

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
    protected String interfacesToExpose;

    /**
     * Additional Facades. When encounted in an object tree, they are passed by ref not value to the client
     * @parameter
     * @required
     */
    protected String additionalFacades;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().debug( "Generating JRemoting Stubs.");

        String[] interfacesToExposeArray;
        String[] additionalFacadesArray;
        String[] callbackFacades;

        StringTokenizer st = new StringTokenizer(interfacesToExpose, ",");
        Vector strings = new Vector();
        while (st.hasMoreTokens()) {
            strings.add(st.nextToken().trim());
        }
        interfacesToExposeArray = new String[strings.size()];
        strings.copyInto(interfacesToExposeArray);

        st = new StringTokenizer(additionalFacades, ",");
        strings = new Vector();
        while (st.hasMoreTokens()) {
            strings.add(st.nextToken().trim());
        }
        additionalFacadesArray = new String[strings.size()];
        strings.copyInto(additionalFacadesArray);


            if (interfacesToExpose == null) {
                throw new MojoExecutionException("Specify at least one interface to expose");
            }

            if (srcGenDir == null) {
                throw new MojoExecutionException("Specify the directory to generate Java source in");
            }

            if (classGenDir == null) {
                throw new MojoExecutionException("Specify the directory to generate Java classes in");
            }

            if (genName == null) {
                throw new MojoExecutionException("Specify the name to use for lookup");
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
                proxyGenerator.setSrcGenDir(srcGenDir.getAbsolutePath());
                proxyGenerator.setClassGenDir(classGenDir.getAbsolutePath());
                proxyGenerator.setGenName(genName);
                proxyGenerator.verbose(Boolean.valueOf(verbose).booleanValue());
                proxyGenerator.setClasspath(classpath);

                PublicationDescriptionItem[] interfacesToExpose = new PublicationDescriptionItem[interfacesToExposeArray.length];
                ClassLoader classLoader = null; // TODO new AntClassLoader(getProject(), classpath);

                for (int i = 0; i < interfacesToExposeArray.length; i++) {
                    String cn = interfacesToExposeArray[i];
                    interfacesToExpose[i] = new PublicationDescriptionItem(classLoader.loadClass(cn));
                }

                proxyGenerator.setInterfacesToExpose(interfacesToExpose);

                if (additionalFacades != null) {
                    PublicationDescriptionItem[] additionalFacades = new PublicationDescriptionItem[additionalFacadesArray.length];

                    for (int i = 0; i < additionalFacadesArray.length; i++) {
                        String cn = additionalFacadesArray[i];

                        additionalFacades[i] = new PublicationDescriptionItem(classLoader.loadClass(cn));
                    }

                    proxyGenerator.setAdditionalFacades(additionalFacades);
                }

                ClassLoader classLoader2 = this.getClass().getClassLoader();

                proxyGenerator.generateSrc(classLoader2);
                proxyGenerator.generateClass(classLoader2);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();

                throw new MojoExecutionException("Class not found : " + cnfe.getMessage());
            } catch (ProxyGenerationException sge) {
                throw new MojoExecutionException("Proxy Gerneation error : " + sge.getMessage());
            }
    }

}
