package model;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 *
 * @author cnmoro
 */
//Representacao de um Peer
public class Peer implements Serializable {

    PrivateKey privKey;
    PublicKey pubKey;
    String identifier;
    int port;

    public Peer(PrivateKey privKey, PublicKey pubKey, String identifier, int port) {
        this.privKey = privKey;
        this.pubKey = pubKey;
        this.identifier = identifier;
        this.port = port;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public PrivateKey getPrivKey() {
        return privKey;
    }

    public void setPrivKey(PrivateKey privKey) {
        this.privKey = privKey;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

    public void setPubKey(PublicKey pubKey) {
        this.pubKey = pubKey;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
