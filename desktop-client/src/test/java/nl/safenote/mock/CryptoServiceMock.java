package nl.safenote.mock;


import nl.safenote.model.Message;
import nl.safenote.model.Note;
import nl.safenote.services.CryptoService;

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
    public Note decipher(Note note) {
        return note;
    }

    @Override
    public String decipher(String header) {
        return header;
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
