package nl.safenote.services;

import org.springframework.stereotype.Service;
import nl.safenote.model.Message;
import nl.safenote.model.Note;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
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
        note.setHeader(DatatypeConverter.printBase64Binary(super.aesEncipher(note.getHeader().getBytes(StandardCharsets.UTF_8), this.AESKey)));
        String content = note.getContent();
        if(!Objects.equals(content, "")&&content!=null) {
            note.setContent(DatatypeConverter.printBase64Binary(super.aesEncipher(note.getContent().getBytes(StandardCharsets.UTF_8), this.AESKey)));
        }
        note.setHash(checksum(note));
        note.setEncrypted(true);
        return note;
    }

    @Override
    public Note decipher(Note note) {
        note.setHeader(new String(super.aesDecipher(DatatypeConverter.parseBase64Binary(note.getHeader()), this.AESKey), StandardCharsets.UTF_8));
        String content = note.getContent();
        if(!Objects.equals(content, "") &&content!=null) {
            note.setContent(new String(super.aesDecipher(DatatypeConverter.parseBase64Binary(note.getContent()), this.AESKey), StandardCharsets.UTF_8));
        }
        return note;
    }

    @Override
    public String decipher(String header) {
        return new String(super.aesDecipher(DatatypeConverter.parseBase64Binary(header), this.AESKey), StandardCharsets.UTF_8);
    }

    @Override
    public String checksum(Note note) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(this.HMACSecret);
            return DatatypeConverter.printBase64Binary(mac.doFinal(new StringBuilder()
                    .append(note.getContent())
                    .append(note.getHeader())
                    .append(note.getId())
                            .toString().getBytes(StandardCharsets.UTF_8)));
        } catch(NoSuchAlgorithmException | InvalidKeyException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message sign(Message message, String userId) {
        if(userId==null)throw new SecurityException("No user ID yet");
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(this.privateKey);
            Object object = message.getBody();
            if(object instanceof Note) {
                Note note = (Note) object;
                signature.update(new StringBuilder()
                        .append(note.getContent())
                        .append(note.getHeader())
                        .append(message.getExpires())
                        .toString().getBytes(StandardCharsets.UTF_8));
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
