package comms;

import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 *
 * @author cnmoro
 */
public class SocketConnProperties {

    static MulticastSocket socket = null;
    static final String MULTICAST_IP = "228.5.6.7";
    static InetAddress group;

    public static void initializeSocketGroup() {
        try {
            InetAddress group = InetAddress.getByName(MULTICAST_IP);
            socket = new MulticastSocket(6789);
            socket.joinGroup(group);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
