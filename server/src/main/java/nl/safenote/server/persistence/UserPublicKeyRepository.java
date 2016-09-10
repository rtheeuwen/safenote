package nl.safenote.server.persistence;


import nl.safenote.server.model.UserPublicKey;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.stream.IntStream;

@Repository
public class UserPublicKeyRepository {

    @PersistenceContext
    private EntityManager entityManager;


    @Transactional
    public String create(UserPublicKey userPublicKey){
        try {
            UserPublicKey existingUserPublicKey = (UserPublicKey) entityManager.createQuery("from " + UserPublicKey.class.getName() + " WHERE PUBLICKEY = '" + userPublicKey.getPublicKey() + "'").getSingleResult();
            return existingUserPublicKey.getUserId();
        }catch (Exception e){
            String id = Integer.valueOf(entityManager.createQuery("from "+UserPublicKey.class.getName()).getResultList().size()).toString();
            StringBuilder sb = new StringBuilder("1");
            int length = 4-id.length();
            IntStream.range(0, length).forEachOrdered(i -> sb.append("0"));
            sb.append(id);
            id = sb.toString();
            userPublicKey.setUserId(id);
            entityManager.persist(userPublicKey);
            return id;
        }
    }

    @Transactional(readOnly = true)
    public UserPublicKey findOne(String id){
        return (UserPublicKey) entityManager.createQuery("from " + UserPublicKey.class.getName() + " WHERE id = "+id).getSingleResult();
    }
}
