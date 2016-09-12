package nl.safenote.model;


public final class Message<T> {

    private T body;
    private long expires;
    private String signature;

    public Message(T body, long expires){
        if(body==null)
            throw new IllegalArgumentException("Body cannot be null.");
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
