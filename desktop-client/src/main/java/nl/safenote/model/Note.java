package nl.safenote.model;

import javax.persistence.*;


@Entity
@Access(AccessType.FIELD)
@NamedQueries({
        @NamedQuery(name = "findAll", query = "SELECT n FROM Note n ORDER BY n.created DESC"),
        @NamedQuery(name = "findAllTextNotes", query = "SELECT n FROM Note n WHERE n.contentType = 'TEXT'"),
        @NamedQuery(name = "deleteAll", query = "DELETE FROM Note"),
        @NamedQuery(name = "getContentType", query = "SELECT n.contentType FROM Note n WHERE n.id=:id"),
        @NamedQuery(name = "getHeaders", query = "SELECT n.id, n.header FROM Note n ORDER BY n.created DESC")
})
@Table(indexes = {
    @Index(columnList = "created"),
    @Index(columnList = "contentType")
})
public class Note implements Cloneable{

    public final static String FINDALL = "findAll";
    public final static String FINDALLTEXTNOTES = "findAllTextNotes";
    public final static String DELETEALL = "deleteAll";
    public final static String GETCONTENTTYPE = "getContentType";
    public final static String GETHEADERS = "getHeaders";
    public final static String NEWNOTEHEADER = "New note...";

    public enum ContentType {TEXT, IMAGE}

    @Id
    @Column(updatable = false)
    private String id;
    @Column(nullable = false)
    private String header;

    @Lob
    @Column(nullable = false)
    private String content;

    @Transient
    private boolean encrypted;

    @Column(nullable = false)
    private long modified;

    @Column(updatable = false, nullable = false)
    private long created;

    @Column(nullable = false)
    private long version;

    @Column(updatable = false, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ContentType contentType;

    @Column(nullable = false)
    private String hash;

    @PrePersist
    private void setCreated(){
        if(this.created==0L)
            this.created = this.modified = System.currentTimeMillis();
    }

    @PreUpdate
    private void setModified(){
        this.modified = System.currentTimeMillis();
        this.version++;
    }

    public Note() {

    }

    public Object clone(){
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
    public Note(String id, ContentType contentType){
        if(id==null||contentType==null)
            throw new IllegalArgumentException("Constructor parameter cannot be null");
        this.id = id;
        this.header = "";
        this.content = "";
        this.contentType = contentType;
    }

    public void updateHeader(){
        if(encrypted)
            throw new IllegalArgumentException("note is encrypted");
        int index = content.indexOf("\n");
        index = index!=-1?index:content.indexOf(" ");
        index = index!=-1?index:content.length()<=10?content.length():10;
        index = index>35?35:index;
        header = content.substring(0, index);
    }

    public String getId() {
        return id;
    }

    public String getHeader() {
        if(encrypted)
            return header;
        else
            return header.equals("")? NEWNOTEHEADER :header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public long getModified() {
        return modified;
    }

    public long getCreated() {
        return created;
    }

    public long getVersion() {
        return version;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public int hashCode(){
        return 13^id.hashCode()^content.hashCode()^header.hashCode();
    }

    @Override
    public boolean equals(Object object){
        if(object==this) return true;
        if(!(object instanceof Note)) return false;
        Note other = (Note) object;
        return (other.id.equals(this.id)&&other.content.equals(this.content)&&(other.header.equals(this.header)));
    }
}



