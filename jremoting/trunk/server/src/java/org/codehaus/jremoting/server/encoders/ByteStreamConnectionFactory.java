package org.codehaus.jremoting.server.encoders;

import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.StreamConnectionFactory;
import org.codehaus.jremoting.server.StreamConnection;

import java.io.*;

public class ByteStreamConnectionFactory implements StreamConnectionFactory {

    public StreamConnection makeStreamConnection(ServerMonitor serverMonitor, ClassLoader facadesClassLoader,
                                           InputStream inputStream, OutputStream outputStream, String connectionDetails) {
        return new ByteStreamConnection(serverMonitor, new DataInputStream(inputStream),
                new DataOutputStream(new BufferedOutputStream(outputStream)), facadesClassLoader, connectionDetails);
    }

}
