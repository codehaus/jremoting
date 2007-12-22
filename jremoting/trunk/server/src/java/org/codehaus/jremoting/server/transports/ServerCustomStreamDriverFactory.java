package org.codehaus.jremoting.server.transports;

import org.codehaus.jremoting.server.ServerMonitor;

import java.io.*;

public class ServerCustomStreamDriverFactory implements ServerStreamDriverFactory {

    public ServerStreamDriver createDriver(ServerMonitor serverMonitor, ClassLoader facadesClassLoader,
                                           InputStream inputStream, OutputStream outputStream, Object connectionDetails) {
        return new ServerCustomStreamDriver(serverMonitor, new DataInputStream(inputStream),
                new DataOutputStream(new BufferedOutputStream(outputStream)), facadesClassLoader, connectionDetails);
    }

}
