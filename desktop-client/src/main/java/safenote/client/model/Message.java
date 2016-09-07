package safenote.client.model;

/**
 * Provides a generic message structure which contains required metadata for making a request to the safenote server.
 * All messages sent need to be signed with the user's private key to ensure the server will only take authorized
 * requests into consideration. A message has an expiration date to ensure it cannot be stolen and resent by someone who
 * is monitoring the network.
 * @Author Roel Theeuwen
 * @Verion 1.0
 * @Since 2016-09-04
 * @param <T> type
 */
public final class Message<T> {

    private T body;
    private long expires;
    private String signature;

    public Message(T body, long expires){
        this.body = body;
        this.expires = expires;
    }

    public Message(long expires){
        this.expires = expires;
        this.body = null;
    }

    public Message() {
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
