package safenote.client.mock;


import safenote.client.model.Message;
import safenote.client.model.Note;
import safenote.client.services.CryptoService;

import javax.crypto.spec.SecretKeySpec;
import java.security.PrivateKey;

public class CryptoServiceMock implements CryptoService{
    @Override
    public void init(SecretKeySpec aesKey, SecretKeySpec hmacSecret, PrivateKey privateKey) {

    }

    @Override
    public Note encipher(Note note) {
        return note;
    }

    @Override
    public Note decipher(Note note, boolean headerOnly) {
        return note;
    }

    @Override
    public String checksum(Note note) {
        return null;
    }

    @Override
    public Message sign(Message message, String userId) {
        return null;
    }
}
