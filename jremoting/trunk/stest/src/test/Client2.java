import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.encoders.ByteStreamEncoding;
import org.codehaus.jremoting.client.factories.JRemotingClient;
import org.codehaus.jremoting.client.monitors.Log4JClientMonitor;
import org.codehaus.jremoting.client.transports.socket.SocketTransport;

import java.net.InetSocketAddress;

public class Client2 {
    public static void main(String[] args) throws ConnectionException {

        for (int i = 0; i < 10; i++) {
            new Thread() {
                public void run() {
                    System.out.println("Client Starting ...");
                    try {
                        mathLoop((Addition) new JRemotingClient(new SocketTransport(new Log4JClientMonitor(), new ByteStreamEncoding(), new InetSocketAddress("localhost", 10333))).lookupService("Addition"));
                    } catch (Throwable e) {
                        System.out.println("Client Quitting ...");
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }
    private static synchronized void mathLoop(Addition addition) {
        while (true) {
            double a = Math.random();
            double b = Math.random();
            double c = addition.add(a,b);
            if (a+b != c) {
                System.err.println("MATH ERROR");
            }
        }
    }

}