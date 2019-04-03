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
    boolean masterAlive = false;
    final int deltaT1 = 10000; //10 s

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

                //Recebe continuamente as mensagens
                while (s != null) {
                    String receivedMsg = getMessage();
                    System.out.println("Peer " + peer.getIdentifier() + " received: " + receivedMsg);
                    resetBuffer();

                    //Controle de definicao de mestre - 1a vez
                    if (isMasterRequest(receivedMsg)) {
                        if (!isMasterDefined()) {
                            setMaster(getSender(receivedMsg));
                            System.out.println("I am " + peer.getIdentifier() + " and my master now is: " + master);
                            sendPublicKey();
                            if (amIMaster()) {
                                //Inicia thread de envio de heartbeat/keepalive...
                                new heartBeat(s, group).start();
                            }
                            new masterChecker(s, group).start();
                        }
                        //Controle de recebimento de informacoes dos peers (dados relogio)
                    } else if (isPeerInformationRequest(receivedMsg)) {
//                        sdf.parse("DATASTRING");
                        //TODO
                        //Controle do recebimento de informacao para substituir o mestre (quando ocorrem falhas)
                        //Aqui tambem ocorre o teste de autenticidade atraves da chave publica
                    } else if (isMasterReplaceRequest(receivedMsg)) {
                        setMaster(getSender(receivedMsg));
                        System.out.println("I am " + peer.getIdentifier() + " and my master (replaced) now is: " + master);
                        //sendPublicKey();
                        if (amIMaster()) {
                            //Inicia thread de envio de heartbeat/keepalive...
                            new heartBeat(s, group).start();
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

                        this.publicKeys.add(new PeerID(peerId, originalKey));
                    } else if (receivedMsg.contains("SendingHeartBeat:")) {
                        masterAlive = true;
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
                return receivedMsg.trim();
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        void setMaster(String m) {
            master = m;
            masterAlive = true;
        }

        void resetBuffer() {
            this.buffer = new byte[1000];
            msgIn.setData(this.buffer);
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

        String getSender(String msg) {
            String[] parts = msg.split("\\:");
            return parts[1];
        }
    }

    public class heartBeat extends Thread {

        MulticastSocket s;
        InetAddress group;

        public heartBeat(MulticastSocket s, InetAddress group) {
            this.s = s;
            this.group = group;
        }

        @Override
        public void run() {
            try {
                while (s != null && amIMaster()) {
                    //Envia disponibilidade do mestre
                    sleep();
                    sendHeartBeat();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void sendHeartBeat() {
            try {
                byte[] msg = ("SendingHeartBeat:" + master).getBytes();

                DatagramPacket messageOut = new DatagramPacket(msg, msg.length, group, 6789);
                s.send(messageOut);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void sleep() throws InterruptedException {
            Thread.sleep(deltaT1);
        }
    }

    public class masterChecker extends Thread {

        MulticastSocket s;
        InetAddress group;

        public masterChecker(MulticastSocket s, InetAddress group) {
            this.s = s;
            this.group = group;
        }

        @Override
        public void run() {
            try {
                while (s != null) {
                    //Indica que o mestre não está funcionando
                    masterAlive = false;
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
            if (masterAlive == false) {
                sendMasterReplacementRequest();
            }
        }

        void sendMasterReplacementRequest() {
            try {
                String req = "ReplaceMaster:" + peer.getIdentifier();
                byte[] msg = req.getBytes();

                DatagramPacket messageOut = new DatagramPacket(msg, msg.length, group, 6789);
                s.send(messageOut);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void sleep() throws InterruptedException {
            Thread.sleep(deltaT1);
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
                    //Envia informacoes se nao for o mestre, e se o mestre ja estiver definido
                    if (isMasterDefined() && amIMaster() == false) {
                        sendPeerData();
                    } else if (isMasterDefined() == false) {
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
                String req = "MasterRequest:" + peer.getIdentifier();
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

    boolean isMasterDefined() {
        return !master.equals("");
    }

    boolean amIMaster() {
        return master.equalsIgnoreCase(peer.getIdentifier());
    }
}
