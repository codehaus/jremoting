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
package org.codehaus.jremoting.itests;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

/**
 * Class TestInterfaceImpl
 *
 * @author Paul Hammant <a href="mailto:Paul_Hammant@yahoo.com">Paul_Hammant@yahoo.com</a>
 * @author Benjamin David Hall
 * @version $Revision: 1.3 $
 */
public class TestInterfaceImpl implements TestFacade {

    Vector ti2Holder = new Vector();
    TstObject[] testObjects;


    HashMap storedState = new HashMap();

    public Object getStoredState(String key) {
        return storedState.get(key);
    }

    public void hello(String greeting) {
        storedState.put("void:hello(String)", greeting);
    }

    public int intParamReturningInt(int greeting) {
        storedState.put("int:intParamReturningInt(int)", "" + greeting);
        return greeting;
    }

    public boolean shortParamThatMayReturnBoolOrThrow(short greeting) throws PropertyVetoException, IOException {
        storedState.put("boolean:shortParamThatMayReturnBoolOrThrow(short)", "" + greeting);
        switch (greeting) {
            case 90:
                throw new PropertyVetoException("Forced Exception Test", null);
            case 91:
                throw new IOException("Forced Exception");
        }
        return true;
    }

    public StringBuffer floatAndDoubleParamsReturningStrungBuffer(float greeting1, double greeting2) {
        StringBuffer sb = new StringBuffer("" + greeting1 + " " + greeting2);
        storedState.put("StringBuffer:floatAndDoubleParamsReturningStrungBuffer(float,double)", sb);
        return sb;
    }

    public void testSpeed() {
        // do nothing
    }

    /**
     * Method makeTestFacade2Or3
     *
     * @param thingName
     * @return
     */
    public TestFacade2 makeTestFacade2Or3(String thingName) {

        TestFacade2 ti2;
        if (thingName.equals("abc")) {
            // even calls only
            ti2 = new TstInterface3Impl(new Date(), thingName);
        } else {
            ti2 = new TstInterface2Impl(thingName);
        }

        ti2Holder.add(ti2);

        return ti2;
    }

    /**
     * Method morphName
     *
     * @param forThisImpl
     */
    public void morphName(TestFacade2 forThisImpl) {

        String name = forThisImpl.getName();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < name.length(); i++) {
            sb.append(name.substring(i, i + 1).toUpperCase());
            sb.append("_");
        }

        forThisImpl.setName(sb.toString());
    }

    /**
     * Method findTestInterface2ByName
     *
     * @param nameToFind
     * @return
     */
    public TestFacade2 findTestInterface2ByName(String nameToFind) {

        for (int i = 0; i < ti2Holder.size(); i++) {
            TestFacade2 ti2 = (TestFacade2) ti2Holder.elementAt(i);

            if (ti2.getName().equals(nameToFind)) {
                return ti2;
            }
        }

        return new TstInterface2Impl("Not Found");
    }

    /**
     * Method getTestInterfaces
     *
     * @return
     */
    public TestFacade2[] getTestInterface2s() {

        TestFacade2[] retVal = new TestFacade2[ti2Holder.size()];

        for (int i = 0; i < ti2Holder.size(); i++) {
            TestFacade2 interface2 = (TestFacade2) ti2Holder.elementAt(i);

            retVal[i] = interface2;
        }

        return retVal;
    }

    /**
     * Method getTestObjects
     * Helps illustrate the bug http://developer.java.sun.com/developer/bugParade/bugs/4499841.html
     *
     * @return
     */
    public TstObject[] getTestObjects() {

        if (testObjects == null) {
            testObjects = new TstObject[3];
            testObjects[0] = new TstObject("AAA");
            testObjects[1] = new TstObject("BBB");
            testObjects[2] = new TstObject("CCC");
        }

        return testObjects;
    }

    /**
     * Method changeTestObjectNames
     * Helps illustrate the bug http://developer.java.sun.com/developer/bugParade/bugs/4499841.html
     */
    public void changeTestObjectNames() {

        testObjects[0].setName("aaa");
        testObjects[1].setName("bbb");
        testObjects[2].setName("ccc");
    }

    /**
     * Method makeNewTestObjectNames
     * Helps illustrate the bug http://developer.java.sun.com/developer/bugParade/bugs/4499841.html
     */
    public void makeNewTestObjectNames() {

        testObjects[0] = new TstObject("aAa");
        testObjects[1] = new TstObject("bBb");
        testObjects[2] = new TstObject("cCc");
    }

    protected void finalize() throws Throwable {
        super.finalize();
        //System.out.println( "impl finalized" );
    }


    public void ping() {
    }

    public byte byteArrayParamReturningByte(byte b, byte[] array) {
        storedState.put("byte:byteArrayParamReturningByte(byte, byte[]#1)", "" + b);
        storedState.put("byte:byteArrayParamReturningByte(byte, byte[]#2)", array);
        byte val = 0;
        for (int i = 0; i < array.length; i++) {
            val += array[i];
        }
        return (byte) ((b * 2) + val);
    }

    public void throwSpecialException(int i) {
        if (i == 0) {
            throw new RuntimeException("hello");
        } else if (i == 1) {
            throw new Error("world");
        }
    }

    public void testLong(long l) {
    }

    public String toString() {
        return "YeeeeHaaaa";
    }

    public CustomSerializableParam testCustomSerializableParameter(CustomSerializableParam param) {
        return param;
    }

}
