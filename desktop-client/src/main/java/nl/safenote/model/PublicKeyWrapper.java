package nl.safenote.model;


public class PublicKeyWrapper {

    private String publicKey;

    public PublicKeyWrapper(String publicKey){
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
