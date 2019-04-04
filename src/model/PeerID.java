package model;

import java.io.Serializable;
import java.security.PublicKey;

/**
 *
 * @author cnmoro
 */
//Representacao de um Peer
public class PeerID implements Serializable {

    String identifier;
    PublicKey publicKey;
    int port;

    public PeerID(String identifier, PublicKey publicKey, int port) {
        this.identifier = identifier;
        this.publicKey = publicKey;
        this.port = port;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
