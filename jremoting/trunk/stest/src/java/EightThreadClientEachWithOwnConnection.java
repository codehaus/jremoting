import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.SocketDetails;
import org.codehaus.jremoting.client.streams.ByteStreamConnectionFactory;
import org.codehaus.jremoting.client.resolver.ServiceResolver;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.transports.SocketTransport;

public class EightThreadClientEachWithOwnConnection {
    public static void main(String[] args) throws ConnectionException {

        for (int i = 0; i < 9; i++) {
            new Thread() {
                public void run() {
                    System.out.println("Client Starting ...");
                    try {
                        Thread.sleep(1000);
                        mathLoop((Addition) new ServiceResolver(new SocketTransport(
                                new ConsoleClientMonitor(), 
                                new ByteStreamConnectionFactory(),
                                new SocketDetails("209.20.68.189", 10333, 1))).resolveService("Addition"));
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