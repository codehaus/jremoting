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

import org.codehaus.jremoting.requests.Ping;

/**
 * PerpetualConnectionPinger pings forever.
 * You are going to have to stop() it yourself
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public class PerpetualConnectionPinger extends IntervalConnectionPinger {

    public PerpetualConnectionPinger(int pingIntervalSeconds) {
        super(pingIntervalSeconds);
    }

    /**
     * Ping every 10 seconds
     */
    public PerpetualConnectionPinger() {
        this(10);
    }

    protected void ping() {
        getInvocationHandler().invoke(new Ping());
    }
}
