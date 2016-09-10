package nl.safenote.server.persistence;

import nl.safenote.server.model.Note;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class NoteRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    @SuppressWarnings("unsafe")
    private Note findOne(String id, String userId) {
        return (Note) entityManager.createQuery("from " + Note.class.getName() + " WHERE id=:id AND DELETED= false AND USERID=:userId").setParameter("id", id).setParameter("userId", userId).getSingleResult();
    }

    @Transactional(readOnly = true)
    public List<Note> findNotes(List<String> ids, String userId){
        return ids.stream().map(id -> findOne(id, userId)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<String, String> findChecksums(String userId) {
        List<Note> notes = entityManager.createQuery("from " + Note.class.getName() + " WHERE USERID =:userId AND DELETED=false").setParameter("userId", userId).getResultList();
        return notes.stream().collect(Collectors.toMap(Note::getId, Note::getHash));
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<String> findDeleted(String userId){
        List<Note> notes = entityManager.createQuery("from " + Note.class.getName() + " WHERE USERID =:userId").setParameter("userId", userId).getResultList();
        return notes.stream().filter(note -> note.isDeleted()).map(Note::getId).collect(Collectors.toList());
    }

    @Transactional
    public void create(Note note){
        entityManager.persist(note);
    }

    @Transactional
    public void update(Note note){
    entityManager.remove(findOne(note.getId(), note.getUserId()));
        entityManager.persist(note);


    }

    @Transactional
    public void setDelete(Note note) {
        note.setDeleted(true);
        update(note);
    }

    @Transactional
    public void delete(Note note) {
            entityManager.remove(note);
    }

    @Transactional
    public void deleteAll(){
        entityManager.createQuery("DELETE FROM " + Note.class.getName()).executeUpdate();
    }

}