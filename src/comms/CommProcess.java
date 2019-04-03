package comms;

import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 *
 * @author cnmoro
 */
public class CommProcess {

    MulticastSocket s;
    InetAddress group;

    public CommProcess(MulticastSocket s, InetAddress group) {
        this.s = s;
        this.group = group;
    }

    public void start() {
        new Receiver(s, group).start();
        new Sender(s, group).start();
        new TimeOutChecker(s, group).start();
        System.out.println("Peer " + CommonInfo.peer.getIdentifier() + " started.");
    }
}
