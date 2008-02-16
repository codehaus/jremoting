package org.codehaus.jremoting.server.streams;

import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.StreamConnectionFactory;
import org.codehaus.jremoting.server.StreamConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ObjectStreamConnectionFactory implements StreamConnectionFactory {

    public StreamConnection makeStreamConnection(ServerMonitor serverMonitor, ClassLoader facadesClassLoader,
                                           InputStream inputStream, OutputStream outputStream, String connectionDetails) throws IOException {
        return new ObjectStreamConnection(serverMonitor, facadesClassLoader, inputStream, outputStream, connectionDetails);
    }

}
