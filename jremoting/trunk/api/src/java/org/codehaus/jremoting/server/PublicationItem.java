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
import java.util.Vector;

/**
 * Class PublicationItem
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public class PublicationItem {

    private final Class facadeClass;
    private final Vector<String> asyncMethods = new Vector<String>();
    private final Vector<String> commitMethods = new Vector<String>();
    private final Vector<String> rollbackMethods = new Vector<String>();

    public PublicationItem(Class facade) {
        this.facadeClass = facade;
        Method[] methods = facade.getDeclaredMethods();
        try {
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.isAnnotationPresent(Asynchronous.class)) {
                    checkReturnType(method);
                    asyncMethods.add(MethodNameHelper.getMethodSignature(method));
                }
                if (method.isAnnotationPresent(Commit.class)) {
                    checkReturnType(method);
                    commitMethods.add(MethodNameHelper.getMethodSignature(method));
                }
                if (method.isAnnotationPresent(Rollback.class)) {
                    checkReturnType(method);
                    rollbackMethods.add(MethodNameHelper.getMethodSignature(method));
                }
            }
        } catch (NoClassDefFoundError ncdfe) {
            // TODO a soft check for Atributes / CommonsLogger missing?
            //System.out.println("--> ncdfe " + ncdfe.getMessage());
            //ncdfe.printStackTrace();
            // attribute jars are missing.
            // This allowed for when there is no Async functionality.
        } catch (RuntimeException re) {
            if (!re.getClass().getName().equals("org.apache.commons.attributes.AttributesException")) {
                throw re;
            }
        }
    }

    private void checkReturnType(Method method) {
        if (!method.getReturnType().getName().equals("void")) {
            throw new PublicationException("Only 'void' returning methods are eligible as asynchronous/commit/rollback methods.");
        }
    }

    public Class getFacadeClass() {
        return facadeClass;
    }

    public boolean isCommit(Method method) {
        String mthSig = MethodNameHelper.getMethodSignature(method);
        for (int i = 0; i < commitMethods.size(); i++) {
            String commitMethod = commitMethods.elementAt(i);
            if (commitMethod.equals(mthSig)) {
                return true;
            }
        }
        return false;
    }

    public boolean isRollback(Method method) {
        String mthSig = MethodNameHelper.getMethodSignature(method);
        for (int i = 0; i < rollbackMethods.size(); i++) {
            String rollbackMethod = rollbackMethods.elementAt(i);
            if (rollbackMethod.equals(mthSig)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAsync(Method method) {
        String mthSig = MethodNameHelper.getMethodSignature(method);
        for (int i = 0; i < asyncMethods.size(); i++) {
            String asyncMethod = asyncMethods.elementAt(i);
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
