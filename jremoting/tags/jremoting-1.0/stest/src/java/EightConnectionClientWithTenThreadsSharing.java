import org.codehaus.jremoting.client.transports.SocketTransport;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.SocketDetails;
import org.codehaus.jremoting.client.streams.ByteStream;
import org.codehaus.jremoting.client.resolver.ServiceResolver;
import org.codehaus.jremoting.ConnectionException;

public class EightConnectionClientWithTenThreadsSharing {
    public static void main(String[] args) throws ConnectionException {

        // Client side setup
        final Addition addition = (Addition) new ServiceResolver(new SocketTransport(
                new ConsoleClientMonitor(),
                new ByteStream(),
                new SocketDetails("localhost", 10333, 8))).resolveService("Addition");

        for (int i = 0; i < 10; i++) {
            new Thread() {
                public void run() {
                    mathLoop(addition);
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
