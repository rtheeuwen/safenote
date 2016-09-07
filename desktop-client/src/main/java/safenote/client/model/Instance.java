package safenote.client.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.stream.IntStream;

/**
 * When persisting a note, the primary key should be unique in the database. To achieve this a sequence is used, which
 * generates a new id by incrementing the previous id with 1. When users have multiple devices, every device, or,
 * application 'instance', should use * a unique sequence in order to prevent duplicate ids for notes with different
 * origins. This class generates a random number which is used as the starting point of the sequence. The ids of all
 * notes are prefixed with this random number.
 * @Author Roel Theeuwen
 * @Verion 1.0
 * @Since 2016-09-04
 */
@Entity
public final class Instance {

    @Id
    private final String id;

    public Instance() {
        StringBuilder sb = new StringBuilder("");
        IntStream.range(0, 10).forEach(i -> sb.append(Integer.valueOf((int)(Math.random()*10)).toString()));
        String id = sb.toString();
        if(id.charAt(0)=='0') id = "1"+id.substring(1);
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
