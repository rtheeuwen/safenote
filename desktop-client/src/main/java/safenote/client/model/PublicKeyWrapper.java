package safenote.client.model;

/**
 * The generic message class cannot be used to send a public key over the network (because the server cannot verify the
 * message yet), thus this class is required.
 * @Author Roel Theeuwen
 * @Verion 1.0
 * @Since 2016-09-04
 */
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
