import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.transports.SocketServer;

import java.net.InetSocketAddress;

public class Server {

    static int ct = 0;
    static long last = System.currentTimeMillis();

    public static void main(String[] args) {

               // server side setup.
        SocketServer server = new SocketServer(new ConsoleServerMonitor(), new InetSocketAddress(10333));
        Addition addition = new Addition() {
            public double add(double a, double b) {
                pc();
                //sleepTenMillis();
                return a+b;
            }
        };
        Publication pd = new Publication(Addition.class);
        server.publish(addition, "Addition", pd);
        server.start();
    }

    private static void sleepTenMillis() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        }
    }

    private synchronized static void pc() {
        ct++;
        if (System.currentTimeMillis() > (last + 60000)) {
            System.out.println(""+(int)ct/60 + "/sec");
            ct = 0;
            last = System.currentTimeMillis();
        }
    }

}
