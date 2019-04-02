package comms;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import model.KeyPacket;
import model.Peer;
import model.PeerID;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 *
 * @author cnmoro
 */
public class CommProcess {

    Gson gson = new Gson();
    String master = "";
    Peer peer;
    MulticastSocket s;
    InetAddress group;
    DateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public CommProcess(MulticastSocket s, InetAddress group, Peer peer) {
        this.s = s;
        this.group = group;
        this.peer = peer;
    }

    public void start() {
        new msgReceiver(s, group).start();
        new msgSender(s, group).start();
        System.out.println("Peer " + peer.getIdentifier() + " started.");
    }

    public class msgReceiver extends Thread {

        MulticastSocket s;
        InetAddress group;
        byte[] buffer;
        DatagramPacket msgIn;
        ArrayList<PeerID> publicKeys;

        public msgReceiver(MulticastSocket s, InetAddress group) {
            this.s = s;
            this.group = group;
            this.publicKeys = new ArrayList<>();
        }

        @Override
        public void run() {
            try {
                this.buffer = new byte[6400];
                this.msgIn = new DatagramPacket(buffer, buffer.length);

                while (s != null) {
                    String receivedMsg = getMessage();
//                    System.out.println("Peer " + peer.getIdentifier() + " received: " + receivedMsg);
                    resetBuffer();

                    if (isMasterRequest(receivedMsg)) {
                        if (!isMasterDefined()) {
                            setMaster(getSender(receivedMsg));
                            System.out.println("I am " + peer.getIdentifier() + " and my master now is: " + master);
                            sendPublicKey();
                        }
                    } else if (receivedMsg.contains("SendingPeerInformation:")) {
//                        sdf.parse("DATASTRING");
                    } else if (receivedMsg.contains("ReplaceMaster:")) {
                        //replace Master
                    } else if (receivedMsg.contains("SendingPeerPublicKey:")) {
                        // PARSE KEYPACKET OBJECT
                        receivedMsg = receivedMsg.replace("SendingPeerPublicKey:", "");
                        String peerId = receivedMsg.substring(0, receivedMsg.indexOf("***"));
                        System.out.println("peerId: " + peerId);
                        String publickey = receivedMsg.substring(receivedMsg.indexOf("***") + 3, receivedMsg.length());
                        System.out.println("publicKey: " + publickey);

                        byte[] decodedKey = Base64.getDecoder().decode(publickey.trim());
                        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                        PublicKey originalKey = keyFactory.generatePublic(keySpec);

                        this.publicKeys.add(new PeerID(peerId, originalKey));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void sendPublicKey() {
            try {
                String encodedPubKey = Base64.getEncoder().encodeToString(peer.getPubKey().getEncoded());
                KeyPacket kp = new KeyPacket(peer.getIdentifier(), encodedPubKey);

                byte[] msg = ("SendingPeerPublicKey:" + kp.getIdentifier() + "***" + kp.getPublicKey()).getBytes();

                DatagramPacket packet = new DatagramPacket(msg, msg.length, group, 6789);
                s.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String getMessage() {
            try {
                s.receive(msgIn);
                String receivedMsg = new String(msgIn.getData());
                return receivedMsg;
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        void setMaster(String m) {
            master = m;
        }

        void resetBuffer() {
            this.buffer = new byte[1000];
            msgIn.setData(this.buffer);
        }

        boolean isMasterRequest(String msg) {
            return msg.contains("masterRequest:");
        }

        String getSender(String msg) {
            String[] parts = msg.split("\\:");
            return parts[1];
        }

        boolean isMasterDefined() {
            return !master.equals("");
        }
    }

    public class msgSender extends Thread {

        MulticastSocket s;
        InetAddress group;

        public msgSender(MulticastSocket s, InetAddress group) {
            this.s = s;
            this.group = group;
        }

        @Override
        public void run() {
            try {
                sleep();

                while (s != null) {
                    if (isMasterDefined()) {
                        sendPeerData();
                        sleep();
                    } else {
                        sendMasterRequest();
                        sleep();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void sendPeerData() {
            try {
                JsonElement jsonElement = gson.toJsonTree(peer);
                jsonElement.getAsJsonObject().addProperty("clock", sdf.format(new Date()));
                byte[] msg = ("SendingPeerInformation:" + gson.toJson(jsonElement)).getBytes();

                DatagramPacket messageOut = new DatagramPacket(msg, msg.length, group, 6789);
                s.send(messageOut);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void sendMasterRequest() {
            try {
                String req = "masterRequest:" + peer.getIdentifier();
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

        boolean isMasterDefined() {
            return !master.equals("");
        }
    }
}
