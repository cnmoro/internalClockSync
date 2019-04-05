package comms;

import crypto.RSACryptography;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import model.Peer;

/**
 *
 * @author cnmoro
 */
public class UnicastServer extends Thread {

    ServerSocket server;

    public UnicastServer() {
        try {
            this.server = new ServerSocket(CommonInfo.peer.getPort());

            System.out.println("UnicastServer of Peer " + CommonInfo.peer.getIdentifier() + " started in port " + CommonInfo.peer.getPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            InputStream in;

            while (true) {
                Socket socket = server.accept();

                socket.getRemoteSocketAddress().toString();

                InputStreamReader isr = new InputStreamReader(socket.getInputStream());
                BufferedReader br = new BufferedReader(isr);

                String msg = br.readLine();

                //TODO -> colocar em funções as 'msg.contains' igual ao receiver
                if (msg != null) {
                    System.out.println("Received via unicast: " + msg);
                    if (msg.contains("SendingClockPoll:")) {
                        System.out.println("Received clock poll from: " + msg.replace("SendingClockPoll:", ""));
                        //m = mestre da mensagem
                        String m = msg.replace("SendingClockPoll:", "");
                        if (m.equalsIgnoreCase(CommonInfo.master)) {
                            //Se o poll estiver vindo do mestre, envia informação do relógio interno
                            sendPeerInfo();
                        }
                    } else if (msg.contains("UpdateClock:")) {
                        String msgAux = msg.replace("UpdateClock:", "");

                        //Encontra o mestre na lista para acessar sua chave pública
                        for (Peer p : CommonInfo.publicKeys) {
                            if (p.getIdentifier().equalsIgnoreCase(CommonInfo.master)) {
                                String decryptedText = RSACryptography.decryptMessage(msgAux, p.getPubKey());
                                if (decryptedText.contains("Ok:")) {
                                    decryptedText = decryptedText.replace("Ok:", "");
                                    long diff = Long.parseLong(decryptedText);
                                    System.out.println("Time adjustment is: " + diff + " ms");
                                }
                                break;
                            }
                        }
                    } else if (msg.contains("PeerInfo:")) {
                        //Separa a mensagem

                        String msgAux = msg.replace("PeerInfo:", "");
                        String msgPeer = msgAux.substring(0, 1);
                        msgAux = msgAux.substring(4, msgAux.length());
                        String msgClock = msgAux;

                        //Verifica qual peer que está enviando a informação de relógio
                        for (int i = 0; i < CommonInfo.timePeers.size(); i++) {
                            //Este é o peer que enviou
                            if (CommonInfo.timePeers.get(i).getPeerIdentifier().equalsIgnoreCase(msgPeer)) {
                                //Calcula o round trip time (RTT)
                                long RTT = System.currentTimeMillis() - CommonInfo.timePeers.get(i).getTime();

                                //Calcula a diferença do relógio interno com o do peer + RTT
                                Date dateMaster = new Date();
                                Date datePeer = CommonInfo.sdf.parse(msgClock);
                                long diff = (datePeer.getTime() - dateMaster.getTime()) + RTT / 2;

                                for (Peer p : CommonInfo.publicKeys) {
                                    if (p.getIdentifier().equalsIgnoreCase(msgPeer)) {
                                        //Envia mensagem de ajuste de relógio (atrasar ou adiantar) CRIPTOGRAFADA com chave privada
                                        new UnicastMessenger(p.getPort(), "UpdateClock:" + RSACryptography.encryptMessage("Ok:" + diff, CommonInfo.peer.getPrivKey())).start();
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPeerInfo() {
        String peerInfo = "PeerInfo:" + CommonInfo.peer.getIdentifier() + "***" + CommonInfo.sdf.format(new Date());
        for (Peer p : CommonInfo.publicKeys) {
            if (p.getIdentifier().equalsIgnoreCase(CommonInfo.master)) {
                System.out.println("Poll came from the master, sending my info via unicast to peer at port " + p.getPort());
                new UnicastMessenger(p.getPort(), peerInfo).start();
                break;
            }
        }
    }
}
