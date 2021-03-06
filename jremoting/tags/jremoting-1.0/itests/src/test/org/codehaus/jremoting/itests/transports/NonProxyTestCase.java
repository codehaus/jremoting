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
package org.codehaus.jremoting.itests.transports;

import org.codehaus.jremoting.itests.TestFacadeImpl;

/**
 * Test Non Stub for comparison sake
 *
 * @author Paul Hammant
 */
public class NonProxyTestCase extends AbstractHelloTestCase {

    protected void setUp() throws Exception {

        testServer = new TestFacadeImpl();
        testClient = testServer;

    }

    protected int getNumIterationsForSpeedTest() {
        return super.getNumIterationsForSpeedTest() * 1000000;
    }    

    protected void tearDown() throws Exception {
    }
}
