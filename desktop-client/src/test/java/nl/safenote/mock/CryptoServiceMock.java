package nl.safenote.mock;


import nl.safenote.model.SafeNote;
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
    public SafeNote encipher(Note note) {
        SafeNote safeNote = new SafeNote();
        safeNote.setContent(note.getContent());
        safeNote.setId(note.getId());
        safeNote.setHeader(note.getHeader());
        safeNote.setModified(note.getModified());
        safeNote.setCreated(note.getCreated());
        return safeNote;
    }

    @Override
    public Note decipher(SafeNote note, boolean headerOnly) {
        return note;
    }

    @Override
    public String checksum(SafeNote note) {
        return null;
    }

    @Override
    public Message sign(Message message, String userId) {
        return null;
    }
}
