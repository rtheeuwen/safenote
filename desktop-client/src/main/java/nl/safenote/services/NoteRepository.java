package nl.safenote.services;

import nl.safenote.model.Header;
import nl.safenote.model.Note;
import org.springframework.stereotype.Repository;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.util.List;
import java.util.UUID;

public interface NoteRepository {

    Note findOne(String id);
    List<Note> findAll();
    List<Header> findHeaders();
    List<Note> findAllTextNotes();
    void create(Note... notes);
    void update(Note note);
    void delete(Note note);
    void delete(String... id);
    String nextId();
}

@Repository
class NoteRepositoryImpl implements NoteRepository{

    private final Sql2o sql2o;

    public NoteRepositoryImpl(Sql2o sql2o){
        this.sql2o = sql2o;

        String sql = "CREATE TABLE IF NOT EXISTS note (" +
                "id VARCHAR(255) NOT NULL, " +
                "content CLOB NOT NULL, " +
                "contenttype VARCHAR(255) NOT NULL, " +
                "created BIGINT NOT NULL, " +
                "hash VARCHAR(255) NOT NULL, " +
                "header VARCHAR(255) NOT NULL, " +
                "modified BIGINT NOT NULL, " +
                "version BIGINT NOT NULL, " +
                "PRIMARY KEY (id)); " +
                "CREATE INDEX IF NOT EXISTS created_index ON NOTE (created); "+
                "CREATE INDEX IF NOT EXISTS type_index ON NOTE (contenttype);";

        try(Connection connection = sql2o.open()){
            connection.createQuery(sql).executeUpdate();
        }
    }

    @Override
    public Note findOne(String id) {

        String sql = "SELECT id, content, contenttype, created, hash, header, modified, version " +
                    "FROM note WHERE id=:id";

        try(Connection connection = sql2o.open()){
            Note note = connection.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchFirst(Note.class);

            note.setEncrypted(true);
            return note;
        }
    }

    @Override
    public List<Note> findAll() {

        String sql = "SELECT id, content, contenttype, created, hash, header, modified, version " +
                    "FROM note ORDER BY created DESC";

        try(Connection connection = sql2o.open()){
            List<Note> notes = connection.createQuery(sql)
                    .executeAndFetch(Note.class);

            notes.stream().forEachOrdered(note -> note.setEncrypted(true));
            return notes;
        }
    }

    @Override
    public List<Header> findHeaders() {

        String sql = "SELECT id, header " +
                    "FROM note ORDER BY created DESC";

        try(Connection connection = sql2o.open()){
            return connection.createQuery(sql)
                    .executeAndFetch(Header.class);
        }
    }

    @Override
    public List<Note> findAllTextNotes() {

        String sql = "SELECT id, content, contenttype, created, hash, header, modified, version " +
                "FROM note WHERE contenttype=:text ORDER BY created DESC";

        try(Connection connection = sql2o.open()){
            return connection.createQuery(sql)
                    .addParameter("text", Note.ContentType.TEXT)
                    .executeAndFetch(Note.class);
        }
    }

    @Override
    public void create(Note... notes) {

        String sql = "INSERT INTO note(id, content, contenttype, created, hash, header, modified, version) " +
                "VALUES(:id, :content, :contenttype, :created, :hash, :header, :modified, :version)";

        if(notes.length==1){
            Note note = notes[0];
            isEncrypted(note);

            try(Connection connection = sql2o.open()){
                connection.createQuery(sql)
                        .bind(note)
                        .addParameter("contenttype", note.getContentType())
                        .executeUpdate();
            }

        } else {

            try(Connection connection = sql2o.beginTransaction()) {
                Query query = connection.createQuery(sql);

                for(Note note: notes){
                    isEncrypted(note);
                    query.bind(note)
                            .addParameter("contenttype", note.getContentType())
                            .addToBatch();
                }

                query.executeBatch();
                connection.commit();
            }
        }
    }

    @Override
    public void update(Note note) {
        isEncrypted(note);

        Note.ContentType contentType;

        String sql = "SELECT contenttype " +
                    "FROM NOTE " +
                    "WHERE id=:id";

        try(Connection connection=sql2o.open()) {
            contentType = Note.ContentType.valueOf(connection.createQuery(sql)
                    .addParameter("id", note.getId())
                    .executeAndFetchFirst(String.class));

            if(contentType.equals(Note.ContentType.TEXT)) {

                sql = "UPDATE note " +
                        "SET content=:content, " +
                        "hash=:hash, " +
                        "header=:header, " +
                        "modified=:modified, " +
                        "version=:version " +
                        "WHERE id=:id";

                    connection.createQuery(sql)
                            .addParameter("content", note.getContent())
                            .addParameter("hash", note.getHash())
                            .addParameter("header", note.getHeader())
                            .addParameter("modified", System.currentTimeMillis())
                            .addParameter("version", note.getVersion() + 1)
                            .addParameter("id", note.getId()).executeUpdate();

            } else
                throw new IllegalArgumentException("this type of content cannot be updated");

        }
    }

    @Override
    public void delete(Note note) {
        delete(note.getId());
    }

    @Override
    public void delete(String... ids) {

        String sql = "DELETE FROM note " +
                    "WHERE id=:id";

        if(ids.length==1) {
            try (Connection connection = sql2o.open()) {
                connection.createQuery(sql)
                        .addParameter("id", ids)
                        .executeUpdate();
            }
        } else {
            try (Connection connection = sql2o.beginTransaction()){
                Query query = connection.createQuery(sql);

                for(String id: ids){
                    query.addParameter("id", id)
                            .addToBatch();
                }

                query.executeBatch();
                connection.commit();
            }
        }
    }

    @Override
    public String nextId() {
        return UUID.randomUUID().toString();
    }

    private void isEncrypted(Note note){
        if(!note.isEncrypted())
            throw new IllegalStateException("note must be encrypted");
    }
}
