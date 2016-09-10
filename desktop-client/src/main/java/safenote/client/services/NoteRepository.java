package safenote.client.services;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import safenote.client.model.Note;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

public interface NoteRepository{

    Note findOne(String id);
    List<Note> findAll();
    void create(Note note);
    Note update(Note note);
    void delete(Note note);
    void delete(String id);
    void deleteAll();
    String nextId();
}

@Repository
@Transactional
class NoteRepositoryImpl implements NoteRepository{

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    @Override
    public Note findOne(String id) {
        return (Note) entityManager.createQuery("from " + Note.class.getName() + " WHERE id=:id").setParameter("id", id).getSingleResult();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    @Override
    public List<Note> findAll() {
        return entityManager.createQuery("from " + Note.class.getName()).getResultList();
    }

    @Override
    public void create(Note note) {
            entityManager.persist(note);
    }

    @Override
    public Note update(Note note) {
        return entityManager.merge(note);
    }

    @Override
    public void delete(Note note) {
        entityManager.remove(note);
    }

    @Override
    public void delete(String id) {
        final Note note = findOne(id);
        if(note!=null)delete(note);
    }

    @Override
    public void deleteAll(){
        entityManager.createQuery("DELETE FROM " + Note.class.getName()).executeUpdate();
    }

    @Override
    public String nextId(){
        return UUID.randomUUID().toString();
    }
}
