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
package org.codehaus.jremoting.itests.stubs;


import org.codehaus.jremoting.client.context.ThreadLocalContextFactory;
import org.codehaus.jremoting.client.encoders.ByteStreamEncoding;
import org.codehaus.jremoting.client.factories.JRemotingClient;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.transports.socket.SocketTransport;
import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TestFacade3;
import org.codehaus.jremoting.itests.TestFacadeImpl;
import org.codehaus.jremoting.itests.CustomSerializableParam;
import org.codehaus.jremoting.itests.transports.AbstractHelloTestCase;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.PublicationItem;
import org.codehaus.jremoting.server.Publisher;
import org.codehaus.jremoting.server.servicehandlers.ServiceHandler;
import org.codehaus.jremoting.server.adapters.DefaultServerDelegate;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.transports.socket.SocketServer;
import org.codehaus.jremoting.requests.InvokeMethod;

import java.net.InetSocketAddress;
import java.lang.reflect.InvocationTargetException;
import java.beans.PropertyVetoException;
import java.io.IOException;


/**
 * Test showing hand crafted stubs.
 * The inner classes of HandCraftedTestFacadeStubFactory are what BCEL and ASM StubGeneraters should make.
 *
 * @author Paul Hammant
 */
public class ByteStreamOverSocketWithHandCraftedStubsAndSkelsTestCase extends AbstractHelloTestCase {

    protected void setUp() throws Exception {
        super.setUp();

        final DefaultServerDelegate dsd = new HandCraftedServerDelegate((ServerMonitor) mockServerMonitor.proxy());

        // server side setup.
        server = new SocketServer((ServerMonitor) mockServerMonitor.proxy(), dsd,
                SocketServer.defaultStreamEncoding(), SocketServer.defaultExecutor(),
                SocketServer.defaultClassLoader(),
                new InetSocketAddress(10333));
        testServer = new TestFacadeImpl();
        server.publish(testServer, "Hello", TestFacade.class, TestFacade3.class, TestFacade2.class);
        server.start();

        // Client side setup
        jremotingClient = new JRemotingClient(new SocketTransport(new ConsoleClientMonitor(), new ByteStreamEncoding(), new InetSocketAddress("localhost", 10333)),
                new ThreadLocalContextFactory(), new HandCraftedTestFacadeStubFactory());
        testClient = (TestFacade) jremotingClient.lookupService("Hello");

    }

    protected void tearDown() throws Exception {
        super.tearDown();
        testClient = null;
        System.gc();
        Thread.sleep(300);
        jremotingClient.close();
        server.stop();
    }

    private static class HandCraftedServiceHandler extends ServiceHandler {
        private static final String HASH_CODE = "hashCode()";
        private static final String PING = "ping()";
        private static final String BYTE_ARRAY_PARAM_RETURNING_BYTE_BYTE_B = "byteArrayParamReturningByte(byte, [B)";
        private static final String THROW_SPECIAL_EXCEPTION_INT = "throwSpecialException(int)";
        private static final String TEST_LONG_LONG = "testLong(long)";
        private static final String EQUALS_JAVA_LANG_OBJECT = "equals(java.lang.Object)";
        private static final String TO_STRING = "toString()";
        private static final String MAKE_NEW_TEST_OBJECT_NAMES = "makeNewTestObjectNames()";
        private static final String CHANGE_TEST_OBJECT_NAMES = "changeTestObjectNames()";
        private static final String GET_TEST_OBJECTS = "getTestObjects()";
        private static final String GET_NAME = "getName()";
        private static final String GET_TEST_FACADE2S = "getTestFacade2s()";
        private static final String SET_NAME_JAVA_LANG_STRING = "setName(java.lang.String)";
        private static final String FIND_TEST_FACADE2_BY_NAME_JAVA_LANG_STRING = "findTestFacade2ByName(java.lang.String)";
        private static final String MORPH_NAME_ORG_CODEHAUS_JREMOTING_ITESTS_TEST_FACADE2 = "morphName(org.codehaus.jremoting.itests.TestFacade2)";
        private static final String MAKE_TEST_FACADE2_OR3_JAVA_LANG_STRING = "makeTestFacade2Or3(java.lang.String)";
        private static final String FLOAT_AND_DOUBLE_PARAMS_RETURNING_STRUNG_BUFFER_FLOAT_DOUBLE = "floatAndDoubleParamsReturningStrungBuffer(float, double)";
        private static final String SHORT_PARAM_THAT_MAY_RETURN_BOOL_OR_THROW_SHORT = "shortParamThatMayReturnBoolOrThrow(short)";
        private static final String INT_PARAM_RETURNING_INT_INT = "intParamReturningInt(int)";
        private static final String HELLO_JAVA_LANG_STRING = "hello(java.lang.String)";
        private static final String TEST_SPEED = "testSpeed()";
        private static final String TEST_CUSTOM_SERIALIZABLE_PARAMETER_ORG_CODEHAUS_JREMOTING_ITESTS_CUSTOM_SERIALIZABLE_PARAM = "testCustomSerializableParameter(org.codehaus.jremoting.itests.CustomSerializableParam)";

