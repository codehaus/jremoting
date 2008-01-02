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
package org.codehaus.jremoting.server;


/**
 * Class StubGenerator
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public interface StubGenerator {

    /**
     * Set the primary facades.
     *
     * @param primaryFacades the facades to expose
     */
    void setPrimaryFacades(PublicationItem[] primaryFacades);

    /**
     * Set the additional facades
     *
     * @param additionalFacades additional facades to expose
     */
    void setAdditionalFacades(PublicationItem[] additionalFacades);

    /**
     * Set the classpath
     *
     * @param classpath the classpath for compilation (if appl).
     */
    void setClasspath(String classpath);

    /**
     * Set the class generation directory.
     *
     * @param classGenDir the class generation directory (if appl).
     */
    void setClassGenDir(String classGenDir);

    /**
     * Set the generation name.
     *
     * @param genName the generation name
     */
    void setGenName(String genName);

    /**
     * Generate class
     *
     * @param facadesClassLoader the classloader active during compilation/generation.
     */
    void generateClass(ClassLoader facadesClassLoader);

}
