package nl.safenote.services;

import nl.safenote.model.NoteBuilder;
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
    Note encipher(Note note);
    Note decipher(Note note, boolean headerOnly);
    String checksum(Note note);
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
    public Note encipher(Note note) {
        NoteBuilder noteBuilder = new NoteBuilder()
                .setId(note.getId())
                .setModified(note.getModified())
                .setCreated(note.getCreated())
                .setVersion(note.getVersion())
                .setContentType(note.getContentType())
                .setHeader(DatatypeConverter.printBase64Binary(super.aesEncipher(note.getHeader().getBytes(), this.AESKey)));

        String content = note.getContent();
        if(!Objects.equals(content, "")&&content!=null) {
            noteBuilder.setContent(DatatypeConverter.printBase64Binary(super.aesEncipher(note.getContent().getBytes(), this.AESKey)));
        } else {
            noteBuilder.setContent("");
        }
        Note safeNote = noteBuilder.setEncrypted(true).build();
        safeNote.setHash(checksum(safeNote));
        return safeNote;
    }

    @Override
    public Note decipher(Note note, boolean headerOnly) {
        NoteBuilder noteBuilder = new NoteBuilder()
                .setId(note.getId())
                .setHeader(new String(super.aesDecipher(DatatypeConverter.parseBase64Binary(note.getHeader()), this.AESKey)));
        if(headerOnly)
            return noteBuilder.build();

        noteBuilder.setModified(note.getModified())
                .setCreated(note.getCreated())
                .setVersion(note.getVersion())
                .setContentType(note.getContentType());

        String content = note.getContent();
        if(!Objects.equals(content, "") &&content!=null&&content.length()!=0) {
            noteBuilder.setContent(new String(super.aesDecipher(DatatypeConverter.parseBase64Binary(note.getContent()), this.AESKey)));
        } else {
            noteBuilder.setContent("");
        }
        return noteBuilder.build();
    }

    @Override
    public String checksum(Note note) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(this.HMACSecret);
            byte[] digest = mac.doFinal((note.getContent()+ note.getHeader()+ note.getId()).getBytes("ASCII"));
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
            if(message.getBody() instanceof Note) {
                Note note = (Note) message.getBody();
                signature.update((note.getContent() + note.getHeader() + message.getExpires()).getBytes());
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
