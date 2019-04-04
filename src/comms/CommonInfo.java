package comms;

import com.google.gson.Gson;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import model.Peer;
import model.PeerID;

/**
 *
 * @author cnmoro
 */
public class CommonInfo {

    public static String master = "";
    public static Peer peer;
    public static boolean masterAlive = false;
    public static Gson gson = new Gson();
    public static DateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    public static ArrayList<PeerID> publicKeys;
    public static String host = "localhost";

    //time interval - heartbeat / keepalive
    public static final int deltaT1 = 10000; //10 s

    //time interval - clock sync
    public static final int deltaT2 = 15000; //15 s

    public static boolean isMasterDefined() {
        return !master.equals("");
    }

    public static boolean amIMaster() {
        return master.equalsIgnoreCase(CommonInfo.peer.getIdentifier());
    }
}
