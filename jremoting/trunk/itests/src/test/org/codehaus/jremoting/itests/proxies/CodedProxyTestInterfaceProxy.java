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
package org.codehaus.jremoting.itests.proxies;

import org.codehaus.jremoting.itests.CustomSerializableParam;
import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TstObject;

import java.beans.PropertyVetoException;
import java.io.IOException;

/**
 * Class CodedProxyTestInterfaceProxy
 *
 * @author Paul Hammant <a href="mailto:Paul_Hammant@yahoo.com">Paul_Hammant@yahoo.com</a>
 * @author Benjamin David Hall
 * @version $Revision: 1.3 $
 */
public class CodedProxyTestInterfaceProxy implements TestFacade {

    private TestFacade actualImpl;

    /**
     * Constructor CodedProxyTestInterfaceProxy
     *
     * @param actualImpl
     */
    public CodedProxyTestInterfaceProxy(TestFacade actualImpl) {
        this.actualImpl = actualImpl;
    }

    /**
     * Method hello
     *
     * @param greeting
     */
    public void hello(String greeting) {
        actualImpl.hello(greeting);
    }

    /**
     * Method intParamReturningInt
     *
     * @param greeting
     */
    public int intParamReturningInt(int greeting) {
        return actualImpl.intParamReturningInt(greeting);
    }

    /**
     * Method shortParamThatMayReturnBoolOrThrow
     *
     * @param greeting
     * @return
     * @throws IOException
     * @throws PropertyVetoException
     */
    public boolean shortParamThatMayReturnBoolOrThrow(short greeting) throws PropertyVetoException, IOException {
        return actualImpl.shortParamThatMayReturnBoolOrThrow(greeting);
    }

    /**
     * Method floatAndDoubleParamsReturningStrungBuffer
     *
     * @param greeting1
     * @param greeting2
     * @return
     */
    public StringBuffer floatAndDoubleParamsReturningStrungBuffer(float greeting1, double greeting2) {
        return actualImpl.floatAndDoubleParamsReturningStrungBuffer(greeting1, greeting2);
    }

    /**
     * Method testSpeed
     */
    public void testSpeed() {
        actualImpl.testSpeed();
    }

    /**
     * Method makeTestFacade2Or3
     *
     * @param thingName
     * @return
     */
    public TestFacade2 makeTestFacade2Or3(String thingName) {
        return actualImpl.makeTestFacade2Or3(thingName);
    }

    /**
     * Method morphName
     *
     * @param forThisImpl
     */
    public void morphName(TestFacade2 forThisImpl) {
        actualImpl.morphName(forThisImpl);
    }

    /**
     * Method findTestInterface2ByName
     *
     * @param nameToFind
     * @return
     */
    public TestFacade2 findTestInterface2ByName(String nameToFind) {
        return actualImpl.findTestInterface2ByName(nameToFind);
    }

    /**
     * Method getTestInterface2s
     *
     * @return
     */
    public TestFacade2[] getTestInterface2s() {
        return actualImpl.getTestInterface2s();
    }

    /**
     * Method getTestObjects
     * Helps ilustrate the bug http://developer.java.sun.com/developer/bugParade/bugs/4499841.html
     *
     * @return
     */
    public TstObject[] getTestObjects() {
        return actualImpl.getTestObjects();
    }

    /**
     * Method changeTestObjectNames
     * Helps ilustrate the bug http://developer.java.sun.com/developer/bugParade/bugs/4499841.html
     */
    public void changeTestObjectNames() {
        actualImpl.changeTestObjectNames();
    }

    /**
     * Method makeNewTestObjectNames
     * Helps ilustrate the bug http://developer.java.sun.com/developer/bugParade/bugs/4499841.html
     */
    public void makeNewTestObjectNames() {
        actualImpl.makeNewTestObjectNames();
    }

    public void ping() {
        actualImpl.ping();
    }

    public byte byteArrayParamReturningByte(byte b, byte[] array) {
        return actualImpl.byteArrayParamReturningByte(b, array);
    }

    public void throwSpecialException(int i) {
        actualImpl.throwSpecialException(i);
    }

    public void testLong(long l) {
    }

    public String toString() {
        return actualImpl.toString();
    }

    public int hashCode() {
        return actualImpl.hashCode();
    }

    public CustomSerializableParam testCustomSerializableParameter(CustomSerializableParam param) {
        return actualImpl.testCustomSerializableParameter(param);
    }

}
