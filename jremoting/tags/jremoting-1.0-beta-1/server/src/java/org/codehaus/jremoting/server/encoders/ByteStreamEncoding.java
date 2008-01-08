package org.codehaus.jremoting.server.encoders;

import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.StreamEncoding;
import org.codehaus.jremoting.server.StreamEncoder;

import java.io.*;

public class ByteStreamEncoding implements StreamEncoding {

    public StreamEncoder createEncoder(ServerMonitor serverMonitor, ClassLoader facadesClassLoader,
                                           InputStream inputStream, OutputStream outputStream, Object connectionDetails) {
        return new ByteStreamEncoder(serverMonitor, new DataInputStream(inputStream),
                new DataOutputStream(new BufferedOutputStream(outputStream)), facadesClassLoader, connectionDetails);
    }

}
