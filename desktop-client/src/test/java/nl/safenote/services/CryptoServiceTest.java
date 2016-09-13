package nl.safenote.services;

import nl.safenote.model.Quadruple;
import nl.safenote.utils.KeyUtils;
import org.junit.Test;
import nl.safenote.model.Message;
import nl.safenote.model.Note;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class CryptoServiceTest {

    private final CryptoService cryptoService = new CryptoServiceImpl();
    private final PublicKey publicKey;

    public CryptoServiceTest(){
        Quadruple<SecretKeySpec, SecretKeySpec, PrivateKey, PublicKey> keyStore = KeyUtils.keyStoreFromByteArray(KeyUtils.generateKeyStore());
        cryptoService.init(keyStore.getA(), keyStore.getB(), keyStore.getC());
        this.publicKey = keyStore.getD();
    }

    @Test
    public void noteEncryptionIsDoneCorrectly(){
        doEncryption("header", "content");
    }

    @Test
    public void noteEncryptionIsDoneCorrectlyForNoteWithoutContent(){
        doEncryption("header", "");
    }

    @Test
    public void headerOnlyDecryptionIsDoneCorrectly(){
        Note note = new Note();
        note.setHeader("header");
        note.setContent("content");

        note = cryptoService.decipher(cryptoService.encipher(note), true);
        assertEquals(note.getHeader(), "header");
        assertEquals(note.getContent(), null);
    }

    @Test
    public void checksumIsDoneCorrectly(){
        Note note = new Note();
        note.setId("id");
        note.setHeader("header");
        note.setContent("content");
        assertEquals(cryptoService.checksum(note), cryptoService.checksum(note));
    }

    @Test
    public void signingIsDoneCorrectly() throws Exception{
        Note note = new Note();
        note.setId(UUID.randomUUID().toString());
        note.setHeader("Header");
        note.setContent("content");
        note = cryptoService.encipher(note);

        Message<Note> message = new Message<>(note, 0);
        cryptoService.sign(message, "AAAAA");

        note = message.getBody();
        byte[] data = (note.getContent() + note.getHeader() + message.getExpires()).getBytes();
        byte[] claimedSignature = DatatypeConverter.parseBase64Binary(message.getSignature().substring(5));
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data);
        signature.verify(claimedSignature);
    }

    private void doEncryption(String header, String content){
        Note note = new Note();
        note.setHeader(header);
        note.setContent(content);
        note = cryptoService.decipher(cryptoService.encipher(note), false);
        assertEquals(note.getHeader(), header);
        assertEquals(note.getContent(), content);
    }
}
