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

import junit.framework.Assert;
import org.codehaus.jremoting.itests.AbstractJRemotingTestCase;
import org.codehaus.jremoting.itests.CustomSerializableParam;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TestFacade3;
import org.codehaus.jremoting.itests.TestFacadeImpl;
import org.codehaus.jremoting.itests.TstObject;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Extended by classes that name the transport.
 *
 * @author Paul Hammant
 * @author Benjamin David Hall
 */
public abstract class AbstractHelloTestCase extends AbstractJRemotingTestCase {


    public void testIntParamReturningInt() throws Exception {
        // lookup worked ?
        assertNotNull(testClient);

        // Invoke a method over rpc.
        int retVal = testClient.intParamReturningInt(11);

        // test our returned result
        assertEquals(11, retVal);

        // test the server has logged the message.
        Assert.assertEquals("11", ((TestFacadeImpl) testServer).getStoredState("int:intParamReturningInt(int)"));
    }

    public void testByteArrayParamReturningByte() throws Exception {
        // lookup worked ?
        assertNotNull(testClient);

        // Invoke a method over rpc.
        byte retVal = testClient.byteArrayParamReturningByte((byte) 5, new byte[]{1, 2});

        // test our returned result
        assertEquals(13, retVal);

        // test the server has logged the message.
        assertEquals("5", ((TestFacadeImpl) testServer).getStoredState("byte:byteArrayParamReturningByte(byte, byte[]#1)"));
    }

    /**
     * Test throwing special exceptions.
     *
     * @throws Exception
     */
    public void testThrowOfUncheckedExceptionAndError() throws Exception {
        // lookup worked ?
        assertNotNull(testClient);

        try {
            // 0 for this method causes RuntimeException 'hello' as message
            testClient.throwSpecialException(0);
            fail();
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().equals("hello"));
        }

