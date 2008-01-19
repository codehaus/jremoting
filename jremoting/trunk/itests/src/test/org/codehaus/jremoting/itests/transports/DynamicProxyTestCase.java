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
package org.codehaus.jremoting.itests.transports;

import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacadeImpl;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Test Dynamic Stub (reflection) for comparison sake
 *
 * @author Paul Hammant
 */
public class DynamicProxyTestCase extends AbstractHelloTestCase {

    protected void setUp() throws Exception {

        testServer = new TestFacadeImpl();
        final Class[] interfaces = new Class[]{TestFacade.class};

        final ClassLoader classLoader = testServer.getClass().getClassLoader();

        // Standard dynamic proxy code.
        final InvocationHandler proxy = new InvocationHandler() {
            public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
                try {
                    return method.invoke(testServer, objects);
                } catch (final InvocationTargetException ite) {
                    throw ite.getTargetException();
                }
            }
        };

        testClient = (TestFacade) Proxy.newProxyInstance(classLoader, interfaces, proxy);

    }

    protected void tearDown() throws Exception {
    }
}
