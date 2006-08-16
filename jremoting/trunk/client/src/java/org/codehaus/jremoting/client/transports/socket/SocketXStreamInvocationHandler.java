package org.codehaus.jremoting.client.transports.socket;

import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.ClientStreamReadWriter;
import org.codehaus.jremoting.client.transports.ClientCustomStreamReadWriter;
import org.codehaus.jremoting.client.transports.ClientXStreamStreamReadWriter;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class SocketCustomStreamInvocationHandler
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public final class SocketXStreamInvocationHandler extends AbstractSocketStreamInvocationHandler {

    /**
     * Constructor SocketCustomStreamInvocationHandler
     *
     * @param host                  the host name
     * @param port                  the port
     * @param interfacesClassLoader the classloader for deserialization hints.
     * @throws org.codehaus.jremoting.api.ConnectionException if a problem
     */
    public SocketXStreamInvocationHandler(ThreadPool threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger, ClassLoader interfacesClassLoader, String host, int port) throws ConnectionException {
        super(threadPool, clientMonitor, connectionPinger, interfacesClassLoader, host, port);
    }

    /**
     * Create a client stream read/writer
     *
     * @param in  the input stream
     * @param out the output stream
     * @return the read/writer
     * @throws org.codehaus.jremoting.api.ConnectionException if a problem
     */
    protected ClientStreamReadWriter createClientStreamReadWriter(InputStream in, OutputStream out) throws ConnectionException {
        return new ClientXStreamStreamReadWriter(in, out, interfacesClassLoader);
    }
}
