package model;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 *
 * @author cnmoro
 */
//Representacao de um peer e suas informacoes
public class Peer implements Serializable {

    String status;
    transient PrivateKey privKey;
    transient PublicKey pubKey;
    String identifier;
    int port;

    public Peer(String status, PrivateKey privKey, PublicKey pubKey, String identifier, int port) {
        this.status = status;
        this.privKey = privKey;
        this.pubKey = pubKey;
        this.identifier = identifier;
        this.port = port;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
