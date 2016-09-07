package safenote.client.persistence;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import safenote.client.model.Instance;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class InstanceRepository {

    static String deviceId;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void init(){
        @SuppressWarnings("unchecked")
        List<Instance> instanceList = entityManager.createQuery("from " + Instance.class.getName()).getResultList();
        if(instanceList.size()!=1){
            Instance instance = new Instance();
            entityManager.persist(instance);
            deviceId = instance.getId();
            return;
        }
        deviceId = instanceList.get(0).getId();
    }
}
