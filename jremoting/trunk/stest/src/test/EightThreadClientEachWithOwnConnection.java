import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.client.SocketDetails;
import org.codehaus.jremoting.client.encoders.ByteStreamEncoding;
import org.codehaus.jremoting.client.factories.JRemotingClient;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.transports.socket.SocketTransport;

public class EightThreadClientEachWithOwnConnection {
    public static void main(String[] args) throws ConnectionException {

        for (int i = 0; i < 9; i++) {
            new Thread() {
                public void run() {
                    System.out.println("Client Starting ...");
                    try {
                        Thread.sleep(1000);
                        mathLoop((Addition) new JRemotingClient(new SocketTransport(new ConsoleClientMonitor(), new ByteStreamEncoding(),
                                new SocketDetails("localhost", 10333, 1))).lookupService("Addition"));
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