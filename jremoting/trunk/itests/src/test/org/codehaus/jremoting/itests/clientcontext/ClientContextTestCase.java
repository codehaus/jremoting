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
package org.codehaus.jremoting.itests.clientcontext;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.Context;
import org.codehaus.jremoting.client.Transport;
import org.codehaus.jremoting.client.factories.JRemotingClient;
import org.codehaus.jremoting.client.context.ThreadLocalContextFactory;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.encoders.StreamEncoding;
import org.codehaus.jremoting.client.transports.socket.SocketTransport;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.context.ServerContextFactory;
import org.codehaus.jremoting.server.context.ServerSideContext;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.stubretrievers.BcelDynamicStubRetriever;
import org.codehaus.jremoting.server.encoders.ByteStreamEncoding;
import org.codehaus.jremoting.server.transports.socket.SocketStreamServer;
import org.jmock.MockObjectTestCase;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Paul Hammant and Rune Johanessen (pairing for part)
 * @version $Revision: 1.2 $
 */

public class ClientContextTestCase extends MockObjectTestCase {

    public void testSimpleMathsWorks() {

        AccountListener nullAccountListener = new AccountListener() {
            public void record(String event, Context context) {
            }
        };

        ServerContextFactory contextFactory = new ThreadLocalServerContextFactory();

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

        ServerContextFactory contextFactory = new ServerContextFactory() {
            public ServerSideContext get() {
                return new ServerSideContext(new Long(123), new TestContext());
            }

            public void set(ServerSideContext context) {
            }

            public boolean isSet() {
                return false;
            }
        };

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

        assertNotNull("Debit should have been registered on server side", debit11Context);
        assertNotNull("Credit should have been registered on server side", credit11Context);
        assertEquals("Debit Context and Credit Context should be .equals()", debit11Context, credit11Context);

        Context debit22Context = contextualEvents.get("fredsAccount:debited:22");
        Context credit22Context = contextualEvents.get("wilmasAccount:credited:22");

        assertNotNull("Debit should have been registered on server side", debit22Context);
        assertNotNull("Credit should have been registered on server side", credit22Context);
        assertEquals("Debit Context and Credit Context should be .equals()", debit22Context, credit22Context);

        assertFalse(debit11Context == credit22Context);

    }

    public void testWithCustomContextWithRPC() throws InterruptedException, PublicationException, ConnectionException {

        final HashMap<String, Context> contextualEvents = new HashMap<String, Context>();

        AccountListener al = new AccountListener() {
            public void record(String event, Context context) {
                contextualEvents.put(event, context);
            }
        };

        ServerContextFactory sscf = new ThreadLocalServerContextFactory();

        Account fredsAccount = new TalkativeAccountStartingWith123Dollars(sscf, "fredsAccount", al);
        Account wilmasAccount = new TalkativeAccountStartingWith123Dollars(sscf, "wilmasAccount", al);

        final AccountManager accountManager = new AccountManagerImpl(sscf, fredsAccount, wilmasAccount);

        BcelDynamicStubRetriever stubRetriever = new BcelDynamicStubRetriever(this.getClass().getClassLoader());

        ServerMonitor serverMonitor = new ConsoleServerMonitor();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
        SocketStreamServer server = new SocketStreamServer(serverMonitor, stubRetriever, new NullAuthenticator(),
                new ByteStreamEncoding(), executorService, sscf, 19333);

        Publication pd = new Publication(AccountManager.class);
        server.publish(accountManager, "OurAccountManager", pd);
        server.start();

        StreamEncoding factory0 = new org.codehaus.jremoting.client.encoders.ByteStreamEncoding();
        ClientMonitor cm = new ConsoleClientMonitor();
        Transport handler = new SocketTransport(cm, factory0, "127.0.0.1", 19333);
        ThreadLocalContextFactory factory1 = new ThreadLocalContextFactory();
        JRemotingClient jc = new JRemotingClient(handler, factory1);

        final AccountManager clientSideAccountManager = (AccountManager) jc.lookupService("OurAccountManager");

        Thread threadOne = makeAmountTransferringThread(clientSideAccountManager, "fredsAccount", "wilmasAccount", 11);
        Thread threadTwo = makeAmountTransferringThread(clientSideAccountManager, "fredsAccount", "wilmasAccount", 22);
        threadOne.start();
        threadTwo.start();

        Thread.sleep(1000);

        Context debit11 = contextualEvents.get("fredsAccount:debited:11");
        Context credit11 = contextualEvents.get("wilmasAccount:credited:11");


        assertNotNull("Debit should have been registered on server side", debit11);
        assertNotNull("Credit should have been registered on server side", credit11);
        assertEquals("Debit Context and Credit Context should be .equals()", debit11, credit11);

        assertTrue("Wrong type of Context", credit11 instanceof ServerSideContext);

        Context debit22 = contextualEvents.get("fredsAccount:debited:22");
        Context credit22 = contextualEvents.get("wilmasAccount:credited:22");

        assertNotNull("Debit should have been registered on server side", debit22);
        assertNotNull("Credit should have been registered on server side", credit22);
        assertEquals("Debit Context and Credit Context should be .equals()", debit22, credit22);

        assertFalse(debit11 == credit22);


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
