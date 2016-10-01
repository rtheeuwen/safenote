package nl.safenote.model;

public final class Message<T> {

    private final T body;
    private final long expires;
    private final String signee;
    private String signature;

    public Message(T body, long expires, String signee){
        if(body==null)
            throw new IllegalArgumentException("Body cannot be null.");
        this.body = body;
        this.expires = expires;
        this.signee = signee;
    }

    public Message(long expires, String signee){
        this.expires = expires;
        this.body = null;
        this.signee = signee;
    }

    public T getBody() {
        return body;
    }

    public long getExpires() {
        return expires;
    }

    public String getSignee() {
        return signee;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
