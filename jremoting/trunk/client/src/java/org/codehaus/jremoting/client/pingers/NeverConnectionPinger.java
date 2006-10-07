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
package org.codehaus.jremoting.client.pingers;

import org.codehaus.jremoting.client.ClientInvoker;
import org.codehaus.jremoting.client.ConnectionPinger;

/**
 * Interface NeverConnectionPinger
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public class NeverConnectionPinger implements ConnectionPinger {

    /**
     * Implemnt the abstract ping() from the IntervalConnectionPinger
     */
    protected void ping() {
    }

    public void setInvoker(ClientInvoker invoker) {
    }

    public void start() {
    }

    public void stop() {
    }
}
