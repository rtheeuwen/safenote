package nl.safenote.services;

import org.springframework.stereotype.Service;
import nl.safenote.model.Message;
import nl.safenote.model.Note;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Objects;


public interface CryptoService{
    void init(SecretKeySpec aesKey, SecretKeySpec hmacSecret, PrivateKey privateKey);
    Note encipher(Note note);
    Note decipher(Note note);
    String decipher(String header);
    String checksum(Note note);
    Message sign(Message message, String userId);
}

@Service
class CryptoServiceImpl extends AbstractAesService implements CryptoService {

    private SecretKeySpec AESKey;
    private SecretKeySpec HMACSecret;
    private PrivateKey privateKey;

    @Override
    public void init(SecretKeySpec aesKey, SecretKeySpec hmacSecret, PrivateKey privateKey) {
        assert aesKey!=null&&hmacSecret!=null&&privateKey!=null;
        if(!Objects.equals(aesKey.getAlgorithm(), "AES")||!Objects.equals(hmacSecret.getAlgorithm(), "HmacSHA512"))
            throw new IllegalArgumentException("Invalid keys");
        this.AESKey = aesKey;
        this.HMACSecret = hmacSecret;
        this.privateKey = privateKey;
    }

    @Override
    public Note encipher(Note note) {
        String header = note.getHeader();
        if(!Objects.equals(header, "")&&header!=null)
            note.setHeader(super.aesEncipher(header, this.AESKey));

        String content = note.getContent();
        if(!Objects.equals(content, "")&&content!=null)
            note.setContent(super.aesEncipher(content, this.AESKey));

        note.setHash(checksum(note));
        note.setEncrypted(true);
        return note;
    }

    @Override
    public Note decipher(Note note) {
        if(!note.getHash().equals(checksum(note)))
            throw new SecurityException("Checksum does not match");

        String header = note.getHeader();
        if(!Objects.equals(header, "") &&header!=null)
            note.setHeader(super.aesDecipher(header, this.AESKey));

        String content = note.getContent();
        if(!Objects.equals(content, "") &&content!=null)
            note.setContent(super.aesDecipher(content, this.AESKey));
        return note;
    }

    @Override
    public String decipher(String header) {
        if(!Objects.equals(header, "")&&header!=null)
            return super.aesDecipher(header, this.AESKey);
        else
            return header;
    }

    @Override
    public String checksum(Note note) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(this.HMACSecret);
            return DatatypeConverter.printBase64Binary(mac.doFinal((
                    note.getContent() +
                    note.getHeader() +
                    note.getId() +
                    note.getContentType())
                    .getBytes(StandardCharsets.UTF_8)));
        } catch(NoSuchAlgorithmException | InvalidKeyException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message sign(Message message, String userId) {
        if(userId==null)throw new SecurityException("No user ID yet");
        try {
            Signature signature = Signature.getInstance("SHA512withRSA");
            signature.initSign(this.privateKey);
            Object object = message.getBody();
            if(object instanceof Note) {
                Note note = (Note) object;
                signature.update((
                        note.getContent() +
                        note.getHeader() +
                        message.getExpires()).getBytes(StandardCharsets.UTF_8));
            } else {
                signature.update(Long.valueOf(message.getExpires()).toString().getBytes(StandardCharsets.UTF_8));
            }
            message.setSignature(userId + DatatypeConverter.printBase64Binary(signature.sign()));
            return message;
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }
}
