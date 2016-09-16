package nl.safenote.model;

/**
 * This class is a sub-representation of a note. It contains just the title (header).
 * The front end contains a list of all headers. When users click on a header the complete note is fetched.
 * @Author Roel Theeuwen
 * @Verion 1.0
 * @Since 2016-09-04
 */
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

    public void setHeader(String header) {
        this.header = header;
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