        public HandCraftedServiceHandler(Publisher publisher, String publishedThing, Publication publicationDescription, Class facadeClass) {
            super(publisher, publishedThing, publicationDescription, facadeClass);
        }

        protected boolean isFacadeMethodSignature(String methodSignature) {
            return methodSignature.equals(TEST_SPEED) ||
                   methodSignature.equals(HELLO_JAVA_LANG_STRING) ||
                   methodSignature.equals(INT_PARAM_RETURNING_INT_INT) ||
                   methodSignature.equals(SHORT_PARAM_THAT_MAY_RETURN_BOOL_OR_THROW_SHORT) ||
                   methodSignature.equals(TEST_LONG_LONG) ||
                   methodSignature.equals(FLOAT_AND_DOUBLE_PARAMS_RETURNING_STRUNG_BUFFER_FLOAT_DOUBLE) ||
                   methodSignature.equals(MAKE_TEST_FACADE2_OR3_JAVA_LANG_STRING) ||
                   methodSignature.equals(MORPH_NAME_ORG_CODEHAUS_JREMOTING_ITESTS_TEST_FACADE2) ||
                   methodSignature.equals(FIND_TEST_FACADE2_BY_NAME_JAVA_LANG_STRING) ||
                   methodSignature.equals(SET_NAME_JAVA_LANG_STRING) ||
                   methodSignature.equals(GET_TEST_FACADE2S) ||
                   methodSignature.equals(GET_TEST_OBJECTS) ||
                   methodSignature.equals(CHANGE_TEST_OBJECT_NAMES) ||
                   methodSignature.equals(MAKE_NEW_TEST_OBJECT_NAMES) ||
                   methodSignature.equals(PING) ||
                   methodSignature.equals(HASH_CODE) ||
                   methodSignature.equals(TO_STRING) ||
                   methodSignature.equals(GET_NAME) ||
                   methodSignature.equals(EQUALS_JAVA_LANG_OBJECT) ||
                   methodSignature.equals(BYTE_ARRAY_PARAM_RETURNING_BYTE_BYTE_B) ||
                   methodSignature.equals(THROW_SPECIAL_EXCEPTION_INT) ||
                   methodSignature.equals(TEST_LONG_LONG) ||
                   methodSignature.equals(TEST_CUSTOM_SERIALIZABLE_PARAMETER_ORG_CODEHAUS_JREMOTING_ITESTS_CUSTOM_SERIALIZABLE_PARAM);
        }

