package comms;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Date;
import java.util.Random;
import com.google.gson.JsonElement;

/**
 *
 * @author cnmoro
 */
public class Sender extends Thread {

    MulticastSocket s;
    InetAddress group;

    public Sender(MulticastSocket s, InetAddress group) {
        this.s = s;
        this.group = group;
    }

    @Override
    public void run() {
        try {
            sleep();

            while (s != null) {
                //Envia informacoes se nao for o mestre, e se o mestre ja estiver definido
                if (CommonInfo.isMasterDefined() && CommonInfo.amIMaster() == false) {
                    sendPeerData();
                } else if (CommonInfo.isMasterDefined() == false) {
                    sendMasterRequest();
                }
                sleep();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendPeerData() {
        try {
            JsonElement jsonElement = CommonInfo.gson.toJsonTree(CommonInfo.peer);
            jsonElement.getAsJsonObject().addProperty("clock", CommonInfo.sdf.format(new Date()));
            byte[] msg = ("SendingPeerInformation:" + CommonInfo.gson.toJson(jsonElement)).getBytes();

            DatagramPacket messageOut = new DatagramPacket(msg, msg.length, group, 6789);
            s.send(messageOut);
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
