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
    //TODO photo
    //TODO // FIXME: 9/12/16 moar variaballs
    @Id
    private String id;
    private String header;

    @Lob
    private String content;
    private String modified;
    private long created;
    private String hash;

    public Note() {
        this.setCreated(System.currentTimeMillis());
    }

    public Note(String id, String header){
        this.id = id;
        this.header = header;
        this.setContent("");
        this.setModified(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        this.setCreated(System.currentTimeMillis());
        this.hash = "";
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

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public long getCreated() {
        return created;
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
    public String toString(){
        return this.hash;
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

