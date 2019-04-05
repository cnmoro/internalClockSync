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
            System.out.println("Trying to send '" + msg + "' to " + CommonInfo.host + " at " + port);
            this.socket = new Socket(CommonInfo.host, port);
            OutputStream output = socket.getOutputStream();
            output.write(msg.getBytes());
            this.socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
