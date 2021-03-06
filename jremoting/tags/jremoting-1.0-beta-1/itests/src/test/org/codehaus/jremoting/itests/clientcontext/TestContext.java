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
package org.codehaus.jremoting.itests.clientcontext;

import org.codehaus.jremoting.client.Context;


/**
 * @author Paul Hammant and Rune Johanessen (pairing for part)
 * @version $Revision: 1.2 $
 */

public class TestContext implements Context {

    private String ctx;
    private static final long serialVersionUID = 1684374969959580640L;

    public TestContext() {
        ctx = "TestCCF:" + System.identityHashCode(Thread.currentThread());
    }

    public int hashCode() {
        return ctx.hashCode();
    }

    public String toString() {
        return ctx;
    }

    public boolean equals(Object obj) {
        return ctx.equals(((TestContext) obj).ctx);
    }

}
