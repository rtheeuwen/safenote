package nl.safenote.model;


public class NoteBuilder {

    private Note note;

    public NoteBuilder(){
        note = new Note();
    }

    public NoteBuilder setId(String id){
        note.setId(id);
        return this;
    }

    public NoteBuilder setHeader(String header) {
        note.setHeader(header);
        return this;
    }

    public NoteBuilder setContent(String content) {
        note.setContent(content);
        return this;
    }

    public NoteBuilder setEncrypted(boolean encrypted) {
        note.setEncrypted(encrypted);
        return this;
    }

    public NoteBuilder setModified(String modified) {
        note.setModified(modified);
        return this;
    }

    public NoteBuilder setVersion(int version) {
        note.setVersion(version);
        return this;
    }

    public NoteBuilder setContentType(ContentType contentType) {
        note.setContentType(contentType);
        return this;
    }

    public NoteBuilder setCreated(long created) {
        note.setCreated(created);
        return this;
    }

    public NoteBuilder setHash(String hash) {
        note.setHash(hash);
        return this;
    }

    public Note build(){
        return note;
    }

}
