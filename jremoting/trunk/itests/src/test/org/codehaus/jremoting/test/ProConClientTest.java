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

import org.codehaus.jremoting.client.Factory;
import org.codehaus.jremoting.client.HostContext;
import org.codehaus.jremoting.client.factories.ClientSideStubFactory;
import org.codehaus.jremoting.client.factories.ServerSideStubFactory;
import org.codehaus.jremoting.client.transports.socket.SocketCustomStreamHostContext;
import org.codehaus.jremoting.client.transports.socket.SocketObjectStreamHostContext;

/**
 * Class ProConClientTest
 *
 * @author Vinay Chandrasekharan
 */
public class ProConClientTest {

    /**
     * Method main
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        System.out.println("Stream over Socket Client");

        HostContext arhc;

        if (args[1].equals("ObjectStream")) {
            System.out.println("(Object Stream)");

            arhc = new SocketObjectStreamHostContext("127.0.0.1", 1234);
        } else {
            System.out.println("(Custom Stream)");

            arhc = new SocketCustomStreamHostContext("127.0.0.1", 1235);
        }

        Factory af = null;

        if (args[0].equals("S")) {
            af = new ServerSideStubFactory(arhc, false);
        } else {
            af = new ClientSideStubFactory(arhc, false);
        }

        //list
        System.out.println("Listing Published Objects At Server...");

        String[] listOfPublishedObjectsOnServer = af.listServices();

        for (int i = 0; i < listOfPublishedObjectsOnServer.length; i++) {
            System.out.println("..[" + i + "]:" + listOfPublishedObjectsOnServer[i]);
        }

        TestProvider tpi = (TestProvider) af.lookupServices("P");
        TestConsumer tci = (TestConsumer) af.lookupServices("C");

        System.out.println("Provider.getName(0)" + tpi.getName(0));
        System.out.println("Consumer.getProviderName(0)" + tci.getProviderName(tpi));
        af.close();
    }
}
