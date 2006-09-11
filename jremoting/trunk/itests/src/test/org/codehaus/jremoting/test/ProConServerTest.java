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
package org.codehaus.jremoting.test;

import org.codehaus.jremoting.api.DefaultThreadPool;
import org.codehaus.jremoting.server.authenticators.DefaultAuthenticator;
import org.codehaus.jremoting.server.classretrievers.JarFileStubRetriever;
import org.codehaus.jremoting.server.monitors.NullServerMonitor;
import org.codehaus.jremoting.server.transports.AbstractServer;
import org.codehaus.jremoting.server.transports.DefaultServerSideClientContextFactory;
import org.codehaus.jremoting.server.transports.socket.SelfContainedSocketCustomStreamServer;
import org.codehaus.jremoting.server.transports.socket.SelfContainedSocketObjectStreamServer;

/**
 * Class ProConServerTest
 *
 * @author Vinay Chandrasekharan
 */
public class ProConServerTest {

    /**
     * Method main
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        System.out.println("Stream over Socket Server");

        AbstractServer as;

        if (args[1].equals("ObjectStream")) {
            System.out.println("(Object Stream)");

            as = new SelfContainedSocketObjectStreamServer(1234);
        } else {

            // CustomStream
            System.out.println("(Custom Stream)");

            as = new SelfContainedSocketCustomStreamServer(1235);
        }

        if (args[0].equals("S")) {
            as = new SelfContainedSocketCustomStreamServer(new JarFileStubRetriever("build/classes2"), new DefaultAuthenticator(), new NullServerMonitor(), new DefaultThreadPool(), new DefaultServerSideClientContextFactory(), 1235);
        }

        //provider
        TestProvider tpi = new TestProviderImpl();

        as.publish(tpi, "P", TestProvider.class);

        //consumer
        TestConsumer tci = new TestConsumerImpl();

        as.publish(tci, "C", TestConsumer.class);

        //start
        as.start();
    }
}
