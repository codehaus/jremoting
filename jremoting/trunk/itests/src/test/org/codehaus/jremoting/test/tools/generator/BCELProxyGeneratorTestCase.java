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
package org.codehaus.jremoting.test.tools.generator;

import junit.framework.TestCase;
import org.codehaus.jremoting.api.DefaultThreadPool;
import org.codehaus.jremoting.client.factories.ClientSideClassFactory;
import org.codehaus.jremoting.client.factories.DefaultProxyHelper;
import org.codehaus.jremoting.client.monitors.DumbClientMonitor;
import org.codehaus.jremoting.client.pingers.DefaultConnectionPinger;
import org.codehaus.jremoting.client.transports.direct.DirectHostContext;
import org.codehaus.jremoting.server.ProxyGenerator;
import org.codehaus.jremoting.server.PublicationDescriptionItem;
import org.codehaus.jremoting.tools.generator.BCELProxyGeneratorImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Class BCELProxyGeneratorTest
 * Unit testing of BCELProxyGeneratorImpl
 *
 * @author <a href="mailto:vinayc@apache.org">Vinay Chandrasekharan</a>
 * @version 1.0
 */
public class BCELProxyGeneratorTestCase extends TestCase {
    private ProxyGenerator proxyGenerator;
    private Class generatedProxyClass;
    private Object generatedProxyObject;
    private ClientSideClassFactory factory;
    /**
     * ********************* TestInterface ******************
     */
    public static final Class testInterfaceClass = TstRemoteInterface.class;

    public BCELProxyGeneratorTestCase(String testName) {
        super(testName);
    }

    private Class createNewClass() {
        if (generatedProxyClass != null) {
            return generatedProxyClass;
        }
        proxyGenerator.setGenName("Something");
        proxyGenerator.setInterfacesToExpose(new PublicationDescriptionItem[]{new PublicationDescriptionItem(testInterfaceClass)});
        proxyGenerator.setClassGenDir(".");
        proxyGenerator.verbose(true);
        proxyGenerator.generateClass(null);


        generatedProxyClass = ((BCELProxyGeneratorImpl) proxyGenerator).getGeneratedClass("JRemotingGeneratedSomething_Main");
        return generatedProxyClass;
    }


    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        Class proxyGenClass = Class.forName("org.codehaus.jremoting.tools.generator.BCELProxyGeneratorImpl");
        proxyGenerator = (ProxyGenerator) proxyGenClass.newInstance();
        //create the Proxy Class using the BCEL Generator
        createNewClass();
        proxyGenerator.verbose(true);
    }


    /**
     * Method testGeneratedClassName.
     * Checks whether 'Class' is created properly
     */
    public void testGeneratedClassNameOfProxy() {
        assertNotNull(proxyGenerator);
        assertNotNull(generatedProxyClass);
        assertEquals(generatedProxyClass.getName().equals("JRemotingGeneratedSomething_Main"), true);
    }

    /**
     * Method testConstructorOfProxy.
     * Test if the instance is created properly using the lone
     * Constructor embedded within the Proxy implementation
     *
     * @throws Exception
     */
    public void testConstructorOfProxy() throws Exception {
        if (generatedProxyClass == null) {
            testGeneratedClassNameOfProxy();
        }
        TstInvocationHandler invocationHandler = new TstInvocationHandler(new DefaultThreadPool(), new DumbClientMonitor(), new DefaultConnectionPinger());
        //create the factory;
        factory = new ClientSideClassFactory(new DirectHostContext(invocationHandler), false);
        DefaultProxyHelper defaultProxyHelper = new DefaultProxyHelper(factory, invocationHandler, "PublishedName", "ObjectName", new Long(1010), new Long(3030));

        Constructor[] _constructors = generatedProxyClass.getConstructors();
        //there shld be only 1 constructor for the generated proxy
        // one that takes BaseServedObject as the argument
        assertEquals(_constructors.length, 1);

        generatedProxyObject = _constructors[0].newInstance(new Object[]{defaultProxyHelper});
        assertNotNull(generatedProxyObject);

    }


    /**
     * Method testGetReferenceIDMethodOfProxy.
     * Testing
     * =================================
     * public Long codehausRemotingGetReferenceID(Object factoryThatIsAsking) {
     * return mBaseServedObject.getReferenceID(factoryThatIsAsking);
     * }
     * =================================
     *
     * @throws Exception
     */
    public void testGetReferenceIDMethodOfProxy() throws Exception {
        if (generatedProxyObject == null) {
            testConstructorOfProxy();
        }

        Method _getReferenceIDMethod = generatedProxyClass.getMethod("codehausRemotingGetReferenceID", new Class[]{Object.class});
        assertNotNull(_getReferenceIDMethod);
        Object _ret = _getReferenceIDMethod.invoke(generatedProxyObject, new Object[]{factory});
        assertEquals(new Long(1010), _ret);
    }

    /**
     * Method testGeneratedMethodsPassOne.
     * Testing
     * This test involves the crux of the stub-generation
     * routine.
     * 1. Pass an test interface for stub-generation
     * 2. Test the created stub
     *
     * @throws Exception
     */
    public void testGeneratedMethodsPassOne() throws Exception {
        if (generatedProxyObject == null) {
            testConstructorOfProxy();
        }


        Method[] methods = generatedProxyClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().indexOf("test") == -1) {
                continue;
            }
            //System.out.println("Testing method["+methods[i].getName()+"]");
            Object[] _arguments = new Object[methods[i].getParameterTypes().length];
            for (int j = 0; j < _arguments.length; j++) {

                _arguments[j] = testInterfaceClass.getField(methods[i].getName() + "_arg" + j).get(null);
                //System.out.println("argType["+methods[i].getParameterTypes()[j]+"]arg["+j+"]"+_arguments[j]);
            }
            if (methods[i].getParameterTypes().length == 0) {
                _arguments = null;
            }
            Object _ret = methods[i].invoke(generatedProxyObject, _arguments);

            if (methods[i].getReturnType() != Void.TYPE) {
                assertEquals(testInterfaceClass.getField(methods[i].getName() + "_retValue").get(null), _ret);
            }
        }
    }

}
