package org.codehaus.jremoting.server.transports;

import org.codehaus.jremoting.server.ServerMonitor;

import java.util.concurrent.ExecutorService;
import java.io.InputStream;
import java.io.OutputStream;

public class ServerCustomStreamDriverFactory implements ServerStreamDriverFactory {

    public ServerStreamDriver createDriver(ServerMonitor serverMonitor, ClassLoader facadesClassLoader,
                                           InputStream inputStream, OutputStream outputStream, Object connectionDetails) {
        return new ServerCustomStreamDriver(serverMonitor, inputStream, outputStream, facadesClassLoader, connectionDetails);
    }

}
