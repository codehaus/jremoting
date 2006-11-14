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
import org.codehaus.jremoting.itests.TestInterface;
import org.codehaus.jremoting.itests.TestInterface2;
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
public class CodedProxyTestInterfaceProxy implements TestInterface {

    private TestInterface actualImpl;

    /**
     * Constructor CodedProxyTestInterfaceProxy
     *
     * @param actualImpl
     */
    public CodedProxyTestInterfaceProxy(TestInterface actualImpl) {
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
     * Method hello2
     *
     * @param greeting
     */
    public int hello2(int greeting) {
        return actualImpl.hello2(greeting);
    }

    /**
     * Method hello3
     *
     * @param greeting
     * @return
     * @throws IOException
     * @throws PropertyVetoException
     */
    public boolean hello3(short greeting) throws PropertyVetoException, IOException {
        return actualImpl.hello3(greeting);
    }

    /**
     * Method hello4
     *
     * @param greeting1
     * @param greeting2
     * @return
     */
    public StringBuffer hello4(float greeting1, double greeting2) {
        return actualImpl.hello4(greeting1, greeting2);
    }

    /**
     * Method testSpeed
     */
    public void testSpeed() {
        actualImpl.testSpeed();
    }

    /**
     * Method makeTestInterface2
     *
     * @param thingName
     * @return
     */
    public TestInterface2 makeTestInterface2(String thingName) {
        return actualImpl.makeTestInterface2(thingName);
    }

    /**
     * Method morphName
     *
     * @param forThisImpl
     */
    public void morphName(TestInterface2 forThisImpl) {
        actualImpl.morphName(forThisImpl);
    }

    /**
     * Method findTestInterface2ByName
     *
     * @param nameToFind
     * @return
     */
    public TestInterface2 findTestInterface2ByName(String nameToFind) {
        return actualImpl.findTestInterface2ByName(nameToFind);
    }

    /**
     * Method getTestInterface2s
     *
     * @return
     */
    public TestInterface2[] getTestInterface2s() {
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

    public byte bytes(byte b, byte[] array) {
        return actualImpl.bytes(b, array);
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
