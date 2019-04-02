package model;

import java.io.Serializable;

/**
 *
 * @author cnmoro
 */
public class KeyPacket implements Serializable {

    String identifier;
    String publicKey;

    public KeyPacket(String identifier, String publicKey) {
        this.identifier = identifier;
        this.publicKey = publicKey;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

}
