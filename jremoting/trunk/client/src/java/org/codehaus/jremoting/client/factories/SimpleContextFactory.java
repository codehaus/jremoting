/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
 * Portions copyright 2001 - 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.codehaus.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.jremoting.client.factories;

import org.codehaus.jremoting.client.Context;
import org.codehaus.jremoting.client.ContextFactory;
import org.codehaus.jremoting.client.SimpleContext;

/**
 * @author Paul Hammant and Rune Johanessen (pairing for part)
 * @version $Revision: 1.2 $
 */

public class SimpleContextFactory implements ContextFactory {

    // The next serial number to be assigned
    private static long nextSerialNum = 1;

    private static ThreadLocal serialContext = new ThreadLocal() {
        protected synchronized Object initialValue() {
            return new SimpleContext(nextSerialNum++);
        }
    };

    public static Context get() {
        return (Context) (serialContext.get());
    }


    public Context getClientContext() {
        return get();
    }

}
