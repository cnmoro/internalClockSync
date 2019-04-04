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
    int port;

    public KeyPacket(String identifier, String publicKey, int port) {
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

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
