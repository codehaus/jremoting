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

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import org.codehaus.jremoting.client.ClientInvoker;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.requests.Ping;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.ConnectionException;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;

public class PerpetualPingerTestCase extends MockObjectTestCase {

    public void testFoo() throws InterruptedException {
        final ScheduledExecutorService ses = Executors.newScheduledThreadPool(10);

        ClientInvoker ci = new ClientInvoker() {

            public Response invoke(Request request) {
                System.out.println("--> " + request);
                assertNotNull(((Ping) request).getSession());
                return new org.codehaus.jremoting.responses.Ping();
            }

            public void initialize() throws ConnectionException {
            }

            public void close() {
            }

            public long getLastRealRequestTime() {
                return 0;
            }

            public void ping() {
            }

            public ClassLoader getFacadesClassLoader() {
                return null;
            }

            public Object resolveArgument(String remoteObjName, String methodSignature, Class inputArgumentClass, Object inputArgumentInstance) {
                return inputArgumentInstance;
            }

            public ScheduledExecutorService getScheduledExecutorService() {
                return ses;
            }

            public ClientMonitor getClientMonitor() {
                return new ConsoleClientMonitor();
            }
        };

        PerpetualConnectionPinger pcp = new PerpetualConnectionPinger(1);
        pcp.setInvoker(ci);
        pcp.start();
        Thread.sleep(3500);
    }
}
