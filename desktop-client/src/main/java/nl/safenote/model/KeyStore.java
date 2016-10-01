package nl.safenote.model;


import javax.crypto.spec.SecretKeySpec;
import java.security.PrivateKey;
import java.security.PublicKey;

public final class KeyStore{

    private final SecretKeySpec aes;
    private final SecretKeySpec hmac;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;


    public KeyStore(SecretKeySpec aes, SecretKeySpec hmac, PublicKey publicKey, PrivateKey privateKey) {
        if(!(aes.getAlgorithm().equals("AES")&&hmac.getAlgorithm().equals("HmacSHA512")))
            throw new IllegalArgumentException();
        this.aes = aes;
        this.hmac = hmac;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public SecretKeySpec getAes() {
        return aes;
    }

    public SecretKeySpec getHmac() {
        return hmac;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
