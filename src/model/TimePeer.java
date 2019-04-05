package model;

/**
 *
 * @author cnmoro
 */
public class TimePeer {

    String peerIdentifier;
    long time;
    long RTT;

    public TimePeer(String peerIdentifier, long time, long RTT) {
        this.peerIdentifier = peerIdentifier;
        this.time = time;
        this.RTT = RTT;
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

    public long getRTT() {
        return RTT;
    }

    public void setRTT(long RTT) {
        this.RTT = RTT;
    }

}
