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
import org.codehaus.jremoting.client.Transport;
import org.codehaus.jremoting.client.ContextFactory;
import org.codehaus.jremoting.util.StubHelper;

/**
 * Class StubsOnClient
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class StubsOnClient extends AbstractFactory {

    private ClassLoader stubsClasLoader;

    public StubsOnClient(Transport transport) throws ConnectionException {
        this(transport, Thread.currentThread().getContextClassLoader(), new NullContextFactory());
    }

    public StubsOnClient(Transport transport, ContextFactory contextFactory) throws ConnectionException {
        this(transport, Thread.currentThread().getContextClassLoader(), contextFactory);
    }

    public StubsOnClient(Transport transport, ClassLoader stubsClasLoader, ContextFactory contextFactory
    ) throws ConnectionException {
        super(transport, contextFactory);
        this.stubsClasLoader = stubsClasLoader;
    }


    protected Class getStubClass(String publishedServiceName, String objectName) throws ConnectionException, ClassNotFoundException {
        String stubClassName = StubHelper.formatStubClassName(publishedServiceName, objectName);
        return stubsClasLoader.loadClass(stubClassName);
    }

}
