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
package org.codehaus.jremoting.test.async;

import org.codehaus.jremoting.api.AsynchronousMethod;
import org.codehaus.jremoting.api.AsynchronousRollbackMethod;
import org.codehaus.jremoting.api.AsynchronousCommitMethod;
import org.codehaus.jremoting.api.AsynchronousFacade;

@AsynchronousFacade
public interface AsyncTest {

    @AsynchronousMethod
    void setOne(String one);

    @AsynchronousMethod
    void setTwo(String two);

    @AsynchronousMethod
    void setThree(String three);

    @AsynchronousCommitMethod
    void fire();

    @AsynchronousRollbackMethod
    void whoa();

}
