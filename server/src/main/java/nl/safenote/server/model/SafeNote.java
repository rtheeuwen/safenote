package nl.safenote.server.model;



import com.google.gson.annotations.Expose;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Entity
@IdClass(SafeNote.PrimaryKey.class)
public class SafeNote {

    static class PrimaryKey implements Serializable{
        private String id;
        private String userId;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }

    public static enum ContentType {TEXT, IMAGE}

    @Id
    @Column(updatable = false)
    private String id;

    @Id
    @Column(updatable = false)
    @Expose(serialize = false, deserialize = false)
    private String userId;
    private String header;

    @Lob
    private String content;
    private String modified;

    @Column(updatable = false)
    private long created;
    private int version;

    @Column(updatable = false)
    @Enumerated(value = EnumType.STRING)
    private ContentType contentType;
    private String hash;

    @Expose(serialize = false, deserialize = false)
    private boolean deleted;

    public SafeNote() {
        this.setCreated(System.currentTimeMillis());
    }

    public SafeNote(String id, String header){
        this.id = id;
        this.header = header;
        this.setContent("");
        this.setModified(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        this.hash = "";
        this.deleted = false;
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

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString(){
        return this.hash;
    }

    @Override
    public int hashCode(){
        return Integer.valueOf(id.substring(6));
    }
}

