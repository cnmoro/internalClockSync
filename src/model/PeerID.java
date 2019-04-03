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

    public PeerID(String identifier, PublicKey publicKey) {
        this.identifier = identifier;
        this.publicKey = publicKey;
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

}
