/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
 * Portions copyright 2001 - 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.codehaus.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.jremoting.client.factories;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.HostContext;

/**
 * Class ClientSideStubFactory
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class ClientSideStubFactory extends AbstractStubFactory {
    private ClassLoader classLoader;

    public ClientSideStubFactory(HostContext hostContext) throws ConnectionException {
        super(hostContext);
    }

    public ClientSideStubFactory(HostContext hostContext, ClassLoader classLoader) throws ConnectionException {
        super(hostContext);
        this.classLoader = classLoader;
    }


    protected Class getStubClass(String publishedServiceName, String objectName) throws ConnectionException, ClassNotFoundException {

        String stubClassName = "JRemotingGenerated" + publishedServiceName + "_" + objectName;

        try {
            if (classLoader == null) {
                return Thread.currentThread().getContextClassLoader().loadClass(stubClassName);
            } else {
                return classLoader.loadClass(stubClassName);

            }
        } catch (ClassNotFoundException e) {
            return this.getClass().getClassLoader().loadClass(stubClassName);
        }
    }

}
