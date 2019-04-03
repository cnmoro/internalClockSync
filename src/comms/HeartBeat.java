package comms;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 *
 * @author cnmoro
 */
public class HeartBeat extends Thread {

    MulticastSocket s;
    InetAddress group;

    public HeartBeat(MulticastSocket s, InetAddress group) {
        this.s = s;
        this.group = group;
    }

    @Override
    public void run() {
        try {
            while (s != null && CommonInfo.amIMaster()) {
                //Envia disponibilidade do mestre
                sleep();
                sendHeartBeat();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendHeartBeat() {
        try {
            byte[] msg = ("SendingHeartBeat:" + CommonInfo.master).getBytes();

            DatagramPacket messageOut = new DatagramPacket(msg, msg.length, group, 6789);
            s.send(messageOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sleep() throws InterruptedException {
        Thread.sleep(CommonInfo.deltaT1);
    }
}
