package nl.safenote.server.persistence;

import nl.safenote.server.model.SafeNote;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class SafeNoteRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    @SuppressWarnings("unsafe")
    private SafeNote findOne(String id, String userId) {
        //TODO // FIXME: 9/12/16
        return (SafeNote) entityManager.createQuery("from " + SafeNote.class.getName() + " WHERE id=:id AND DELETED= false AND USERID=:userId").setParameter("id", id).setParameter("userId", userId).getSingleResult();
    }

    @Transactional(readOnly = true)
    public List<SafeNote> findNotes(List<String> ids, String userId){
        return ids.stream().map(id -> findOne(id, userId)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<String, String> findChecksums(String userId) {
        //TODO // FIXME: 9/12/16
        List<SafeNote> safeNotes = entityManager.createQuery("from " + SafeNote.class.getName() + " WHERE USERID =:userId AND DELETED=false").setParameter("userId", userId).getResultList();
        return safeNotes.stream().collect(Collectors.toMap(SafeNote::getId, SafeNote::getHash));
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<String> findDeleted(String userId){
        //TODO // FIXME: 9/12/16
        List<SafeNote> safeNotes = entityManager.createQuery("from " + SafeNote.class.getName() + " WHERE USERID =:userId").setParameter("userId", userId).getResultList();
        return safeNotes.stream().filter(safeNote -> safeNote.isDeleted()).map(SafeNote::getId).collect(Collectors.toList());
    }

    @Transactional
    public void save(SafeNote safeNote){
    entityManager.merge(safeNote);


    }

    @Transactional
    public void setDelete(SafeNote safeNote) {
        safeNote.setDeleted(true);
        save(safeNote);
    }

    @Transactional
    public void delete(SafeNote safeNote) {
            entityManager.remove(safeNote);
    }

    @Transactional
    public void deleteAll(){
        entityManager.createQuery("DELETE FROM " + SafeNote.class.getName()).executeUpdate();
    }

}