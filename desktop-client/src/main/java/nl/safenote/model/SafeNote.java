package nl.safenote.model;


import javax.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name = "findById", query = "SELECT n FROM SafeNote n WHERE n.id = :id"),
        @NamedQuery(name = "findAll", query = "FROM SafeNote"),
        @NamedQuery(name = "deleteAll", query = "DELETE FROM SafeNote")
})
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "id")),
        @AttributeOverride(name = "content", column = @Column(name = "content")),
        @AttributeOverride(name = "header", column = @Column(name = "header")),
        @AttributeOverride(name = "modified", column = @Column(name = "modified")),
        @AttributeOverride(name = "created", column = @Column(name = "created")),
        @AttributeOverride(name = "noteType", column = @Column(name = "noteType")),
        @AttributeOverride(name = "version", column = @Column(name = "version"))
})
public class SafeNote extends Note{

    String hash;

    @Override
    @Id
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    @Lob
    public String getContent() {
        return content;
    }

    @Override
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public void setHeader(String header) {
        this.header = header;
    }

    @Override
    public String getModified() {
        return modified;
    }

    @Override
    public void setModified(String modified) {
        this.modified = modified;
    }

    @Override
    public long getCreated() {
        return created;
    }

    @Override
    public void setCreated(long created) {
        this.created = created;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public NoteType getNoteType() {
        return noteType;
    }

    @Override
    public void setNoteType(NoteType noteType) {
        this.noteType = noteType;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
