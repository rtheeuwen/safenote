package nl.safenote.server.model;

import javax.persistence.NamedQueries;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({
    @NamedQuery(name = "exists", query = "SELECT COUNT(u.publicKey) FROM UserPublicKey u WHERE u.publicKey=:publicKey"),
    @NamedQuery(name = "findByPublicKey", query = "SELECT u FROM UserPublicKey u WHERE u.publicKey=:publicKey"),
    @NamedQuery(name = "lastId", query = "SELECT COUNT (u.publicKey) FROM UserPublicKey u")
})
public class UserPublicKey {

    public final static transient String EXISTS ="exists";
    public final static transient String FINDBYPUBLICKEY ="findByPublicKey";
    public final static transient String LASTID = "lastId";

    @Id
    private String userId;

    @Column(length = 500)
    private String publicKey;

    public UserPublicKey() {
    }

    public UserPublicKey(String publicKey){
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

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