        protected Object invokeFacadeMethod(InvokeMethod request, String methodSignature, Object instance) throws InvocationTargetException {
            Object[] args = request.getArgs();
            TestFacade tf = null;
            try {
                tf = (TestFacade) instance;
            } catch (Exception e) {
            }
            TestFacade2 tf2 = null;
            try {
                tf2 = (TestFacade2) instance;
            } catch (Exception e) {
            }
            TestFacade3 tf3 = null;
            try {
                tf3 = (TestFacade3) instance;
            } catch (Exception e) {
            }
            try {
                if (methodSignature.equals(TEST_SPEED)) {
                    tf.testSpeed();
                    return null;
                }
                if (methodSignature.equals(HELLO_JAVA_LANG_STRING)) {
                    tf.hello((String) args[0]);
                    return null;
                }
                if (methodSignature.equals(INT_PARAM_RETURNING_INT_INT)) {
                    return tf.intParamReturningInt((Integer) args[0]);
                }
                if (methodSignature.equals(SHORT_PARAM_THAT_MAY_RETURN_BOOL_OR_THROW_SHORT)) {
                    try {
                        return tf.shortParamThatMayReturnBoolOrThrow((Short) args[0]);
                    } catch (PropertyVetoException e) {
                        throw new InvocationTargetException(e);
                    } catch (IOException e) {
                        throw new InvocationTargetException(e);
                    }
                }
                if (methodSignature.equals(FLOAT_AND_DOUBLE_PARAMS_RETURNING_STRUNG_BUFFER_FLOAT_DOUBLE)) {
                    return tf.floatAndDoubleParamsReturningStrungBuffer((Float) args[0], (Double) args[1]);
                }
                if (methodSignature.equals(MAKE_TEST_FACADE2_OR3_JAVA_LANG_STRING)) {
                    return tf.makeTestFacade2Or3((String) args[0]);
                }
                if (methodSignature.equals(MORPH_NAME_ORG_CODEHAUS_JREMOTING_ITESTS_TEST_FACADE2)) {
                    tf.morphName((TestFacade2) args[0]);
                    return null;
                }
                if (methodSignature.equals(FIND_TEST_FACADE2_BY_NAME_JAVA_LANG_STRING)) {
                    return tf.findTestFacade2ByName((String) args[0]);
                }
                if (methodSignature.equals(SET_NAME_JAVA_LANG_STRING)) {
                    tf2.setName((String) args[0]);
                    return null;
                }
                if (methodSignature.equals(GET_TEST_FACADE2S)) {
                    return tf.getTestFacade2s();
                }
                if (methodSignature.equals(GET_NAME)) {
                    return tf2.getName();
                }
                if (methodSignature.equals(GET_TEST_OBJECTS)) {
                    return tf.getTestObjects();
                }
                if (methodSignature.equals(CHANGE_TEST_OBJECT_NAMES)) {
                    tf.changeTestObjectNames();
                    return null;
                }
                if (methodSignature.equals(MAKE_NEW_TEST_OBJECT_NAMES)) {
                    tf.makeNewTestObjectNames();
                    return null;
                }
                if (methodSignature.equals(TO_STRING)) {
                    return tf.toString();
                }
                if (methodSignature.equals(HASH_CODE)) {
                    return tf.hashCode();
                }
                if (methodSignature.equals(PING)) {
                    tf.ping();
                    return null;
                }
                if (methodSignature.equals(BYTE_ARRAY_PARAM_RETURNING_BYTE_BYTE_B)) {
                    return tf.byteArrayParamReturningByte((Byte) args[0], (byte[]) args[1]);
                }
                if (methodSignature.equals(THROW_SPECIAL_EXCEPTION_INT)) {
                    tf.throwSpecialException((Integer) args[0]);
                    return null;
                }
                if (methodSignature.equals(TEST_LONG_LONG)) {
                    tf.testLong((Long) args[0]);
                    return null;
                }
                if (methodSignature.equals(EQUALS_JAVA_LANG_OBJECT)) {
                    if (tf != null) {
                        return tf.equals(args[0]);
                    } else if (tf2 != null) {
                        return tf2.equals(args[0]);
                    } else {
                        return tf3.equals(args[0]);
                    }
                }
                if (methodSignature.equals(TEST_CUSTOM_SERIALIZABLE_PARAMETER_ORG_CODEHAUS_JREMOTING_ITESTS_CUSTOM_SERIALIZABLE_PARAM)) {
                    return tf.testCustomSerializableParameter((CustomSerializableParam) args[0]);
                }
            } catch (RuntimeException e) {
                throw new InvocationTargetException(e);
            } catch (Error e) {
                throw new InvocationTargetException(e);
            }
            return null;
        }
    }
}