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

/**
 * Interface TimingOutConnectionPinger
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public class TimingOutConnectionPinger extends IntervalConnectionPinger {

    private final long giveUpIntervalMilliSeconds;

    public TimingOutConnectionPinger(int pingIntervalSeconds, int giveUpIntervalSeconds) {
        super(pingIntervalSeconds);
        this.giveUpIntervalMilliSeconds = giveUpIntervalSeconds;
    }

    /**
     * Ten seconds between pings
     * Stop trying 100 seconds after last real request
     */
    public TimingOutConnectionPinger() {
        this(10, 100);
    }

    protected void ping() {

        long shouldHaveGivenUpBy = System.currentTimeMillis() - giveUpIntervalMilliSeconds;
        if (getInvoker().getLastRealRequestTime() > shouldHaveGivenUpBy) {
            getInvoker().ping();
        } else {
            //TODO should be restartable after reconnect of socket.
            stop();
        }
    }
}
