package comms;

import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author Carlo
 */
public class UnicastMessenger extends Thread {

    Socket socket;
    int port;
    String msg;

    public UnicastMessenger(int port, String msg) {
        this.port = port;
        this.msg = msg;
    }

    public void run() {
        try {
            this.socket = new Socket(CommonInfo.host, port);
            OutputStream output = socket.getOutputStream();
            output.write(msg.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
