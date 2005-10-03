/* ====================================================================
 * Copyright 2005 JRemoting Committers
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
package org.codehaus.jremoting.client.factories;

import org.codehaus.jremoting.api.DefaultThreadPool;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.InterfaceLookupFactory;
import org.codehaus.jremoting.client.monitors.DumbClientMonitor;
import org.codehaus.jremoting.client.pingers.DefaultConnectionPinger;

/**
 * Class DefaultInterfaceLookupFactory
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class DefaultInterfaceLookupFactory extends AbstractInterfaceLookupFactory {

    public static final String[] SUPPORTEDSTREAMS = new String[]{"SocketObjectStream", "SocketCustomStream", "RMI"};
    private ThreadPool threadPool;
    private ClientMonitor clientMonitor;
    private ConnectionPinger connectionPinger;

    public DefaultInterfaceLookupFactory() {
        this(new DefaultThreadPool(), new DumbClientMonitor(), new DefaultConnectionPinger());
    }


    public DefaultInterfaceLookupFactory(ThreadPool threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger) {
        this.threadPool = threadPool;
        this.connectionPinger = connectionPinger;
        this.clientMonitor = clientMonitor;

        try {
            Class ilf = this.getClass().getClassLoader().loadClass("org.codehaus.jremoting.client.impl.socket.SocketObjectStreamFactoryHelper");
            InterfaceLookupFactory factory = (InterfaceLookupFactory) ilf.newInstance();
            factory.setClientMonitor(this.clientMonitor);
            factory.setConnectionPinger(this.connectionPinger);
            factory.setThreadPool(this.threadPool);
            addFactory("SocketObjectStream:", factory);
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        } catch (InstantiationException ie) {
            ie.printStackTrace();


        } catch (IllegalAccessException iae) {

        }

        try {
            Class ilf = this.getClass().getClassLoader().loadClass("org.codehaus.jremoting.client.impl.socket.SocketCustomStreamFactoryHelper");
            InterfaceLookupFactory factory = (InterfaceLookupFactory) ilf.newInstance();
            factory.setClientMonitor(this.clientMonitor);
            factory.setConnectionPinger(this.connectionPinger);
            factory.setThreadPool(this.threadPool);
            addFactory("SocketCustomStream:", factory);


        } catch (ClassNotFoundException cnfe) {

        } catch (InstantiationException ie) {

        } catch (IllegalAccessException iae) {

        }

        try {
            Class ilf = Class.forName("org.codehaus.jremoting.client.impl.rmi.RmiFactoryHelper");
            InterfaceLookupFactory factory = (InterfaceLookupFactory) ilf.newInstance();
            factory.setClientMonitor(this.clientMonitor);
            factory.setConnectionPinger(this.connectionPinger);
            factory.setThreadPool(this.threadPool);
            addFactory("RMI:", factory);

        } catch (ClassNotFoundException cnfe) {
        } catch (InstantiationException ie) {
        } catch (IllegalAccessException iae) {
        }

        // TODO - add the rest.
    }

    public void setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public void setClientMonitor(ClientMonitor clientMonitor) {
        this.clientMonitor = clientMonitor;
    }

    public void setConnectionPinger(ConnectionPinger connectionPinger) {
        this.connectionPinger = connectionPinger;
    }
}
