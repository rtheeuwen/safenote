package nl.safenote.services;

import nl.safenote.model.SafeNote;
import nl.safenote.model.Note;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

public interface SafeNoteRepository {

    SafeNote findOne(String id);
    List<SafeNote> findAll();
    void create(SafeNote safeNote);
    Note update(SafeNote safeNote);
    void delete(SafeNote safeNote);
    void delete(String id);
    void deleteAll();
    String nextId();
}

@Repository
@Transactional
class SafeNoteRepositoryImpl implements SafeNoteRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    @Override
    public SafeNote findOne(String id) {
        return entityManager.createNamedQuery("findById", SafeNote.class).setParameter("id", id).getSingleResult();
    }

    @Transactional(readOnly = true)
    @Override
    public List<SafeNote> findAll() {
        return entityManager.createNamedQuery("findAll", SafeNote.class).getResultList();
    }

    @Override
    public void create(SafeNote safeNote) {
            entityManager.persist(safeNote);
    }

    @Override
    public Note update(SafeNote safeNote) {
        return entityManager.merge(safeNote);
    }

    @Override
    public void delete(SafeNote safeNote) {
        entityManager.remove(safeNote);
    }

    @Override
    public void delete(String id) {
        final SafeNote safeNote = findOne(id);
        if(safeNote !=null)delete(safeNote);
    }

    @Override
    public void deleteAll(){
        entityManager.createNamedQuery("deleteAll", SafeNote.class).executeUpdate();
    }

    @Override
    public String nextId(){
        return UUID.randomUUID().toString();
    }
}
