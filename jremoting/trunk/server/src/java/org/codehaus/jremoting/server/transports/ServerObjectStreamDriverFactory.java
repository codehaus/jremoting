package org.codehaus.jremoting.server.transports;

import org.codehaus.jremoting.server.ServerMonitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ServerObjectStreamDriverFactory implements ServerStreamDriverFactory {

    public ServerStreamDriver createDriver(ServerMonitor serverMonitor, ClassLoader facadesClassLoader,
                                           InputStream inputStream, OutputStream outputStream, Object connectionDetails) throws IOException {
        return new ServerObjectStreamDriver(serverMonitor, facadesClassLoader, inputStream, outputStream, connectionDetails);
    }

}
