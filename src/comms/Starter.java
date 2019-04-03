package comms;

import java.security.PrivateKey;
import java.security.PublicKey;
import static crypto.RSACryptography.*;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;
import java.util.Scanner;
import model.Peer;

/**
 *
 * @author cnmoro
 */
public class Starter {

    static final String MULTICAST_IP = "228.5.6.7";

    public static void main(String[] args) throws Exception {
//        String plainText = "Hello World!";
//
//        // Generate public and private keys using RSA
//        Map<String, Object> keys = getRSAKeys();
//
//        PrivateKey privateKey = (PrivateKey) keys.get("private");
//        PublicKey publicKey = (PublicKey) keys.get("public");
//
//        String encryptedText = encryptMessage(plainText, privateKey);
//        String descryptedText = decryptMessage(encryptedText, publicKey);
//
//        System.out.println("input:" + plainText);
//        System.out.println("encrypted:" + encryptedText);
//        System.out.println("decrypted:" + descryptedText);

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

            CommonInfo.peer = new Peer(
                    "Slave",
                    (PrivateKey) keysA.get("private"),
                    (PublicKey) keysA.get("public"),
                    id);

            new CommProcess(socket, group).start();

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}
