package nl.safenote.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@NamedQueries({
        @NamedQuery(name = "findById", query = "SELECT n FROM Note n WHERE n.id = :id"),
        @NamedQuery(name = "findAll", query = "FROM Note"),
        @NamedQuery(name = "deleteAll", query = "DELETE FROM Note")
})
public class Note{

    @Id
    private String id;
    private String header;

    @Lob
    private String content;
    private transient boolean encrypted;
    private String modified;
    private long created;
    private int version;
    private ContentType contentType;
    private String hash;

    public Note() {

    }

    public Note(String id, String header, ContentType contentType){
        if(id==null||header==null)
            throw new IllegalArgumentException();
        this.id = id;
        this.header = header;
        this.setContent("");
        this.setModified(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        this.setCreated(System.currentTimeMillis());
        this.contentType = contentType;
        this.version = 1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHeader() {
        return header;
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

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public long getCreated() {
        return created;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public int hashCode(){
        return id.hashCode() ^ modified.hashCode();
    }

    @Override
    public boolean equals(Object object){
        if(object==this) return true;
        if(!(object instanceof Note)) return false;
        Note other = (Note) object;
        if(other.hashCode()!=this.hashCode()) return false;
        return (other.content.equals(this.content)&&(other.header.equals(this.header)));
    }
}

