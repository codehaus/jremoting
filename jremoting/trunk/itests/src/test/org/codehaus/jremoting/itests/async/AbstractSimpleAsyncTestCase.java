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
package org.codehaus.jremoting.itests.async;

import org.codehaus.jremoting.client.ContextFactory;
import org.codehaus.jremoting.client.Factory;
import org.codehaus.jremoting.client.factories.StubsFromServer;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.transports.ByteStreamEncoding;
import org.codehaus.jremoting.client.transports.socket.SocketTransport;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.PublicationDescriptionItem;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.factories.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.stubretrievers.DynamicStubRetriever;
import org.codehaus.jremoting.server.transports.ServerByteStreamDriverFactory;
import org.codehaus.jremoting.server.transports.socket.SelfContainedSocketStreamServer;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class AbstractSimpleAsyncTestCase extends MockObjectTestCase {

    AsyncTestImpl asyncTestImpl;
    AsyncTest testClient;
    Factory factory;
    SelfContainedSocketStreamServer server;
    private Mock mockServerMonitor;

    protected abstract DynamicStubRetriever getAbstractDynamicGeneratorClassRetriever(ClassLoader cl);

    /**
     * Fetch the directory to store the classes and java source generated by the dynamic class retrievers
     */
    private String getClassGenDir() {
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        return path;
    }

    protected void setUp() throws Exception {
        super.setUp();

        mockServerMonitor = mock(ServerMonitor.class);

        // server side setup.
        DynamicStubRetriever stubRetriever = getAbstractDynamicGeneratorClassRetriever(this.getClass().getClassLoader());

        // Fetch the dir to place the generated classes from the System properties
        String class_gen_dir = getClassGenDir();

        stubRetriever.setClassGenDir(class_gen_dir);

        ThreadLocalServerContextFactory ccf = new ThreadLocalServerContextFactory();

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
        server = new SelfContainedSocketStreamServer((ServerMonitor) mockServerMonitor.proxy(), stubRetriever, new NullAuthenticator(),
                new ServerByteStreamDriverFactory(), executorService,
                ccf, 11003);

        asyncTestImpl = new AsyncTestImpl();
        PublicationDescription pd = new PublicationDescription();
        pd.addPrimaryFacade(new PublicationDescriptionItem(AsyncTest.class, new String[]{"setOne(java.lang.String)", "setTwo(java.lang.String)", "setThree(java.lang.String)", }, new String[]{"fire()"}, new String[]{"whoa()"}));
        stubRetriever.generate("AsyncTest", pd, this.getClass().getClassLoader());
        server.publish(asyncTestImpl, "AsyncTest", pd);
        server.start();

        // Client side setup
        Mock mock = mock(ContextFactory.class);
        mock.expects(atLeastOnce()).method("getClientContext").withNoArguments().will(returnValue(null));
        factory = new StubsFromServer(new SocketTransport(new ConsoleClientMonitor(),
                new ByteStreamEncoding(),"127.0.0.1", 11003), (ContextFactory) mock.proxy());
        testClient = (AsyncTest) factory.lookupService("AsyncTest");

    }

    public void testSimpleAsync() throws Exception {

        testClient.setOne("one");
        testClient.setTwo("two");
        testClient.setThree("three");

        assertNull("Field 'One' should be null", asyncTestImpl.one);
        assertNull("Field 'Two' should be null", asyncTestImpl.two);
        assertNull("Field 'Tree' should be null", asyncTestImpl.three);
        assertFalse("Field 'Fire' should be false", asyncTestImpl.fired);

        testClient.fire();

        assertNotNull("Field 'One' should not be null", asyncTestImpl.one);
        assertNotNull("Field 'Two' should not be null", asyncTestImpl.two);
        assertNotNull("Field 'Tree' should not be null", asyncTestImpl.three);
        assertTrue("Field 'Fire' should not be false", asyncTestImpl.fired);

    }

    public void testRollback() throws Exception {

        testClient.setOne("111");

        assertNull("Field 'One' should be null #1", asyncTestImpl.one);

        testClient.whoa();
        testClient.fire();

        assertNull("Field 'One' should be null #2", asyncTestImpl.one);
        assertTrue("Field 'Whoa' should not be false", asyncTestImpl.whoa);
        assertTrue("Field 'Fire' should not be false", asyncTestImpl.fired);

        testClient.setOne("222");
        testClient.fire();

        assertNotNull("Field 'One' should not be null", asyncTestImpl.one);

    }

    protected void tearDown() throws Exception {
        testClient = null;
        System.gc();
        Thread.sleep(300);
        factory.close();
        server.stop();
    }


}
