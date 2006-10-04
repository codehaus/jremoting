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
import org.codehaus.jremoting.client.Context;
import org.codehaus.jremoting.client.Factory;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.factories.ClientSideStubFactory;
import org.codehaus.jremoting.client.factories.SimpleContextFactory;
import org.codehaus.jremoting.client.transports.socket.SocketClientStreamInvocationHandler;
import org.codehaus.jremoting.client.transports.ClientCustomStreamDriverFactory;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideContextFactory;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.stubretrievers.BcelDynamicStubRetriever;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.transports.DefaultServerSideContext;
import org.codehaus.jremoting.server.transports.DefaultServerSideContextFactory;
import org.codehaus.jremoting.server.transports.ServerCustomStreamDriverFactory;
import org.codehaus.jremoting.server.transports.socket.SelfContainedSocketStreamServer;

/**
 * @author Paul Hammant and Rune Johanessen (pairing for part)
 * @version $Revision: 1.2 $
 */

public class ClientContextTestCase extends TestCase {

    public void testSimpleMathsWorks() {

        AccountListener nullAccountListener = new AccountListener() {
            public void record(String event, Context context) {
            }
        };

        ServerSideContextFactory contextFactory = new DefaultServerSideContextFactory();

        Account fredsAccount = new TalkativeAccountStartingWith123Dollars(contextFactory, "fredsAccount", nullAccountListener);
        Account wilmasAccount = new TalkativeAccountStartingWith123Dollars(contextFactory, "wilmasAccount", nullAccountListener);

        final AccountManager accountManager = new AccountManagerImpl(contextFactory, fredsAccount, wilmasAccount);

        try {
            accountManager.transferAmount("fredsAccount", "wilmasAccount", 23);
        } catch (TransferBarfed transferBarfed) {
            fail("Transfer should have worked");
        }

        assertEquals(fredsAccount.getBalance(), 100);
        assertEquals(wilmasAccount.getBalance(), 146);

    }

    public void testWithCustomContextWithoutRPC() throws InterruptedException {

        final HashMap<String, Context> contextualEvents = new HashMap<String, Context>();

        AccountListener al = new AccountListener() {
            public void record(String event, Context context) {
                contextualEvents.put(event, context);
            }
        };

        ServerSideContextFactory contextFactory = new TestContextFactory();

        Account fredsAccount = new TalkativeAccountStartingWith123Dollars(contextFactory, "fredsAccount", al);
        Account wilmasAccount = new TalkativeAccountStartingWith123Dollars(contextFactory, "wilmasAccount", al);

        final AccountManager accountManager = new AccountManagerImpl(contextFactory, fredsAccount, wilmasAccount);

        Thread threadOne = makeAmountTransferringThread(accountManager, "fredsAccount", "wilmasAccount", 11);
        Thread threadTwo = makeAmountTransferringThread(accountManager, "fredsAccount", "wilmasAccount", 22);
        threadOne.start();
        threadTwo.start();

        Thread.sleep(2000);

        Context debit11Context = contextualEvents.get("fredsAccount:debited:11");
        Context credit11Context = contextualEvents.get("wilmasAccount:credited:11");

        basicAsserts(debit11Context, credit11Context);

        Context debit22Context = contextualEvents.get("fredsAccount:debited:22");
        Context credit22Context = contextualEvents.get("wilmasAccount:credited:22");

        basicAsserts(debit22Context, credit22Context);

        assertFalse(debit11Context == credit22Context);

    }

    public void testWithCustomContextWithRPC() throws InterruptedException, PublicationException, ConnectionException {

        final HashMap<String, Context> contextualEvents = new HashMap<String, Context>();

        AccountListener al = new AccountListener() {
            public void record(String event, Context context) {
                ((HashMap<String, Context>) contextualEvents).put(event, context);
            }
        };

        ServerSideContextFactory sscf = new DefaultServerSideContextFactory();

        Account fredsAccount = new TalkativeAccountStartingWith123Dollars(sscf, "fredsAccount", al);
        Account wilmasAccount = new TalkativeAccountStartingWith123Dollars(sscf, "wilmasAccount", al);

        final AccountManager accountManager = new AccountManagerImpl(sscf, fredsAccount, wilmasAccount);

        BcelDynamicStubRetriever stubRetriever = new BcelDynamicStubRetriever(this.getClass().getClassLoader());

        ServerMonitor serverMonitor = new ConsoleServerMonitor();
        ExecutorService executorService = Executors.newCachedThreadPool();
        SelfContainedSocketStreamServer server = new SelfContainedSocketStreamServer(serverMonitor, stubRetriever, new NullAuthenticator(),
                new ServerCustomStreamDriverFactory(), executorService, sscf, 13333);

        PublicationDescription pd = new PublicationDescription(AccountManager.class);
        server.publish(accountManager, "OurAccountManager", pd);
        server.start();

        Factory factory = new ClientSideStubFactory(
                new SocketClientStreamInvocationHandler(new ConsoleClientMonitor(), new ClientCustomStreamDriverFactory(), "127.0.0.1", 13333),
                new SimpleContextFactory());

        final AccountManager clientSideAccountManager = (AccountManager) factory.lookupService("OurAccountManager");

        Thread threadOne = makeAmountTransferringThread(clientSideAccountManager, "fredsAccount", "wilmasAccount", 11);
        Thread threadTwo = makeAmountTransferringThread(clientSideAccountManager, "fredsAccount", "wilmasAccount", 22);
        threadOne.start();
        threadTwo.start();

        Thread.sleep(1000);

        Context debit11 = (Context) contextualEvents.get("fredsAccount:debited:11");
        Context credit11 = (Context) contextualEvents.get("wilmasAccount:credited:11");

        basicAsserts(debit11, credit11);

        assertTrue("Wrong type of Context", credit11 instanceof DefaultServerSideContext);

        Context debit22 = (Context) contextualEvents.get("fredsAccount:debited:22");
        Context credit22 = (Context) contextualEvents.get("wilmasAccount:credited:22");

        basicAsserts(debit22, credit22);

        assertFalse(debit11 == credit22);


    }

    private void basicAsserts(Context debitContext, Context creditContext) {
        assertNotNull("Debit should have been registered on server side", debitContext);
        assertNotNull("Credit should have been registered on server side", creditContext);
        assertEquals("Debit Context and Credit Context should be .equals()", debitContext, creditContext);
    }

    private Thread makeAmountTransferringThread(final AccountManager accountManager,
                                                final String from, final String to, final int amount) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    accountManager.transferAmount(from, to, amount);
                } catch (TransferBarfed transferBarfed) {
                    fail("Transfer should have worked");
                }
            }
        });
        return thread;
    }

}
