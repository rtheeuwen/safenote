package nl.safenote.services;

import nl.safenote.model.SafeNote;
import org.springframework.stereotype.Service;
import nl.safenote.model.Message;
import nl.safenote.model.Note;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Objects;


public interface CryptoService{
    void init(SecretKeySpec aesKey, SecretKeySpec hmacSecret, PrivateKey privateKey);
    SafeNote encipher(Note note);
    Note decipher(SafeNote safeNote, boolean headerOnly);
    String checksum(SafeNote note);
    Message sign(Message message, String userId);
}

/**
 * Most important nl.safenote.service class
 * Provides full client side encryption for notes:
 * - AES encryption Purpose: ensures only authorized people can read notes
 * - HMAC generation, locally generated checksum which is stored on the server. Recalculated whenever a remote note is
 *   received and compared to remotely stored checksum. Purpose: ensure integrity of data received from remote
 *   (untrusted) server
 * - RSA signing Purpose: make it harder to do a MITM attack. Server verifies signature with user's public key.
 * @Author Roel Theeuwen
 * @Verion 1.0
 * @Since 2016-09-04
 */
@Service
class CryptoServiceImpl extends AbstractAesService implements CryptoService {

    private SecretKeySpec AESKey;
    private SecretKeySpec HMACSecret;
    private PrivateKey privateKey;

    @Override
    public void init(SecretKeySpec aesKey, SecretKeySpec hmacSecret, PrivateKey privateKey) {
        assert aesKey!=null&&hmacSecret!=null&&privateKey!=null;
        if(!Objects.equals(aesKey.getAlgorithm(), "AES")||!Objects.equals(hmacSecret.getAlgorithm(), "HmacSHA256"))
            throw new IllegalArgumentException("Invalid keys");
        this.AESKey = aesKey;
        this.HMACSecret = hmacSecret;
        this.privateKey = privateKey;
    }

    @Override
    public SafeNote encipher(Note note) {
        SafeNote safeNote = new SafeNote();
        safeNote.setId(note.getId());
        safeNote.setModified(note.getModified());
        safeNote.setCreated(note.getCreated());
        safeNote.setVersion(note.getVersion());
        safeNote.setNoteType(note.getNoteType());
        safeNote.setHeader(DatatypeConverter.printBase64Binary(super.aesEncipher(note.getHeader().getBytes(), this.AESKey)));
        String content = note.getContent();
        if(!Objects.equals(content, "")&&content!=null) {
            safeNote.setContent(DatatypeConverter.printBase64Binary(super.aesEncipher(note.getContent().getBytes(), this.AESKey)));
        } else {
            safeNote.setContent("");
        }
        safeNote.setHash(checksum(safeNote));
        return safeNote;
    }

    @Override
    public Note decipher(SafeNote safeNote, boolean headerOnly) {
        Note note = new Note();
        note.setId(safeNote.getId());
        note.setHeader(new String(super.aesDecipher(DatatypeConverter.parseBase64Binary(safeNote.getHeader()), this.AESKey)));
        if(headerOnly) return note;
        note.setModified(safeNote.getModified());
        note.setCreated(safeNote.getCreated());
        note.setVersion(safeNote.getVersion());
        note.setNoteType(safeNote.getNoteType());
        String content = safeNote.getContent();
        if(!Objects.equals(content, "") &&content!=null&&content.length()!=0) {
            note.setContent(new String(super.aesDecipher(DatatypeConverter.parseBase64Binary(safeNote.getContent()), this.AESKey)));
        } else {
            note.setContent("");
        }
        return note;
    }

    @Override
    public String checksum(SafeNote safeNote) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(this.HMACSecret);
            byte[] digest = mac.doFinal((safeNote.getContent()+ safeNote.getHeader()+ safeNote.getId()).getBytes("ASCII"));
            return DatatypeConverter.printHexBinary(digest);
        } catch(NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message sign(Message message, String userId) {
        if(userId==null)throw new SecurityException("No user ID yet");
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(this.privateKey);
            if(message.getBody() instanceof SafeNote) {
                SafeNote safeNote = (SafeNote) message.getBody();
                signature.update((safeNote.getContent() + safeNote.getHeader() + message.getExpires()).getBytes());
            } else {
                signature.update(Long.valueOf(message.getExpires()).toString().getBytes());
            }
            message.setSignature(userId + DatatypeConverter.printBase64Binary(signature.sign()));
            return message;
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }
}
