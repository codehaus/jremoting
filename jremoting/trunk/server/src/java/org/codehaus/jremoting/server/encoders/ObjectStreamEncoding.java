package org.codehaus.jremoting.server.encoders;

import org.codehaus.jremoting.server.ServerMonitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ObjectStreamEncoding implements StreamEncoding {

    public StreamEncoder createEncoder(ServerMonitor serverMonitor, ClassLoader facadesClassLoader,
                                           InputStream inputStream, OutputStream outputStream, Object connectionDetails) throws IOException {
        return new ObjectStreamEncoder(serverMonitor, facadesClassLoader, inputStream, outputStream, connectionDetails);
    }

}
