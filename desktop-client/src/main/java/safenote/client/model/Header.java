package safenote.client.model;

/**
 * This class is a sub-representation of a note. It contains just the title (header).
 * The front end contains a list of all headers. When users click on a header the complete note is fetched.
 * @Author Roel Theeuwen
 * @Verion 1.0
 * @Since 2016-09-04
 */
public final class Header {

    public Header(String id, String header) {
        this.id = id;
        this.header = header;
    }

    private final String id;
    private final String header;

    public String getId() {
        return id;
    }

    public String getHeader() {
        return header;
    }

}
