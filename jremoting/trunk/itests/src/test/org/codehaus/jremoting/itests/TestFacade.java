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

/**
 * Interface TestFacade
 *
 * @author Paul Hammant <a href="mailto:Paul_Hammant@yahoo.com">Paul_Hammant@yahoo.com</a>
 * @author Benjamin David Hall
 * @version * $Revision: 1.3 $
 */
public interface TestFacade {

    /**
     * Method hello
     *
     * @param greeting
     */
    void hello(String greeting);

    /**
     * Method intParamReturningInt
     *
     * @param greeting
     */
    int intParamReturningInt(int greeting);

    /**
     * Method shortParamThatMayReturnBoolOrThrow
     *
     * @param greeting
     * @return
     * @throws IOException
     * @throws PropertyVetoException
     */
    boolean shortParamThatMayReturnBoolOrThrow(short greeting) throws PropertyVetoException, IOException;

    /**
     * Method floatAndDoubleParamsReturningStrungBuffer
     *
     * @param greeting1
     * @param greeting2
     * @return
     */
    StringBuffer floatAndDoubleParamsReturningStrungBuffer(float greeting1, double greeting2);

    /**
     * Method testSpeed
     */
    void testSpeed();


    /**
     * Method makeTestFacade2Or3
     *
     * @param thingName
     * @return
     */
    TestFacade2 makeTestFacade2Or3(String thingName);

    /**
     * Method morphName
     *
     * @param forThisImpl
     */
    void morphName(TestFacade2 forThisImpl);

    /**
     * Method findTestFacade2ByName
     *
     * @param nameToFind
     * @return
     */
    TestFacade2 findTestFacade2ByName(String nameToFind);

    /**
     * Method getTestFacade2s
     *
     * @return
     */
    TestFacade2[] getTestFacade2s();

    /**
     * Method getTestObjects
     * Helps ilustrate the bug http://developer.java.sun.com/developer/bugParade/bugs/4499841.html
     *
     * @return
     */
    TstObject[] getTestObjects();

    /**
     * Method changeTestObjectNames
     * Helps ilustrate the bug http://developer.java.sun.com/developer/bugParade/bugs/4499841.html
     */
    void changeTestObjectNames();

    /**
     * Method makeNewTestObjectNames
     * Helps ilustrate the bug http://developer.java.sun.com/developer/bugParade/bugs/4499841.html
     */
    void makeNewTestObjectNames();

    void ping();

    /**
     * Return the value of the first byte multiplied by 2 and then add the values
     * of all the elements of byte array to it and return that value.
     *
     * @param b     the byte to multiply by 2
     * @param array the array to add the values of
     * @return the value of the first parameter multiplied by 2 plus the sum of the elements of the
     *         second parameter, the array
     */
    byte byteArrayParamReturningByte(byte b, byte[] array);

    void throwSpecialException(int i);

    void testLong(long l);

    /**
     * It seems that there is (or was) a bug that prevents
     * deserialization of instances where a classdef was
     * not in the primordial classloader. This will be (or was)
     * difficult to test under Maven control.
     *
     * @param param
     * @return
     */
    CustomSerializableParam testCustomSerializableParameter(CustomSerializableParam param);


}
