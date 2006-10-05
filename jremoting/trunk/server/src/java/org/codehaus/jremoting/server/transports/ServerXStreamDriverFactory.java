package org.codehaus.jremoting.server.transports;

import org.codehaus.jremoting.server.ServerMonitor;

import java.util.concurrent.ScheduledExecutorService;
import java.io.InputStream;
import java.io.OutputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ServerXStreamDriverFactory implements ServerStreamDriverFactory {

    private XStream xStream;


    public ServerXStreamDriverFactory(XStream xstream) {
        this.xStream = xstream;
    }


    public ServerXStreamDriverFactory() {
        this (new XStream(new DomDriver()));
    }

    public ServerStreamDriver createDriver(ServerMonitor serverMonitor, ClassLoader facadesClassLoader,
                                           InputStream inputStream, OutputStream outputStream, Object connectionDetails) {
        return new ServerXStreamDriver(serverMonitor, facadesClassLoader, inputStream, outputStream, connectionDetails, xStream);
    }

}
