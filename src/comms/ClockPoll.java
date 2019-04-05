package comms;

import java.net.InetAddress;
import java.net.MulticastSocket;
import model.Peer;
import model.TimePeer;

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
            System.out.println("Starting ClockPolling from " + CommonInfo.peer.getIdentifier());
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
            System.out.println("Sending clock poll to " + CommonInfo.publicKeys.size() + " peers.");
            String msg = "SendingClockPoll:" + CommonInfo.master;
            for (Peer pid : CommonInfo.publicKeys) {
                System.out.println("Trying to send clock poll to peer " + pid.getIdentifier() + " in port " + pid.getPort());
                //Só envia se o peer nao for o mestre
                if (!pid.getIdentifier().equalsIgnoreCase(CommonInfo.master)) {
                    System.out.println("Peer " + pid.getIdentifier() + " is not the master. Sending.");
                    //Envia poll para todos os escravos, e marca o tempo atual
                    new UnicastMessenger(pid.getPort(), msg).start();

                    addTimePeer(pid.getIdentifier());
                } else {
                    System.out.println("Peer " + pid.getIdentifier() + " is the master, skipping...");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addTimePeer(String id) {
        boolean exists = false;
        int index = 0;

        for (int i = 0; i < CommonInfo.timePeersInstant.size(); i++) {
            if (CommonInfo.timePeersInstant.get(i).getPeerIdentifier().equalsIgnoreCase(id)) {
                exists = true;
                index = i;
            }
        }

        if (!exists) {
            CommonInfo.timePeersInstant.add(new TimePeer(id, System.currentTimeMillis(), 0));
        } else {
            CommonInfo.timePeersInstant.get(index).setPeerIdentifier(id);
            CommonInfo.timePeersInstant.get(index).setTime(System.currentTimeMillis());
        }
    }

    void sleep() throws InterruptedException {
        Thread.sleep(CommonInfo.deltaT2);
    }
}
