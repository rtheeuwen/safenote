package safenote.client.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Entity
public class Note{

    @Id
    private String id;
    private String header;

    @Lob
    private String content;
    private int wordCount;
    private int charCount;
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
        this.setCharCount(0);
        this.setWordCount(0);
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

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getCharCount() {
        return charCount;
    }

    public void setCharCount(int charCount) {
        this.charCount = charCount;
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
        return Integer.valueOf(id.substring(6));
    }
}

