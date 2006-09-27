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
package org.codehaus.jremoting.util;

/**
 * Helper methods for generation of stub and proxy names
 * 
 * @author Mauro Talevi
 */
public class StubHelper {

    private static final String STUB_PREFIX = "JRemotingGenerated";
    private static final String STUB_POSTFIX = "Main";
    
    public static String formatProxyClassName(String generatedName) {
        return formatProxyClassName(generatedName, STUB_POSTFIX);
    }

    public static String formatProxyClassName(String generatedName, String encodedClassName) {
        return STUB_PREFIX + generatedName + "_" + encodedClassName;
    }

    public static String formatStubClassName(String publishedServiceName) {
        return STUB_PREFIX + publishedServiceName;
    }

    public static String formatStubClassName(String publishedServiceName, String objectName) {
        return STUB_PREFIX + publishedServiceName + "_" + objectName;
    }
    
    public static String formatServiceName(String service) {
        return formatServiceName(service, STUB_POSTFIX);
    }

    public static String formatServiceName(String service, String className) {
        return service + "_" + className;
    }

    public static boolean isService(String item) {
        return item.endsWith("_" + STUB_POSTFIX);
    }

    public static String getServiceName(String item) {
        return item.substring(0, item.lastIndexOf("_" + STUB_POSTFIX));
    }

    public static int getStubPrefixLength() {
        return STUB_PREFIX.length();
    }

}
