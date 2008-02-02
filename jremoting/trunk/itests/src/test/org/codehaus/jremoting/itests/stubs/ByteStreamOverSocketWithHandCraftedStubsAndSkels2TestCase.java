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
import org.codehaus.jremoting.itests.CustomSerializableParam;
import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TestFacade3;
import org.codehaus.jremoting.itests.TestFacadeImpl;
import org.codehaus.jremoting.itests.transports.AbstractHelloTestCase;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.PublicationItem;
import org.codehaus.jremoting.server.Publisher;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.adapters.DefaultServerDelegate;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.servicehandlers.ServiceHandler;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.transports.socket.SocketServer;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;


/**
 * Test showing hand crafted stubs.
 * The inner classes of HandCraftedTestFacadeStubFactory are what BCEL and ASM StubGeneraters should make.
 *
 * @author Paul Hammant
 */
public class ByteStreamOverSocketWithHandCraftedStubsAndSkels2TestCase extends AbstractHelloTestCase {

    protected void setUp() throws Exception {
        super.setUp();

        final DefaultServerDelegate dsd = new MyDefaultServerDelegate2((ServerMonitor) mockServerMonitor.proxy());

        // server side setup.
        server = new SocketServer((ServerMonitor) mockServerMonitor.proxy(), dsd,
                SocketServer.defaultStreamEncoding(), SocketServer.defaultExecutor(),
                SocketServer.defaultClassLoader(),
                new InetSocketAddress(10333));
        testServer = new TestFacadeImpl();
        server.publish(testServer, "Hello", TestFacade.class, TestFacade3.class, TestFacade2.class);
        server.start();

        // Client side setup
        jremotinClient = new JRemotingClient(new SocketTransport(new ConsoleClientMonitor(), new ByteStreamEncoding(), new InetSocketAddress("localhost", 10333)),
                new ThreadLocalContextFactory(), new HandCraftedTestFacadeStubFactory());
        testClient = (TestFacade) jremotinClient.lookupService("Hello");

    }


    protected void tearDown() throws Exception {
        super.tearDown();
        testClient = null;
        System.gc();
        Thread.sleep(300);
        jremotinClient.close();
        server.stop();
    }

    public static class HandCraftedServiceHandler extends ServiceHandler {

        Map<String, Execr> map = new HashMap<String, Execr>();

        public HandCraftedServiceHandler(Publisher publisher, String publishedThing, Publication publicationDescription, Class facadeClass) {
            super(publisher, publishedThing, publicationDescription, facadeClass);

            map.put("hashCode()", new HashCode());
            map.put("testSpeed()", new TestSpeed());
            map.put("hello(java.lang.String)", new Hello());
            map.put("intParamReturningInt(int)", new IntParam());
            map.put("shortParamThatMayReturnBoolOrThrow(short)", new ShortParam());
            map.put("floatAndDoubleParamsReturningStrungBuffer(float, double)", new FloatAnd());
            map.put("makeTestFacade2Or3(java.lang.String)", new MakeTest());
            map.put("morphName(org.codehaus.jremoting.itests.TestFacade2)", new Morph());
            map.put("findTestFacade2ByName(java.lang.String)", new FindTest());
            map.put("setName(java.lang.String)", new SetName());
            map.put("getTestFacade2s()", new GetTest());
            map.put("getName()", new GetName());
            map.put("getTestObjects()", new GetTestObjects());
            map.put("changeTestObjectNames()", new ChgTestObjs());
            map.put("makeNewTestObjectNames()", new MakeNew());
            map.put("toString()", new ToString());
            map.put("ping()", new Ping());
            map.put("byteArrayParamReturningByte(byte, [B)", new ByteArray());
            map.put("throwSpecialException(int)", new ThrowSpecial());
            map.put("testLong(long)", new TestLong());
            map.put("equals(java.lang.Object)", new Equals());
            map.put("testCustomSerializableParameter(org.codehaus.jremoting.itests.CustomSerializableParam)", new TestCustom());
        }

        protected boolean isFacadeMethodSignature(String methodSignature) {
            return map.containsKey(methodSignature);
        }

        protected Object invokeFacadeMethod(InvokeMethod request, String methodSignature, Object instance) throws InvocationTargetException {
            Object[] args = request.getArgs();
            try {
                return map.get(methodSignature).exec(instance, args);
            } catch (RuntimeException e) {
                throw new InvocationTargetException(e);
            } catch (Error e) {
                throw new InvocationTargetException(e);
            }
        }

        abstract static class Execr {
            abstract Object exec(Object target, Object[] args) throws InvocationTargetException;
        }


        private static class HashCode extends Execr {
            Object exec(Object target, Object[] args) {
                return ((TestFacade) target).hashCode();
            }
        }

