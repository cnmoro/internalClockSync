package comms;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author cnmoro
 */
public class UnicastServer extends Thread {

    ServerSocket server;

    public UnicastServer() {
        try {
            this.server = new ServerSocket(CommonInfo.peer.getPort());
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

                if (msg != null) {
                    if (msg.contains("SendingClockPoll:")) {
                        String m = msg.substring(msg.indexOf(":"), msg.length());
                        if (m.equalsIgnoreCase(CommonInfo.master)) {
                            //Se o poll estiver vindo do mestre, envia informação do relógio interno
                            //TODO
                        }
                    } else if (msg.contains("UpdateClock:")) {
                        //Cortar string e obter informação (atrasar/adiantar) e realizar ação
                        //TODO
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
