package comms;

import java.net.InetAddress;
import java.net.MulticastSocket;
import model.PeerID;

/**
 *
 * @author cnmoro
 */
public class ClockPoll extends Thread {

    MulticastSocket s;
    InetAddress group;

    public ClockPoll(MulticastSocket s, InetAddress group) {
        this.s = s;
        this.group = group;
    }

    @Override
    public void run() {
        try {
            while (s != null && CommonInfo.amIMaster()) {
                sleep();

                sendClockPoll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendClockPoll() {
        try {
            for (PeerID pid : CommonInfo.publicKeys) {
                String msg = "SendingClockPoll:" + CommonInfo.master;

                //Só envia se o peer nao for o mestre
                if (!pid.getIdentifier().equalsIgnoreCase(CommonInfo.master)) {
                    //Envia poll para todos os escravos
                    //TODO: guardar timestamp para comparar com o tempo de resposta
                    new UnicastMessenger(pid.getPort(), msg).start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sleep() throws InterruptedException {
        Thread.sleep(CommonInfo.deltaT2);
    }
}
