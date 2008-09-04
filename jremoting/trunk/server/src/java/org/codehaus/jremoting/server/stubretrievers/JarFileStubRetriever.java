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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Class JarFileStubRetriever
 *
 * @author Paul Hammant
 *
 */
public class JarFileStubRetriever extends FromClassLoaderStubRetriever {

    /**
     * Contruct a StubRetriever from url of a JAR file.
     *
     * @param urlOfJarFile the jar file URL.
     */
    public JarFileStubRetriever(URL urlOfJarFile) {
        super(new URLClassLoader(new URL[]{urlOfJarFile}));
    }

    /**
     * Contruct a StubRetriever from urls of JAR files.
     *
     * @param urlsOfJarFiles the jar file URLs.
     */
    public JarFileStubRetriever(URL[] urlsOfJarFiles) {
        super(new URLClassLoader(urlsOfJarFiles));
    }

    /**
     * Contruct a StubRetriever from file paths.
     *
     * @param pathsOfJarFiles the paths that map to URLs
     * @throws MalformedURLException if the paths are not mappable to URLS.
     */
    public JarFileStubRetriever(String[] pathsOfJarFiles) throws MalformedURLException {
        super(new URLClassLoader(makeUrls(pathsOfJarFiles)));
    }

    private static URL[] makeUrls(String[] pathsOfJarFiles) throws MalformedURLException {
        URL[] urls = new URL[pathsOfJarFiles.length];

        for (int i = 0; i < pathsOfJarFiles.length; i++) {
            urls[i] = new File(pathsOfJarFiles[i]).toURL();
        }
        return urls;
    }

    /**
     * Construct from a path to a jar file
     *
     * @param pathOfJarFile the path
     * @throws MalformedURLException if the path is not mappable to a URL.
     */
    public JarFileStubRetriever(String pathOfJarFile) throws MalformedURLException {
        super(new URLClassLoader(new URL[]{new File(pathOfJarFile).toURL()}));
    }
}
