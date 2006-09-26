package org.codehaus.jremoting.server.transports;

import org.codehaus.jremoting.server.ServerMonitor;

import java.util.concurrent.ExecutorService;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class ServerObjectStreamDriverFactory implements ServerStreamDriverFactory {

    public ServerStreamDriver createDriver(ServerMonitor serverMonitor, ExecutorService executorService,
                                           InputStream inputStream, OutputStream outputStream, Object connectionDetails) throws IOException {
        return new ServerObjectStreamDriver(serverMonitor, executorService, inputStream, outputStream, connectionDetails);
    }

}
