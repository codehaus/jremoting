/* ====================================================================
 * Copyright 2005 JRemoting Committers
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
package org.codehaus.jremoting.test.socket;

import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.transports.socket.CompleteSocketCustomStreamServer;
import org.codehaus.jremoting.test.AbstractHelloTestCase;
import org.codehaus.jremoting.test.TestInterface;
import org.codehaus.jremoting.test.TestInterface2;
import org.codehaus.jremoting.test.TestInterface3;
import org.codehaus.jremoting.test.TestInterfaceImpl;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;


/**
 * Test Custom Stream over sockets, using JNDI on the client side ( a small change ).
 *
 * @author Paul Hammant
 */
public class CustomStreamJNDITestCase extends AbstractHelloTestCase {

    private Context jndiContext;

    public CustomStreamJNDITestCase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();

        // server side setup.
        server = new CompleteSocketCustomStreamServer(10006);
        testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        // JNDI lookup.  Note there are no imports of JRemoting classes in this test.
        Hashtable env = new Hashtable();

        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.codehaus.jremoting.client.naming.DefaultInitialContextFactory");
        env.put(Context.PROVIDER_URL, "jremoting://localhost:10006/SocketCustomStream");
        env.put("proxy.type", "ClientSideClasses");
        env.put("bean.type", "NotBeanOnly");
        env.put("optimize", "false");

        jndiContext = new InitialContext(env);

        testClient = (TestInterface) jndiContext.lookup("Hello");

        // just a kludge for unit testing given we are intrinsically dealing with
        // threads, JRemoting being a client/server thing
        Thread.yield();
    }

    protected void tearDown() throws Exception {
        testClient = null;
        System.gc();
        Thread.yield();
        jndiContext.close();
        Thread.yield();
        server.stop();
        Thread.yield();
        server = null;
        testServer = null;
        super.tearDown();
    }


}
