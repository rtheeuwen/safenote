package nl.safenote.server.model;

import javax.persistence.*;

@Entity
@NamedQueries({
		@NamedQuery(name = "exists", query = "SELECT COUNT(u.publicKey) FROM UserPublicKey u WHERE u.publicKey=:publicKey"),
		@NamedQuery(name = "findByPublicKey", query = "SELECT u FROM UserPublicKey u WHERE u.publicKey=:publicKey"),
		@NamedQuery(name = "lastId", query = "SELECT COUNT (u.publicKey) FROM UserPublicKey u")
})
@Access(AccessType.FIELD)
@Table(indexes = {@Index(columnList = "publicKey")})
public class UserPublicKey {

	public final static String EXISTS = "exists";
	public final static String FINDBYPUBLICKEY = "findByPublicKey";
	public final static String LASTID = "lastId";

	@Id
	private String userId;

	@Column(length = 500)
	private String publicKey;

	public UserPublicKey() {
	}

	public UserPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPublicKey() {
		return publicKey;
	}

}
