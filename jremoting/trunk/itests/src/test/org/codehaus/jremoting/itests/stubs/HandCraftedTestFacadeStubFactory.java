package org.codehaus.jremoting.itests.stubs;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.StubFactory;
import org.codehaus.jremoting.client.StubHelper;
import org.codehaus.jremoting.client.Transport;
import org.codehaus.jremoting.client.stubs.StubsViaReflection;
import org.codehaus.jremoting.itests.CustomSerializableParam;
import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TestFacade3;
import org.codehaus.jremoting.itests.TstObject;
import org.codehaus.jremoting.util.MethodNameHelper;
import org.codehaus.jremoting.util.StaticStubHelper;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Date;

public class HandCraftedTestFacadeStubFactory implements StubFactory {

    public Object instantiateStub(String facadeClassName, String publishedServiceName, String objectName,
                                  Transport transport, StubHelper stubHelper) throws ConnectionException {
        if (facadeClassName.equals(TestFacade.class.getName())) {
            return new MyTestFacade(stubHelper, publishedServiceName, objectName);
        } else if (facadeClassName.equals(TestFacade2.class.getName())) {
            return new MyTestFacade2(stubHelper, publishedServiceName, objectName);
        } else if (facadeClassName.equals(TestFacade3.class.getName())) {
            return new MyTestFacade3(stubHelper, publishedServiceName, objectName);
        }
        return null;
    }

    private static void jremotingStandardExceptionHandling(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        throw new RuntimeException("??", throwable);

    }

    private static class MyBase implements StubsViaReflection.ReflectionStub {
        protected final StubHelper stubHelper;
        protected final String publishedServiceName;
        protected final String objectName;

        public MyBase(StubHelper stubHelper, String publishedServiceName, String objectName) {
            this.stubHelper = stubHelper;
            this.publishedServiceName = publishedServiceName;
            this.objectName = objectName;
        }

        public Long jRemotingGetReferenceID(Object factoryThatIsAsking) {
            return stubHelper.getReference(factoryThatIsAsking);
        }

        public String jRemotingGetObjectName() {
            return StaticStubHelper.formatServiceName(publishedServiceName, objectName);
        }

