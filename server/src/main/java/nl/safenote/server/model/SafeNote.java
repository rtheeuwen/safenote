package nl.safenote.server.model;



import com.google.gson.annotations.Expose;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NamedQueries({
    @NamedQuery(name="findOne", query = "SELECT n from SafeNote n WHERE n.id=:id AND n.userId=:userId AND DELETED= false"),
    @NamedQuery(name="findAll", query = "FROM SafeNote n WHERE n.userId=:userId AND deleted=false"),
    @NamedQuery(name="findDeleted", query = "FROM SafeNote n WHERE n.userId=:userId AND deleted=true") ,
    @NamedQuery(name = "deleteAll", query = "DELETE FROM SafeNote"),
})
@Entity
@Access(AccessType.FIELD)
@IdClass(SafeNote.PrimaryKey.class)
@Table(indexes = {@Index(columnList = "deleted")})
public class SafeNote {

    public final static String FINDONE = "findOne";
    public final static String FINDALL = "findAll";
    public final static String FINDDELETED = "findDeleted";
    public final static String DELETEALL = "deleteAll";
    public final static String FINDCHECKSUMS = "findCheckSums";

    @Access(AccessType.FIELD)
    static class PrimaryKey implements Serializable{
        private String id;
        private String userId;

        @Override
        public int hashCode(){
            return 13^id.hashCode()^userId.hashCode();
        }

        @Override
        public boolean equals(Object object){
            if(object==this)return true;
            if(!(object instanceof SafeNote.PrimaryKey)) return false;
            SafeNote.PrimaryKey other = (SafeNote.PrimaryKey)object;
            return other.id.equals(this.id)&&other.userId.equals(this.userId);
        }
    }

    private enum ContentType {TEXT, IMAGE}

    @Id
    @Column(updatable = false)
    private String id;

    @Id
    @Column(updatable = false)
    @Expose(serialize = false, deserialize = false)
    private String userId;

    @Column(nullable = false)
    private String header;

    @Lob
    @Column(nullable = false)
    private String content;
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

    @Expose(serialize = false, deserialize = false)
    @Column(nullable = false)
    private boolean deleted;

    public SafeNote() {

    }

    public String getId() {
        return id;
    }

    public String getHeader() {
        return header;
    }

    public String getContent() {
        return content;
    }

    public String getHash() {
        return hash;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
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
        return 13^id.hashCode()^userId.hashCode()^header.hashCode()^content.hashCode();
    }

    @Override
    public boolean equals(Object object){
        if(object==this) return true;
        if(!(object instanceof SafeNote)) return false;
        SafeNote other = (SafeNote) object;
        return (other.id.equals(this.id)&&other.userId.equals(this.userId)&&other.content.equals(this.content)&&(other.header.equals(this.header)));
    }
}

