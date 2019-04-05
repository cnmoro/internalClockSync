package comms;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import model.Peer;
import model.TimePeer;

/**
 *
 * @author cnmoro
 */
public class CommonInfo {

    public static String master = "";
    public static Peer peer;
    public static boolean masterAlive = false;
    public static DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public static ArrayList<Peer> publicKeys = new ArrayList<>();
    public static ArrayList<TimePeer> timePeers = new ArrayList<>();
    public static String host = "127.0.0.1";
    public static long clockOffset;
    public static Calendar calendar;

    //Time interval - heartbeat / keepalive
    public static final int deltaT1 = 10000; //10 s

    //Time interval - clock sync
    public static final int deltaT2 = 15000; //15 s

    public static boolean isMasterDefined() {
        return !master.equals("");
    }

    public static boolean amIMaster() {
        return master.equalsIgnoreCase(CommonInfo.peer.getIdentifier());
    }
}
