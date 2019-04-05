package model;

/**
 *
 * @author cnmoro
 */
public class TimePeer {

    String peerIdentifier;
    long time;

    public TimePeer(String peerIdentifier, long time) {
        this.peerIdentifier = peerIdentifier;
        this.time = time;
    }

    public String getPeerIdentifier() {
        return peerIdentifier;
    }

    public void setPeerIdentifier(String peerIdentifier) {
        this.peerIdentifier = peerIdentifier;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