        try {
            // 1 for this method causes Error with 'world' as message
            testClient.throwSpecialException(1);
            fail();
        } catch (Error e) {
            assertTrue(e.getMessage().equals("world"));
        }
    }

    public void testShortParamReturningBoolean() throws Exception {
        // lookup worked ?
        assertNotNull(testClient);

        // Invoke a method over rpc.
        boolean retVal = testClient.shortParamThatMayReturnBoolOrThrow((short) 22);

        // test our returned result
        assertTrue(retVal);

        // test the server has logged the message.
        assertEquals("22", ((TestFacadeImpl) testServer).getStoredState("boolean:shortParamThatMayReturnBoolOrThrow(short)"));
    }


    public void testConditionalThrowsOfCheckedException() throws Exception {
        // lookup worked ?
        assertNotNull(testClient);

        // Invoke a method over rpc.
        try {
            // 90 for this method causes PropertyVetoException
            testClient.shortParamThatMayReturnBoolOrThrow((short) 90);
            fail("Expected a Excaption to be throw for hardcoded test 90");
        } catch (PropertyVetoException e) {
            // expected
        } catch (IOException e) {
            fail("Wrong exception throw for hardcoded test 90");
        }
        // test the server has logged the message.
        assertEquals("90", ((TestFacadeImpl) testServer).getStoredState("boolean:shortParamThatMayReturnBoolOrThrow(short)"));

        try {
            // 91 for this method causes IOException
            testClient.shortParamThatMayReturnBoolOrThrow((short) 91);
            fail("Expected a Exception to be throw for hardcoded test 91");
        } catch (PropertyVetoException e) {
            fail("Wrong exception throw for hardcoded test 91");
        } catch (IOException e) {
            // expected
        }
        // test the server has logged the message.
        assertEquals("91", ((TestFacadeImpl) testServer).getStoredState("boolean:shortParamThatMayReturnBoolOrThrow(short)"));

    }

    public void testFloatAndDoubleParamsReturningStrungBuffer() throws Exception {
        // lookup worked ?
        assertNotNull(testClient);

        // Invoke a method over rpc.
        StringBuffer sb = testClient.floatAndDoubleParamsReturningStrungBuffer((float) 10.2, (double) 11.9);
        StringBuffer sb2 = (StringBuffer) ((TestFacadeImpl) testServer).getStoredState("StringBuffer:floatAndDoubleParamsReturningStrungBuffer(float,double)");

        // test the server has logged the message.
        assertEquals("10.2 11.9", sb2.toString());
        // test if the same instance.
        //assertEquals(sb, sb2);
    }

    public void testBasicSecondryFacade() throws Exception {
        // lookup worked ?
        assertNotNull(testClient);

        TestFacade2 xyz = testClient.makeTestFacade2Or3("XYZ");

        assertEquals("XYZ", xyz.getName());

        xyz.setName("123");

        assertEquals("123", xyz.getName());


    }

    public void testSecondaryFacadeCanBeMoreDerivedThanDeclaration() throws Exception {
        // lookup worked ?
        assertNotNull(testClient);

        TestFacade2 testInterface2 = testClient.makeTestFacade2Or3("abc");
        TestFacade3 abc = (TestFacade3) testInterface2;
        TestFacade2 def = testClient.makeTestFacade2Or3("def");

        testClient.morphName(abc);

        assertEquals("A_B_C_", abc.getName());

        TestFacade2 def2 = testClient.findTestFacade2ByName("def");

        assertNotNull(def2);
        assertTrue(def == def2);

        TestFacade2[] ti2s = testClient.getTestFacade2s();

        assertNotNull(ti2s);

        assertEquals("Array of returned testInterface2s should be two", 2, ti2s.length);

        for (int i = 0; i < ti2s.length; i++) {
            TestFacade2 ti2 = ti2s[i];
            assertNotNull(ti2);
        }

    }

    /**
     *
     */
    public void testExistanceOfBugParadeBugNumber4499841() throws Exception {
        TstObject[] tos = testClient.getTestObjects();
        testClient.changeTestObjectNames();
        TstObject[] tos2 = testClient.getTestObjects();
        for (int i = 0; i < tos.length; i++) {
            TstObject to = tos[i];
            TstObject to2 = tos2[i];
            if (bugParadeBug4499841StillExists) {
                // the transport in question highlights bug parade #4499841
                assertEquals(to.getName().toLowerCase(), to2.getName());
            } else {
                // the transport in question is immune to bug parade #4499841
                assertFalse(to.getName().toLowerCase().equals(to2.getName()));
            }
        }
    }

    public void testSpeed() throws Exception {

        int iterations = getNumIterationsForSpeedTest();

        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            testClient.testSpeed();
        }
        long end = System.currentTimeMillis();

        BigDecimal its = BigDecimal.valueOf(iterations);
        BigDecimal dur = BigDecimal.valueOf(end - start);
        BigDecimal durInSecs = dur.divide(BigDecimal.valueOf(1000));
        BigDecimal numPerSec = its.divide(durInSecs, 0, RoundingMode.HALF_EVEN);
        System.err.println("[testSpeed] " + this.getClass().getName() + " "
                + numPerSec + " reqs/sec, duration " + dur.toString() + " milliseconds");

    }

    protected int getNumIterationsForSpeedTest() {
        int iterations = 1000; // default
        String iterationsStr = "@SPEEDTEST-ITERATIONS@";
        try {
            iterations = Integer.parseInt(iterationsStr);
        } catch (NumberFormatException e) {
            // half expected.  The @SPEEDTEST-ITERATIONS@ thing above may be replaced by the
            // the Ant task before the test is run.  However this may be run in a
            // IDE, in which case the test is not run.
        }
        return iterations;
    }

    public void testToString() {

        // lookup worked ?
        assertNotNull(testClient);

        // Invoke a method over rpc.
        String retVal = testClient.toString();

        // test our returned result
        assertEquals("YeeeeHaaaa", retVal);

    }

    public void testEquals() {
        assertTrue(!testClient.equals(null));
        assertTrue(!testClient.equals("ha!"));

        TestFacade2 one = testClient.makeTestFacade2Or3("equals-test-one");
        TestFacade2 two = testClient.makeTestFacade2Or3("equals-test-two");

        // These seem to contradict at first glance, but it is what we want.
        assertFalse(one == two);
        assertTrue(one.equals(two));


    }

    public void testLongParamMethod() {

        testClient.testLong((long) 1);

    }

    public void testCustomSerializableParameter() {
        // lookup worked ?
        assertNotNull(testClient);

        CustomSerializableParam sendParam = new CustomSerializableParam();
        sendParam.name = "sent-by-caller";
        CustomSerializableParam recvParam = testClient.testCustomSerializableParameter(sendParam);
        //test receipt of serialized value object from server
        assertNotNull(recvParam);
        //check whether its the same as one sent (server merely echos back whatever it received)
        assertEquals(sendParam.name, recvParam.name);
    }

}
