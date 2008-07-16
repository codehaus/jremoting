package org.codehaus.jremoting.server.streams;

import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.Stream;
import org.codehaus.jremoting.server.StreamConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ObjectStream implements Stream {

    public StreamConnection makeStreamConnection(ServerMonitor serverMonitor, ClassLoader facadesClassLoader,
                                           InputStream inputStream, OutputStream outputStream, String connectionDetails) throws IOException {
        return new ObjectStreamConnection(serverMonitor, facadesClassLoader, inputStream, outputStream, connectionDetails);
    }

}
