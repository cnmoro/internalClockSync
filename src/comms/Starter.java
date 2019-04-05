package comms;

import java.security.PrivateKey;
import java.security.PublicKey;
import static crypto.RSACryptography.*;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import model.Peer;

/**
 *
 * @author cnmoro
 */
public class Starter {

    static final String MULTICAST_IP = "228.5.6.7";

    public static void main(String[] args) throws Exception {
        //Cria o socket e inicializa o processo
        MulticastSocket socket = null;

        try {
            InetAddress group = InetAddress.getByName(MULTICAST_IP);
            socket = new MulticastSocket(6789);
            socket.joinGroup(group);

            Map<String, Object> keysA = getRSAKeys();

            Scanner scan = new Scanner(System.in);
            System.out.println("Process identifier: ");
            String id = scan.nextLine();

            //Gera porta aleatoria entre 5000 e 6000
            int port = new Random().nextInt((6000 - 5000) + 1) + 5000;

            CommonInfo.peer = new Peer(
                    (PrivateKey) keysA.get("private"),
                    (PublicKey) keysA.get("public"),
                    id,
                    port);

            new CommProcess(socket, group).start();

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}
