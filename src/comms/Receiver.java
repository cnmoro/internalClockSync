package comms;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Random;
import model.KeyPacket;
import model.Peer;

/**
 *
 * @author cnmoro
 */
public class Receiver extends Thread {

    MulticastSocket s;
    InetAddress group;
    byte[] buffer;
    DatagramPacket msgIn;

    public Receiver(MulticastSocket s, InetAddress group) {
        this.s = s;
        this.group = group;
    }

    @Override
    public void run() {
        try {
            this.buffer = new byte[6400];
            this.msgIn = new DatagramPacket(buffer, buffer.length);

            //Recebe continuamente as mensagens
            while (s != null) {
                String receivedMsg = getMessage();
                System.out.println("Peer " + CommonInfo.peer.getIdentifier() + " received: " + receivedMsg);
                resetBuffer();

                //Controle de definicao de mestre - 1a vez
                if (isMasterRequest(receivedMsg)) {
                    if (!CommonInfo.isMasterDefined()) {
                        setMaster(getSender(receivedMsg));
                        System.out.println("I am " + CommonInfo.peer.getIdentifier() + " and my master now is: " + CommonInfo.master);
                        sendPublicKey();
                        if (CommonInfo.amIMaster()) {
                            //Inicia thread de envio de heartbeat/keepalive...
                            new HeartBeat(s, group).start();

                            //Inicia thread de atualizacao de relogios
                            new ClockPoll(s, group).start();
                        }
                        Thread.sleep((new Random().nextInt((4 - 2) + 1) + 2) * 1000);
                        new MasterChecker(s, group).start();
                    }
                } else if (isMasterReplaceRequest(receivedMsg)) {
                    CommonInfo.replacingMaster = true;
                    removeMaster();
                    setMaster(getSender(receivedMsg));
                    System.out.println("I am " + CommonInfo.peer.getIdentifier() + " and my master (replaced) now is: " + CommonInfo.master);
                    //sendPublicKey();
                    if (CommonInfo.amIMaster()) {
                        //Inicia thread de envio de heartbeat/keepalive...
                        new HeartBeat(s, group).start();
                        new ClockPoll(s, group).start();
                    }
                    //Aqui controle de recebimento da chave publica dos outros processos
                } else if (isPeerPublicKeyRequest(receivedMsg)) {
                    // PARSE KEYPACKET OBJECT
                    receivedMsg = receivedMsg.replace("SendingPeerPublicKey:", "");
                    String peerId = receivedMsg.substring(0, receivedMsg.indexOf("***"));
                    //System.out.println("peerId: " + peerId);
                    receivedMsg = receivedMsg.substring(receivedMsg.indexOf("***") + 3, receivedMsg.length());
                    String publickey = receivedMsg.substring(0, receivedMsg.indexOf("***"));
                    //System.out.println("publicKey: " + publickey);
                    String port = receivedMsg.substring(receivedMsg.indexOf("***") + 3, receivedMsg.length());
                    //System.out.println("port: " + port);

                    //External code
                    byte[] decodedKey = Base64.getDecoder().decode(publickey.trim());
                    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    PublicKey originalKey = keyFactory.generatePublic(keySpec);
                    //

                    System.out.println("Stored public key from Peer " + peerId + " in port " + port);

                    addPublicKey(new Peer(null, originalKey, peerId, Integer.parseInt(port)));
                } else if (isHeartBeat(receivedMsg)) {
                    CommonInfo.masterAlive = true;
                } else if (isMasterInquiry(receivedMsg)) {
                    sendMasterInfo();
                } else if (isMasterInfo(receivedMsg)) {
                    setMaster(getSender(receivedMsg));
                    sendPublicKey();
                    if (CommonInfo.amIMaster()) {
                        //Inicia thread de envio de heartbeat/keepalive...
                        new HeartBeat(s, group).start();
                    }
                    new MasterChecker(s, group).start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addPublicKey(Peer pid) {
        //Verifica se o peer ja está na lista, e só adiciona caso nao esteja
        boolean shouldAdd = true;

        for (Peer p : CommonInfo.publicKeys) {
            if (p.getIdentifier().equalsIgnoreCase(pid.getIdentifier())) {
                shouldAdd = false;
                break;
            }
        }

        if (shouldAdd) {
            CommonInfo.publicKeys.add(pid);
        }
    }

    void sendPublicKey() {
        try {
            String encodedPubKey = Base64.getEncoder().encodeToString(CommonInfo.peer.getPubKey().getEncoded());
            KeyPacket kp = new KeyPacket(CommonInfo.peer.getIdentifier(), encodedPubKey, CommonInfo.peer.getPort());

            byte[] msg = ("SendingPeerPublicKey:" + kp.getIdentifier() + "***" + kp.getPublicKey() + "***" + kp.getPort()).getBytes();

            DatagramPacket packet = new DatagramPacket(msg, msg.length, group, 6789);
            s.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendMasterInfo() {
        try {
            String info = "MasterIs:" + CommonInfo.master;
            byte[] msg = info.getBytes();

            DatagramPacket messageOut = new DatagramPacket(msg, msg.length, group, 6789);
            s.send(messageOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String getMessage() {
        try {
            s.receive(msgIn);
            String receivedMsg = new String(msgIn.getData());
            return receivedMsg.trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    void removeMaster() {
        String m = CommonInfo.master;

        for (int i = 0; i < CommonInfo.publicKeys.size(); i++) {
            if (CommonInfo.publicKeys.get(i).getIdentifier().equalsIgnoreCase(m)) {
                CommonInfo.publicKeys.remove(i);
            }
        }

        for (int i = 0; i < CommonInfo.timePeers.size(); i++) {
            if (CommonInfo.timePeers.get(i).getPeerIdentifier().equalsIgnoreCase(m)) {
                CommonInfo.timePeers.remove(i);
            }
        }
    }

    void setMaster(String m) {
        CommonInfo.master = m;
        CommonInfo.masterAlive = true;
    }

    void resetBuffer() {
        this.buffer = new byte[1000];
        msgIn.setData(this.buffer);
    }

    boolean isMasterInfo(String msg) {
        return msg.contains("MasterIs:");
    }

    boolean isMasterRequest(String msg) {
        return msg.contains("MasterRequest:");
    }

    boolean isMasterReplaceRequest(String msg) {
        return msg.contains("ReplaceMaster:");
    }

    boolean isPeerPublicKeyRequest(String msg) {
        return msg.contains("SendingPeerPublicKey:");
    }

    boolean isMasterInquiry(String msg) {
        return msg.contains("WhoIsTheMaster?");
    }

    boolean isHeartBeat(String msg) {
        return msg.contains("SendingHeartBeat:");
    }

    String getSender(String msg) {
        String[] parts = msg.split("\\:");
        return parts[1];
    }
}
