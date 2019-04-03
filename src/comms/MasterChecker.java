package comms;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 *
 * @author cnmoro
 */
public class MasterChecker extends Thread {

    MulticastSocket s;
    InetAddress group;

    public MasterChecker(MulticastSocket s, InetAddress group) {
        this.s = s;
        this.group = group;
    }

    @Override
    public void run() {
        try {
            while (s != null) {
                //Indica que o mestre não está funcionando
                CommonInfo.masterAlive = false;
                //Durante deltaT1 se houver um heartbeat sera indicado que o mestre esta vivo
                sleep();
                //Verifica se o mestre ainda 'esta vivo'
                checkMaster();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Se nao receber keepalive, envia requisicao para substituir o mestre
    void checkMaster() {
        if (CommonInfo.masterAlive == false) {
            sendMasterReplacementRequest();
        }
    }

    void sendMasterReplacementRequest() {
        try {
            String req = "ReplaceMaster:" + CommonInfo.peer.getIdentifier();
            byte[] msg = req.getBytes();

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