        private static class TestSpeed extends Execr {
            Object exec(Object target, Object[] args) {
                ((TestFacade) target).testSpeed();
                return null;
            }
        }

        private static class Hello extends Execr {
            Object exec(Object target, Object[] args) {
                ((TestFacade) target).hello((String) args[0]);
                return null;
            }
        }

        private static class IntParam extends Execr {
            Object exec(Object target, Object[] args) {
                return ((TestFacade) target).intParamReturningInt((Integer) args[0]);
            }
        }

        private static class ShortParam extends Execr {
            Object exec(Object target, Object[] args) throws InvocationTargetException {
                try {
                    return ((TestFacade) target).shortParamThatMayReturnBoolOrThrow((Short) args[0]);
                } catch (PropertyVetoException e) {
                    throw new InvocationTargetException(e);
                } catch (IOException e) {
                    throw new InvocationTargetException(e);
                }
            }
        }

        private static class FloatAnd extends Execr {
            Object exec(Object target, Object[] args) {
                return ((TestFacade) target).floatAndDoubleParamsReturningStrungBuffer((Float) args[0], (Double) args[1]);
            }
        }

        private static class MakeTest extends Execr {
            Object exec(Object target, Object[] args) {
                return ((TestFacade) target).makeTestFacade2Or3((String) args[0]);
            }
        }

        private static class Morph extends Execr {
            Object exec(Object target, Object[] args) {
                ((TestFacade) target).morphName((TestFacade2) args[0]);
                return null;
            }
        }

        private static class FindTest extends Execr {
            Object exec(Object target, Object[] args) {
                return ((TestFacade) target).findTestFacade2ByName((String) args[0]);

            }
        }

        private static class SetName extends Execr {
            Object exec(Object target, Object[] args) {
                ((TestFacade2) target).setName((String) args[0]);
                return null;
            }
        }

        private static class GetTest extends Execr {
            Object exec(Object target, Object[] args) {
                return ((TestFacade) target).getTestFacade2s();
            }
        }

        private static class GetName extends Execr {
            Object exec(Object target, Object[] args) {
                return ((TestFacade2) target).getName();
            }
        }

        private static class GetTestObjects extends Execr {
            Object exec(Object target, Object[] args) {
                return ((TestFacade) target).getTestObjects();
            }
        }

        private static class ChgTestObjs extends Execr {
            Object exec(Object target, Object[] args) {
                ((TestFacade) target).changeTestObjectNames();
                return null;
            }
        }

        private static class MakeNew extends Execr {
            Object exec(Object target, Object[] args) {
                ((TestFacade) target).makeNewTestObjectNames();
                return null;
            }
        }

        private static class ToString extends Execr {
            Object exec(Object target, Object[] args) {
                String s = ((TestFacade) target).toString();
                return s;
            }
        }

        private static class Ping extends Execr {
            Object exec(Object target, Object[] args) {
                ((TestFacade) target).ping();
                return null;
            }
        }

        private static class ByteArray extends Execr {
            Object exec(Object target, Object[] args) {
                return ((TestFacade) target).byteArrayParamReturningByte((Byte) args[0], (byte[]) args[1]);
            }
        }

        private static class ThrowSpecial extends Execr {
            Object exec(Object target, Object[] args) {
                ((TestFacade) target).throwSpecialException((Integer) args[0]);
                return null;
            }
        }

        private static class TestLong extends Execr {
            Object exec(Object target, Object[] args) {
                ((TestFacade) target).testLong((Long) args[0]);
                return null;
            }
        }

        private static class Equals extends Execr {
            Object exec(Object target, Object[] args) {
                if (target instanceof TestFacade) {
                    return ((TestFacade) target).equals(args[0]);
                } else if (target instanceof TestFacade3) {
                    return ((TestFacade3) target).equals(args[0]);
                } else {
                    return ((TestFacade2) target).equals(args[0]);
                }
            }
        }

        private static class TestCustom extends Execr {
            Object exec(Object target, Object[] args) {
                return ((TestFacade) target).testCustomSerializableParameter((CustomSerializableParam) args[0]);
            }
        }
    }

    public static class MyDefaultServerDelegate2 extends DefaultServerDelegate {
        public MyDefaultServerDelegate2(ServerMonitor serverMonitor) {
            super(serverMonitor, new RefusingStubRetriever(), new NullAuthenticator(), new ThreadLocalServerContextFactory());
        }

        protected ServiceHandler makeServiceHandler(String thing, Publication publicationDescription, PublicationItem item) {
            return new HandCraftedServiceHandler(MyDefaultServerDelegate2.this, thing, publicationDescription, item.getFacadeClass());
        }
    }
}