package org.codehaus.jremoting.server.streams;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.Stream;
import org.codehaus.jremoting.server.StreamConnection;

import java.io.InputStream;
import java.io.OutputStream;

public class XmlStream implements Stream {

    private XStream xStream;

    public XmlStream(XStream xstream) {
        this.xStream = xstream;
    }

    public XmlStream() {
        this (new XStream(new DomDriver()));
    }

    public StreamConnection makeStreamConnection(ServerMonitor serverMonitor, ClassLoader facadesClassLoader,
                                           InputStream inputStream, OutputStream outputStream, String connectionDetails) {
        
        return new XStreamConnection(serverMonitor, facadesClassLoader, inputStream, outputStream, connectionDetails, xStream);
    }

}
