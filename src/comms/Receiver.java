package comms;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import model.KeyPacket;
import model.PeerID;

/**
 *
 * @author cnmoro
 */
public class Receiver extends Thread {

    MulticastSocket s;
    InetAddress group;
    byte[] buffer;
    DatagramPacket msgIn;
    ArrayList<PeerID> publicKeys;

    public Receiver(MulticastSocket s, InetAddress group) {
        this.s = s;
        this.group = group;
        this.publicKeys = new ArrayList<>();
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
                        }
                        new MasterChecker(s, group).start();
                    }
                    //Controle de recebimento de informacoes dos peers (dados relogio)
                } else if (isPeerInformationRequest(receivedMsg)) {
//                        sdf.parse("DATASTRING");
                    //TODO
                    //Controle do recebimento de informacao para substituir o mestre (quando ocorrem falhas)
                    //Aqui tambem ocorre o teste de autenticidade atraves da chave publica
                } else if (isMasterReplaceRequest(receivedMsg)) {
                    setMaster(getSender(receivedMsg));
                    System.out.println("I am " + CommonInfo.peer.getIdentifier() + " and my master (replaced) now is: " + CommonInfo.master);
                    //sendPublicKey();
                    if (CommonInfo.amIMaster()) {
                        //Inicia thread de envio de heartbeat/keepalive...
                        new HeartBeat(s, group).start();
                    }
                    //Aqui controle de recebimento da chave publica dos outros processos
                } else if (isPeerPublicKeyRequest(receivedMsg)) {
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

                    addPublicKey(new PeerID(peerId, originalKey));
                } else if (receivedMsg.contains("SendingHeartBeat:")) {
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

    void addPublicKey(PeerID pid) {
        //Verifica se o peer ja está na lista, e só adiciona caso n esteja
        boolean shouldAdd = true;

        for (PeerID p : publicKeys) {
            if (p.getIdentifier().equalsIgnoreCase(pid.getIdentifier())) {
                shouldAdd = false;
                break;
            }
        }

        if (shouldAdd) {
            publicKeys.add(pid);
        }
    }

    void sendPublicKey() {
        try {
            String encodedPubKey = Base64.getEncoder().encodeToString(CommonInfo.peer.getPubKey().getEncoded());
            KeyPacket kp = new KeyPacket(CommonInfo.peer.getIdentifier(), encodedPubKey);

            byte[] msg = ("SendingPeerPublicKey:" + kp.getIdentifier() + "***" + kp.getPublicKey()).getBytes();

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

    boolean isPeerInformationRequest(String msg) {
        return msg.contains("SendingPeerInformation:");
    }

    boolean isMasterInquiry(String msg) {
        return msg.contains("WhoIsTheMaster?");
    }

    String getSender(String msg) {
        String[] parts = msg.split("\\:");
        return parts[1];
    }
}
