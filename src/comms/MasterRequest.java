package comms;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;

/**
 *
 * @author cnmoro
 */
public class MasterRequest extends Thread {

    MulticastSocket s;
    InetAddress group;

    public MasterRequest(MulticastSocket s, InetAddress group) {
        this.s = s;
        this.group = group;
    }

    @Override
    public void run() {
        try {
            sleep();

            while (s != null) {
                //Envia informacoes se nao for o mestre, e se o mestre ja estiver definido
                if (CommonInfo.isMasterDefined() == false) {
                    sendMasterRequest();
                }
                sleep();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendMasterRequest() {
        try {
            String req = "MasterRequest:" + CommonInfo.peer.getIdentifier();
            byte[] msg = req.getBytes();

            DatagramPacket messageOut = new DatagramPacket(msg, msg.length, group, 6789);
            s.send(messageOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sleep() throws InterruptedException {
        Thread.sleep((new Random().nextInt((7 - 4) + 1) + 4) * 1000);
    }

}
