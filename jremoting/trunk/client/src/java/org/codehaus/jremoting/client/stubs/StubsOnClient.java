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
package org.codehaus.jremoting.client.stubs;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.Transport;
import org.codehaus.jremoting.util.StubHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Class StubsOnClient
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class StubsOnClient implements StubClassLoader {

    private ClassLoader stubsClasLoader;

    public StubsOnClient(ClassLoader stubsClasLoader) throws ConnectionException {
        this.stubsClasLoader = stubsClasLoader;
    }

    public Object instantiateStub(String facadeClassName, String publishedServiceName, String objectName, Transport transport, org.codehaus.jremoting.client.StubHelper stubHelper) throws ConnectionException {
        
        try {
            String stubClassName = StubHelper.formatStubClassName(publishedServiceName, objectName);
            Class stubClass = stubsClasLoader.loadClass(stubClassName);
            Constructor[] constructors = stubClass.getConstructors();
            return constructors[0].newInstance(stubHelper);
        } catch (InvocationTargetException ite) {
            throw new ConnectionException("Generated class not instantiated : " + ite.getTargetException().getMessage());
        } catch (ClassNotFoundException cnfe) {
            throw new ConnectionException("Generated class not found during lookup : " + cnfe.getMessage());
        } catch (InstantiationException ie) {
            throw new ConnectionException("Generated class not instantiable during lookup : " + ie.getMessage());
        } catch (IllegalAccessException iae) {
            throw new ConnectionException("Illegal access to generated class during lookup : " + iae.getMessage());
        }


    }

}
