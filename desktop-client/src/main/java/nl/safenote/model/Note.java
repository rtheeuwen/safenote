package nl.safenote.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Note{

    String id;
    String header;
    String content;
    String modified;
    long created;
    int version;
    NoteType noteType;

    public Note() {
        this.setCreated(System.currentTimeMillis());
    }

    public Note(String id, String header, NoteType noteType){
        if(id==null||header==null)
            throw new IllegalArgumentException();
        this.id = id;
        this.header = header;
        this.setContent("");
        this.setModified(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        this.setCreated(System.currentTimeMillis());
        this.noteType = noteType;
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

    public NoteType getNoteType() {
        return noteType;
    }

    public void setNoteType(NoteType noteType) {
        this.noteType = noteType;
    }

    public void setCreated(long created) {
        this.created = created;
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