        public String toString() {
            try {
                return (String) stubHelper.processObjectRequest("toString()", new Object[0], new Class[0]);
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public boolean equals(Object obj) {
            try {
                return (Boolean) stubHelper.processObjectRequest("equals(java.lang.Object)", new Object[] {obj}, new Class[] {Object.class});
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }
    }

    private static class MyTestFacade extends MyBase implements TestFacade, StubsViaReflection.ReflectionStub {

        private MyTestFacade(StubHelper stubHelper, String publishedServiceName, String objectName) {
            super(stubHelper, publishedServiceName, objectName);
        }

        public void hello(String greeting) {
            try {
                stubHelper.processVoidRequest("hello(java.lang.String)", new Object[] {greeting}, new Class[] {String.class});
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public int intParamReturningInt(int greeting) {
            try {
                return (Integer) stubHelper.processObjectRequest("intParamReturningInt(int)", new Object[] {greeting}, new Class[] {Integer.TYPE});
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public boolean shortParamThatMayReturnBoolOrThrow(short greeting) throws PropertyVetoException, IOException {
            try {
                return (Boolean) stubHelper.processObjectRequest("shortParamThatMayReturnBoolOrThrow(short)", new Object[] {greeting}, new Class[] {Short.TYPE});
            } catch (PropertyVetoException e) {
                throw e;
            } catch (IOException e) {
                throw e;
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public StringBuffer floatAndDoubleParamsReturningStrungBuffer(float greeting1, double greeting2) {
            try {
                return (StringBuffer) stubHelper.processObjectRequest("floatAndDoubleParamsReturningStrungBuffer(float, double)", new Object[] {greeting1, greeting2}, new Class[] {Short.TYPE});
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public void testSpeed() {
            try {
                stubHelper.processVoidRequest("testSpeed()", new Object[0], new Class[0]);
                return;
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public TestFacade2 makeTestFacade2Or3(String thingName) {
            try {
                Object o = stubHelper.processObjectRequestGettingFacade(TestFacade2.class, "makeTestFacade2Or3(java.lang.String)", new Object[]{thingName}, MethodNameHelper.encodeClassName(TestFacade2.class));
                return (TestFacade2) o;
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public void morphName(TestFacade2 forThisImpl) {
            try {
                stubHelper.processVoidRequest("morphName(org.codehaus.jremoting.itests.TestFacade2)", new Object[] { forThisImpl }, new Class[] {TestFacade2.class});
                return;
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public TestFacade2 findTestFacade2ByName(String nameToFind) {
            try {
                return (TestFacade2) stubHelper.processObjectRequestGettingFacade(TestFacade2.class, "findTestFacade2ByName(java.lang.String)", new Object[] {nameToFind}, MethodNameHelper.encodeClassName(TestFacade2.class));
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public TestFacade2[] getTestFacade2s() {
            try {
                return (TestFacade2[]) stubHelper.processObjectRequestGettingFacade(TestFacade2.class, "getTestFacade2s()", new Object[0], MethodNameHelper.encodeClassName(TestFacade2.class) + "[]");
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public TstObject[] getTestObjects() {
            try {
                return (TstObject[]) stubHelper.processObjectRequest("getTestObjects()", new Object[0], new Class[0]);
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public void changeTestObjectNames() {
            try {
                stubHelper.processVoidRequest("changeTestObjectNames()", new Object[0], new Class[0]);
                return;
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public void makeNewTestObjectNames() {
            try {
                stubHelper.processVoidRequest("makeNewTestObjectNames()", new Object[0], new Class[0]);
                return;
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public void ping() {
            try {
                stubHelper.processVoidRequest("ping()", new Object[0], new Class[0]);
                return;
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public byte byteArrayParamReturningByte(byte b, byte[] array) {
            try {
                Class[] classes = {Byte.TYPE, byte[].class};
                return (Byte) stubHelper.processObjectRequest("byteArrayParamReturningByte(byte, [B)", new Object[]{b, array}, classes);
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public void throwSpecialException(int i) {
            try {
                stubHelper.processVoidRequest("throwSpecialException(int)", new Object[]{i}, new Class[] { Integer.TYPE });
                return;
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public void testLong(long l) {
            try {
                stubHelper.processVoidRequest("testLong(long)", new Object[]{l}, new Class[] { Long.TYPE });
                return;
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public CustomSerializableParam testCustomSerializableParameter(CustomSerializableParam param) {
            try {
                return (CustomSerializableParam) stubHelper.processObjectRequest("testCustomSerializableParameter(org.codehaus.jremoting.itests.CustomSerializableParam)", new Object[]{param}, new Class[] { CustomSerializableParam.class});
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

    }

    private static class MyTestFacade2 extends MyBase implements TestFacade2, StubsViaReflection.ReflectionStub {

        public MyTestFacade2(StubHelper stubHelper, String publishedServiceName, String objectName) {
            super(stubHelper, publishedServiceName, objectName);
        }

        public void setName(String newThingName) {
            try {
                stubHelper.processVoidRequest("setName(java.lang.String)", new Object[] {newThingName}, new Class[] {String.class});
                return;
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public String getName() {
            try {
                return (String) stubHelper.processObjectRequest("getName()", new Object[0], new Class[0]);
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }
    }

    private static class MyTestFacade3 extends MyTestFacade2 implements TestFacade3 {

        private MyTestFacade3(StubHelper stubHelper2, String publishedServiceName, String objectName) {
            super(stubHelper2, publishedServiceName, objectName);
        }

        public void setDOB(Date dob) {
            try {
                stubHelper.processVoidRequest("setDOB(java.util.Date)", new Object[] {dob}, new Class[] {Date.class});
                return;
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }

        public Date getDOB() {
            try {
                return (Date) stubHelper.processObjectRequest("getDOB()", new Object[0], new Class[0]);
            } catch (Throwable throwable) {
                jremotingStandardExceptionHandling(throwable);
                throw new RuntimeException(); // never called
            }
        }
    }

}
