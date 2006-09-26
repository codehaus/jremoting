package org.codehaus.jremoting.server.transports;

import org.codehaus.jremoting.server.ServerMonitor;

import java.util.concurrent.ExecutorService;
import java.io.InputStream;
import java.io.OutputStream;

public class ServerXStreamDriverFactory implements ServerStreamDriverFactory {

    public ServerStreamDriver createDriver(ServerMonitor serverMonitor, ExecutorService executorService,
                                           InputStream inputStream, OutputStream outputStream, Object connectionDetails) {
        return new ServerXStreamDriver(serverMonitor, executorService, inputStream, outputStream, connectionDetails);
    }

}
