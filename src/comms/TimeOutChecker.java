package comms;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 *
 * @author cnmoro
 */
public class TimeOutChecker extends Thread {

    MulticastSocket s;
    InetAddress group;

    public TimeOutChecker(MulticastSocket s, InetAddress group) {
        this.s = s;
        this.group = group;
    }

    @Override
    public void run() {
        try {
            while (s != null && CommonInfo.isMasterDefined() == false) {
                sleep();
                if (CommonInfo.isMasterDefined() == false) {
                    sendMasterInquiry();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendMasterInquiry() {
        try {
            byte[] msg = "WhoIsTheMaster?".getBytes();

            DatagramPacket messageOut = new DatagramPacket(msg, msg.length, group, 6789);
            System.out.println("Peer " + CommonInfo.peer.getIdentifier() + " sending master inquiry");
            s.send(messageOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sleep() throws InterruptedException {
        Thread.sleep(25000);
    }
}
