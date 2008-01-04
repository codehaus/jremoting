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
import org.codehaus.jremoting.client.Stub;
import org.codehaus.jremoting.client.StubFactory;
import org.codehaus.jremoting.util.MethodNameHelper;
import org.codehaus.jremoting.util.StaticStubHelper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Class StubsOnClient
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class StubsViaReflection implements StubFactory {

    public Object instantiateStub(String facadeClassName, String publishedServiceName, String objectName, Transport transport, final org.codehaus.jremoting.client.StubHelper stubHelper) throws ConnectionException {

        Class facadeClass = null;
        try {
            facadeClass = Class.forName(facadeClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {facadeClass, Stub2.class},
                new MyInvocationHandler(stubHelper, publishedServiceName, objectName));

    }

    public interface Stub2 extends Stub {
        
        public String jRemotingGetObjectName();

    }

    private class MyInvocationHandler implements InvocationHandler {
        private final org.codehaus.jremoting.client.StubHelper stubHelper;
        private String publishedServiceName;
        private final String objectName;

        public MyInvocationHandler(org.codehaus.jremoting.client.StubHelper stubHelper, String publishedServiceName, String objectName) {
            this.stubHelper = stubHelper;
            this.publishedServiceName = publishedServiceName;
            this.objectName = objectName;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            // methods on Stub and Stub2
            if (method.getName().equals("jRemotingGetReferenceID")) {
                return stubHelper.getReference(args[0]);
            } else if (method.getName().equals("jRemotingGetObjectName")) {
                return StaticStubHelper.formatServiceName(publishedServiceName, objectName);
            }

            String signature = MethodNameHelper.getMethodSignature(method);
            Class<?> rt = method.getReturnType();
            if (args == null) {
                args = new Object[0];
            }
            if (stubHelper.isFacadeInterface(rt)) {
                String name = MethodNameHelper.encodeClassName(method.getReturnType());
                return stubHelper.processObjectRequestGettingFacade(rt, signature, args, name);
            } else if (rt.getName().equals("void")) {
                Class<?>[] argTypes = method.getParameterTypes();
                stubHelper.processVoidRequest(signature, args, argTypes);
                return null;
            } else {
                Class<?>[] argTypes = method.getParameterTypes();
                return stubHelper.processObjectRequest(signature, args, argTypes);
            }
        }
    }
}