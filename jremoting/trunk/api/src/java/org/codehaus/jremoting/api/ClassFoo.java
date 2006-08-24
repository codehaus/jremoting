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
package org.codehaus.jremoting.api;

public class ClassFoo {
    public static Object instantiate(String className) {
        try {
            Class clazz = Class.forName(className, true, ClassFoo.class.getClassLoader());
            return clazz.newInstance();
        } catch (ClassNotFoundException e) {
            throw new JRemotingRuntimeException("Class Not Found", e);
        } catch (InstantiationException e) {
            throw new JRemotingRuntimeException("InstantiationException", e);
        } catch (IllegalAccessException e) {
            throw new JRemotingRuntimeException("IllegalAccessException", e);
        }
    }
}
