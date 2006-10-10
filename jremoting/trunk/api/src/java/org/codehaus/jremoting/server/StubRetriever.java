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
 * A StubRetriever is a thing that allows the serverside JRemoting deployer to choose
 * how class defs for proxies are retrieved. They may not want them in the normal
 * classpath.
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public interface StubRetriever {

    /**
     * Get a Stub's bytes (from its class definition)
     *
     * @param publishedName the name the class is published as.
     * @return a byte array for the stub's class representation.
     * @throws org.codehaus.jremoting.server.StubRetrievalException
     *          if the classdef cannot be found.
     */
    byte[] getStubClassBytes(String publishedName) throws StubRetrievalException;

    void setPublisher(Publisher publisher);
}
