package org.codehaus.jremoting.client;

import java.net.InetSocketAddress;
import java.net.InetAddress;

public class SocketDetails {

    private final InetSocketAddress address;
    private final int concurrentConnections;
    
    public SocketDetails(String hostname, int port) {
        this(hostname, port, 1);
    }

    public SocketDetails(String hostname, int port, int concurrentConnections) {
        address = new InetSocketAddress(hostname, port);
        this.concurrentConnections = concurrentConnections;
    }

    public String getHostName() {
        return address.getHostName();
    }

    public int getPort() {
        return address.getPort();
    }

    public int getConcurrentConnections() {
        return concurrentConnections;
    }
}
