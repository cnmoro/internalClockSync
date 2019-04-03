package model;

import java.io.Serializable;

/**
 *
 * @author cnmoro
 */

//Objeto que identifica um pacote contendo a chave publica de um peer (em string - codificada em base64)
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
