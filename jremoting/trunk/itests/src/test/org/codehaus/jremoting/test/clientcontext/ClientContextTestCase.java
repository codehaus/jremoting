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
package org.codehaus.jremoting.test.clientcontext;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.TestCase;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.ClientContext;
import org.codehaus.jremoting.client.Factory;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.factories.ClientSideStubFactory;
import org.codehaus.jremoting.client.transports.socket.SocketCustomStreamHostContext;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.classretrievers.BcelDynamicGeneratorStubRetriever;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.transports.DefaultServerSideClientContext;
import org.codehaus.jremoting.server.transports.DefaultServerSideClientContextFactory;
import org.codehaus.jremoting.server.transports.ServerCustomStreamDriverFactory;
import org.codehaus.jremoting.server.transports.socket.SelfContainedSocketStreamServer;

/**
 * @author Paul Hammant and Rune Johanessen (pairing for part)
 * @version $Revision: 1.2 $
 */

public class ClientContextTestCase extends TestCase {


    public void testSimple() {

        AccountListener al = new AccountListener() {
            public void record(String event, ClientContext clientContext) {
            }
        };

        ServerSideClientContextFactory clientContextFactory = new DefaultServerSideClientContextFactory();

        AccountImpl one = new AccountImpl(clientContextFactory, "one", al);
        AccountImpl two = new AccountImpl(clientContextFactory, "two", al);

        final AccountManager accountManager = new AccountManagerImpl(clientContextFactory, one, two);

        try {
            accountManager.transferAmount("one", "two", 23);
        } catch (TransferBarfed transferBarfed) {
            fail("Transfer should have worked");
        }

        assertEquals(one.getBalance(), 100);
        assertEquals(two.getBalance(), 146);

    }

    public void testWithCustomContextWithoutRPC() throws InterruptedException {

        final HashMap hashMap = new HashMap();

        AccountListener al = makeAccountListener(hashMap);

        ServerSideClientContextFactory clientContextFactory = new TestClientContextFactory();

        AccountImpl one = new AccountImpl(clientContextFactory, "one", al);
        AccountImpl two = new AccountImpl(clientContextFactory, "two", al);

        final AccountManager accountManager = new AccountManagerImpl(clientContextFactory, one, two);

        Thread threadOne = makeThread(accountManager, 11);
        Thread threadTwo = makeThread(accountManager, 22);
        threadOne.start();
        threadTwo.start();

        Thread.sleep(2000);

        ClientContext debit11 = (ClientContext) hashMap.get("one:debit:11");
        ClientContext credit11 = (ClientContext) hashMap.get("two:credit:11");

        basicAsserts(debit11, credit11);

        ClientContext debit22 = (ClientContext) hashMap.get("one:debit:22");
        ClientContext credit22 = (ClientContext) hashMap.get("two:credit:22");

        basicAsserts(debit22, credit22);

        assertFalse(debit11 == credit22);

    }

    public void testWithCustomContextWithRPC() throws InterruptedException, PublicationException, ConnectionException {

        final HashMap hashMap = new HashMap();

        AccountListener al = makeAccountListener(hashMap);

        ServerSideClientContextFactory ccf = new DefaultServerSideClientContextFactory();

        AccountImpl one = new AccountImpl(ccf, "one", al);
        AccountImpl two = new AccountImpl(ccf, "two", al);

        final AccountManager accountManager = new AccountManagerImpl(ccf, one, two);

        BcelDynamicGeneratorStubRetriever stubRetriever = new BcelDynamicGeneratorStubRetriever(this.getClass().getClassLoader());

        ServerMonitor serverMonitor = new ConsoleServerMonitor();
        ExecutorService executorService = Executors.newCachedThreadPool();
        SelfContainedSocketStreamServer server = new SelfContainedSocketStreamServer(serverMonitor, stubRetriever, new NullAuthenticator(),
                new ServerCustomStreamDriverFactory(), executorService,
                ccf, 13333);

        PublicationDescription pd = new PublicationDescription(AccountManager.class);
        server.publish(accountManager, "OurAccountManager", pd);
        server.start();

        Factory factory = new ClientSideStubFactory(new SocketCustomStreamHostContext(new ConsoleClientMonitor(), "127.0.0.1", 13333));
        final AccountManager clientSideAccountManager = (AccountManager) factory.lookupService("OurAccountManager");

        Thread threadOne = makeThread(clientSideAccountManager, 11);
        Thread threadTwo = makeThread(clientSideAccountManager, 22);
        threadOne.start();
        threadTwo.start();

        Thread.sleep(2000);

        ClientContext debit11 = (ClientContext) hashMap.get("one:debit:11");
        ClientContext credit11 = (ClientContext) hashMap.get("two:credit:11");

        basicAsserts(debit11, credit11);

        assertTrue("Wrong type of ClientContext", credit11 instanceof DefaultServerSideClientContext);

        ClientContext debit22 = (ClientContext) hashMap.get("one:debit:22");
        ClientContext credit22 = (ClientContext) hashMap.get("two:credit:22");

        basicAsserts(debit22, credit22);

        assertFalse(debit11 == credit22);


    }

    private void basicAsserts(ClientContext debitContext, ClientContext creditContext) {
        assertNotNull("Debit should have been registered on server side", debitContext);
        assertNotNull("Credit should have been registered on server side", creditContext);
        assertEquals("Debit Context and Credit Context should be .equals()", debitContext, creditContext);
    }

    private AccountListener makeAccountListener(final HashMap hashMap) {
        AccountListener al = new AccountListener() {
            public void record(String event, ClientContext clientContext) {
                //System.out.println("EVENT-" + event + " " + clientContext);
                hashMap.put(event, clientContext);
            }
        };
        return al;
    }

    private Thread makeThread(final AccountManager accountManager, final int amount) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    accountManager.transferAmount("one", "two", amount);
                } catch (TransferBarfed transferBarfed) {
                    fail("Transfer should have worked");
                }
            }
        });
        return thread;
    }

}
