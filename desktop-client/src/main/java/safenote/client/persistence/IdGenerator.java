package safenote.client.persistence;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import safenote.client.model.LastId;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@Transactional
public class IdGenerator {

    @PersistenceContext
    EntityManager entityManager;

    public String nextId(){
        List<LastId> lastIds = entityManager.createQuery("from " + LastId.class.getName()).getResultList();
        if(!lastIds.isEmpty()){
            LastId lastId = lastIds.get(0);
            LastId newId = new LastId(Long.valueOf(Long.valueOf(lastId.getId())+1).toString());
            entityManager.remove(lastId);
            entityManager.persist(newId);
            return newId.getId();
        } else {
            LastId lastId = new LastId(InstanceRepository.deviceId + 0);
            entityManager.persist(lastId);
            return lastId.getId();
        }
    }
}
