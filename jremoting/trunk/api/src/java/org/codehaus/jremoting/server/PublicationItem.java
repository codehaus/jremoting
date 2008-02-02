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
package org.codehaus.jremoting.server;

import org.codehaus.jremoting.annotations.Asynchronous;
import org.codehaus.jremoting.annotations.Commit;
import org.codehaus.jremoting.annotations.Rollback;
import org.codehaus.jremoting.util.MethodNameHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Class PublicationItem
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public class PublicationItem {

    private final Class facadeClass;
    private final List<String> asyncMethods = new ArrayList<String>();
    private final List<String> commitMethods = new ArrayList<String>();
    private final List<String> rollbackMethods = new ArrayList<String>();

    public PublicationItem(Class facade) {
        this.facadeClass = facade;
        Method[] methods = facade.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String sig = MethodNameHelper.getMethodSignature(method);
            if (method.isAnnotationPresent(Asynchronous.class)) {
                ensureReturnTypeIsVoid(method);
                asyncMethods.add(sig);
            }
            if (method.isAnnotationPresent(Commit.class)) {
                ensureReturnTypeIsVoid(method);
                commitMethods.add(sig);
            }
            if (method.isAnnotationPresent(Rollback.class)) {
                ensureReturnTypeIsVoid(method);
                rollbackMethods.add(sig);
            }
        }
    }

    private void ensureReturnTypeIsVoid(Method method) {
        if (!method.getReturnType().getName().equals("void")) {
            throw new PublicationException("Only 'void' returning methods are eligible as asynchronous/commit/rollback methods.");
        }
    }

    public Class getFacadeClass() {
        return facadeClass;
    }

    public boolean isCommit(Method method) {
        return isMethodInList(method, commitMethods);
    }

    public boolean isRollback(Method method) {
        return isMethodInList(method, rollbackMethods);
    }

    public boolean isAsync(Method method) {
        return isMethodInList(method, asyncMethods);
    }

    public boolean isMethodInList(Method method, List<String> methods) {
        String mthSig = MethodNameHelper.getMethodSignature(method);
        for (int i = 0; i < methods.size(); i++) {
            String asyncMethod = methods.get(i);
            if (asyncMethod.equals(mthSig)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasAsyncBehavior() {
        return (asyncMethods.size() != 0 | commitMethods.size() != 0 | rollbackMethods.size() != 0);
    }

}
