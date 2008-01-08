package org.codehaus.jremoting.server.encoders;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.StreamEncoding;
import org.codehaus.jremoting.server.StreamEncoder;

import java.io.InputStream;
import java.io.OutputStream;

public class XStreamEncoding implements StreamEncoding {

    private XStream xStream;

    public XStreamEncoding(XStream xstream) {
        this.xStream = xstream;
    }

    public XStreamEncoding() {
        this (new XStream(new DomDriver()));
    }

    public StreamEncoder createEncoder(ServerMonitor serverMonitor, ClassLoader facadesClassLoader,
                                           InputStream inputStream, OutputStream outputStream, Object connectionDetails) {
        
        return new XStreamEncoder(serverMonitor, facadesClassLoader, inputStream, outputStream, connectionDetails, xStream);
    }

}
