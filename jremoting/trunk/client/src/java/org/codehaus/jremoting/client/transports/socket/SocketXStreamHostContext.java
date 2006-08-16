package org.codehaus.jremoting.client.transports.socket;

import org.codehaus.jremoting.client.factories.AbstractSocketStreamHostContext;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.pingers.NeverConnectionPinger;
import org.codehaus.jremoting.client.monitors.DumbClientMonitor;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.api.DefaultThreadPool;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Method;

/**
 * Class SocketCustomStreamHostContext
 *
 * @author Paul Hammant
 * @version $Revision: 1.3 $
 */
public class SocketXStreamHostContext extends AbstractSocketStreamHostContext {

    private int port;

    /**
     * Constructor SocketCustomStreamHostContext
     *
     * @param threadPool
     * @param clientMonitor
     * @param connectionPinger
     * @param host
     * @param port
     * @throws org.codehaus.jremoting.api.ConnectionException
     */
    public SocketXStreamHostContext(ThreadPool threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger, ClassLoader interfacesClassLoader, String host, int port) throws ConnectionException {
        super(threadPool, clientMonitor, connectionPinger, new SocketXStreamInvocationHandler(threadPool, clientMonitor, connectionPinger, interfacesClassLoader, host, port));
        this.port = port;
    }

    public SocketXStreamHostContext(String host, int port, ClassLoader classLoader) throws ConnectionException {
        this(new DefaultThreadPool(), new DumbClientMonitor(), new NeverConnectionPinger(), classLoader, host, port);
    }

    public SocketXStreamHostContext(String host, int port) throws ConnectionException {
        this(new DefaultThreadPool(), new DumbClientMonitor(), new NeverConnectionPinger(), SocketXStreamHostContext.class.getClassLoader(), host, port);
    }


    private Object bind(Object object, PipedInputStream inputStream, PipedOutputStream outputStream) {

        try {
            Object[] parms = new Object[]{inputStream, outputStream};
            Method method = object.getClass().getMethod("bind", new Class[]{parms.getClass()});
            return method.invoke(object, new Object[]{parms});
        } catch (Exception e) {
            return null;
        }
    }


}
