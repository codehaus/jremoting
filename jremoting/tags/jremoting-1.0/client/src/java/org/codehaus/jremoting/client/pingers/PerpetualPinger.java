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

import org.codehaus.jremoting.client.Transport;
import org.codehaus.jremoting.client.ConnectionPinger;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * PerpetualPinger pings forever.
 * You are going to have to stop() it yourself
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public class PerpetualPinger implements ConnectionPinger {

    private Runnable runnable;

    private Transport invoker;
    private ScheduledFuture pingingFuture;
    private int pingIntervalSeconds;

    public PerpetualPinger(int pingIntervalSeconds) {
        this.pingIntervalSeconds = pingIntervalSeconds;
        runnable = new Runnable() {
            public void run() {
                invoker.ping();
            }
        };
    }

    public PerpetualPinger() {
        this(10);
    }

    public void start(Transport invoker) {
        this.invoker = invoker;
        pingingFuture = invoker.getScheduledExecutorService()
                .scheduleAtFixedRate(runnable, pingIntervalSeconds, pingIntervalSeconds, TimeUnit.SECONDS);
    }

    public void stop() {
        pingingFuture.cancel(true);
    }
}
