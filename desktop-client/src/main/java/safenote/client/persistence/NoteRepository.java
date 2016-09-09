package safenote.client.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import safenote.client.model.Note;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@Transactional
public class NoteRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final IdGenerator idGenerator;

    @Autowired
    public NoteRepository(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Transactional(readOnly = true)
    public Note findOne(String id) {
        return (Note) entityManager.createQuery("from " + Note.class.getName() + " WHERE id= "+id).getSingleResult();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Note> findAll() {
        return entityManager.createQuery("from " + Note.class.getName()).getResultList();
    }

    public void create(Note note) {
            entityManager.persist(note);
    }

    public Note update(Note note) {
        return entityManager.merge(note);
    }

    public void delete(Note note) {
        entityManager.remove(note);
    }

    public void delete(String id) {
        final Note note = findOne(id);
        if(note!=null)delete(note);
    }

    public void deleteAll(){
        entityManager.createQuery("DELETE FROM " + Note.class.getName()).executeUpdate();
    }

    @Transactional(readOnly = true)
    public String nextId(){
        return idGenerator.nextId();
    }
}
