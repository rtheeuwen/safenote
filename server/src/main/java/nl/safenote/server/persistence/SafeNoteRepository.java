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
@Transactional(readOnly = true)
public class SafeNoteRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private SafeNote findOne(String id, String userId) {
        return entityManager.createNamedQuery(SafeNote.FINDONE, SafeNote.class).setParameter("id", id).setParameter("userId", userId).getSingleResult();
    }

    public List<SafeNote> findNotes(List<String> ids, String userId){
        return ids.stream().map(id -> findOne(id, userId)).collect(Collectors.toList());
    }

    public Map<String, String> findChecksums(String userId) {
        List<SafeNote> safeNotes = entityManager.createNamedQuery(SafeNote.FINDALL, SafeNote.class).setParameter("userId", userId).getResultList();
        return safeNotes.stream().collect(Collectors.toMap(SafeNote::getId, SafeNote::getHash));
    }

    public List<String> findDeleted(String userId){
        List<SafeNote> safeNotes = entityManager.createNamedQuery(SafeNote.FINDDELETED, SafeNote.class).setParameter("userId", userId).getResultList();
        return safeNotes.stream().map(SafeNote::getId).collect(Collectors.toList());
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
    public void deleteAll(){
        entityManager.createNamedQuery(SafeNote.DELETEALL, SafeNote.class).executeUpdate();
    }

}