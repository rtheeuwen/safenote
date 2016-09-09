package safenote.client.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class LastId {

    @Id
    private String id;

    public LastId(){

    }

    public LastId(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
