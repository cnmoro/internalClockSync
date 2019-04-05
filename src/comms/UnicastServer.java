package comms;

import crypto.RSACryptography;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import model.Peer;
import model.TimePeer;

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
                                    updateClock(diff);
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
                        for (int i = 0; i < CommonInfo.timePeersInstant.size(); i++) {
                            //Este é o peer que enviou
                            if (CommonInfo.timePeersInstant.get(i).getPeerIdentifier().equalsIgnoreCase(msgPeer)) {
                                //Calcula o round trip time (RTT)
                                long RTT = System.currentTimeMillis() - CommonInfo.timePeersInstant.get(i).getTime();

                                Date datePeer = CommonInfo.sdf.parse(msgClock);
                                
                                //Armazenar em timePeersAvg os relogios de cada peer recebido e seu respectivo RTT
                                CommonInfo.timePeersAvg.add(new TimePeer(
                                        CommonInfo.timePeersInstant.get(i).getPeerIdentifier(),
                                        datePeer.getTime(),
                                        RTT)
                                );

                                //No recebimento do último relógio (PeerInfo), fazer a média entre todos (inclusive mestre)
                                if (CommonInfo.timePeersAvg.size() == CommonInfo.publicKeys.size() - 1) {
                                    long clocksAvg = 0;

                                    for (TimePeer tpa : CommonInfo.timePeersAvg) {
                                        clocksAvg += tpa.getTime();
                                    }

                                    clocksAvg = (clocksAvg + CommonInfo.calendar.getTime().getTime()) / 4;

                                    //Atualizar o relógio do mestre e enviar mensagem de atualização de relógio para os escravos
                                    updateClock(CommonInfo.calendar.getTime().getTime() - clocksAvg);

                                    for (Peer p : CommonInfo.publicKeys) {
                                        //Encontra o TimePeer deste Peer
                                        for (TimePeer tpa : CommonInfo.timePeersAvg) {
                                            if (tpa.getPeerIdentifier().equalsIgnoreCase(p.getIdentifier())) {
                                                //Envia mensagem de ajuste de relógio (atrasar ou adiantar) CRIPTOGRAFADA com chave privada
                                                long diff = (tpa.getTime() - clocksAvg) + tpa.getRTT() / 2;
                                                new UnicastMessenger(p.getPort(), "UpdateClock:" + RSACryptography.encryptMessage("Ok:" + diff, CommonInfo.peer.getPrivKey())).start();
                                                break;
                                            }
                                        }
                                    }
                                    CommonInfo.timePeersAvg = new ArrayList<>();
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

    void sendPeerInfo() {
        String peerInfo = "PeerInfo:" + CommonInfo.peer.getIdentifier() + "***" + CommonInfo.sdf.format(CommonInfo.calendar.getTime());
        for (Peer p : CommonInfo.publicKeys) {
            if (p.getIdentifier().equalsIgnoreCase(CommonInfo.master)) {
                System.out.println("Poll came from the master, sending my info via unicast to peer at port " + p.getPort());
                new UnicastMessenger(p.getPort(), peerInfo).start();
                break;
            }
        }
    }

    void updateClock(long ms) {
        CommonInfo.calendar.add(Calendar.MILLISECOND, (int) ms * (-1));
        System.out.println("Current updated time: " + CommonInfo.sdf.format(CommonInfo.calendar.getTime()));
    }
}
