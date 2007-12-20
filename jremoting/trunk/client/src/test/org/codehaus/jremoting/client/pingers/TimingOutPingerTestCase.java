/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
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
package org.codehaus.jremoting.client.pingers;

import org.codehaus.jremoting.client.ClientInvoker;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TimingOutPingerTestCase extends MockObjectTestCase {

    public void testThatPinderSchedulesAndInvokesPingOncePerRun() throws InterruptedException {

        Mock ci = mock(ClientInvoker.class);
        Mock ses = mock(ScheduledExecutorService.class);
        Mock future = mock(ScheduledFuture.class);
        ci.expects(once()).method("getScheduledExecutorService").withNoArguments().will(returnValue(ses.proxy()));
        long lastRequestTime = System.currentTimeMillis() - 10;

        final Runnable[] runnable = new Runnable[1];

        ses.expects(once()).method("scheduleAtFixedRate").with(new Constraint() {
            public boolean eval(Object o) {
                runnable[0] = (Runnable) o;
                return true;
            }

            public StringBuffer describeTo(StringBuffer stringBuffer) {
                return stringBuffer.append("an instance of Runnable");
            }
        }, eq(1L), eq(1L), eq(TimeUnit.SECONDS)).will(returnValue(future.proxy()));

        TimingOutPinger pcp = new TimingOutPinger(1,2);
        pcp.start((ClientInvoker) ci.proxy());

        ci.expects(exactly(3)).method("ping").withNoArguments();
        ci.expects(once()).method("getLastRealRequestTime").withNoArguments().will(returnValue(lastRequestTime++));

        runnable[0].run();
        ci.expects(once()).method("getLastRealRequestTime").withNoArguments().will(returnValue(lastRequestTime++));
        runnable[0].run();
        ci.expects(once()).method("getLastRealRequestTime").withNoArguments().will(returnValue(lastRequestTime++));
        runnable[0].run();

        ci.expects(once()).method("ping").withNoArguments().will(throwException(new RuntimeException("forced failure")));
        ci.expects(once()).method("getLastRealRequestTime").withNoArguments().will(returnValue(lastRequestTime++));

        try {
            runnable[0].run();
        } catch (RuntimeException e) {
            assertEquals("forced failure", e.getMessage());
        }

        Thread.sleep(2010);

        ci.expects(once()).method("getLastRealRequestTime").withNoArguments().will(returnValue(lastRequestTime++));
        future.expects(once()).method("cancel").with(eq(true)).will(returnValue(true));

        runnable[0].run();


    }
}