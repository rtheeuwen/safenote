package nl.safenote.model;


public final class Header {

    public Header(String id, String header) {
        if(id==null||header==null)
            throw new IllegalArgumentException("Constructor parameters cannot be null");
        this.id = id;
        this.header = header;
    }

    private final String id;
    private String header;

    public String getId() {
        return id;
    }

    public String getHeader() {
        return header;
    }

    public Header setHeader(String header) {
        this.header = header;
        return this;
    }

    @Override
    public int hashCode(){
        return id.hashCode()^header.hashCode();
    }

    @Override
    public boolean equals(Object object){
        if(object == this) return true;
        if(!(object instanceof Header)) return false;
        Header other = (Header) object;
        return other.hashCode()==this.hashCode();
    }

}
