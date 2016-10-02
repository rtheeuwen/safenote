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
	public String enlist(UserPublicKey userPublicKey) {
		if (entityManager.createNamedQuery(UserPublicKey.EXISTS, Long.class).setParameter("publicKey", userPublicKey.getPublicKey()).getSingleResult() == 1L)
			return entityManager.createNamedQuery(UserPublicKey.FINDBYPUBLICKEY, UserPublicKey.class).setParameter("publicKey", userPublicKey.getPublicKey()).getSingleResult().getUserId();

		String id = entityManager.createNamedQuery(UserPublicKey.LASTID, Long.class).getSingleResult().toString();
		StringBuilder sb = new StringBuilder("1");
		int length = 4 - id.length();
		IntStream.range(0, length).forEachOrdered(i -> sb.append("0"));
		sb.append(id);
		id = sb.toString();
		userPublicKey.setUserId(id);
		entityManager.persist(userPublicKey);
		return id;
	}

	@Transactional(readOnly = true)
	public UserPublicKey findOne(String id) {
		return entityManager.find(UserPublicKey.class, id);
	}
}
