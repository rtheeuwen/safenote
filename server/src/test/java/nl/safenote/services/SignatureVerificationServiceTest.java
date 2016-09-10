package nl.safenote.services;


import nl.safenote.model.Message;
import nl.safenote.model.Note;
import nl.safenote.server.model.UserPublicKey;
import nl.safenote.utils.KeyUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import nl.safenote.server.persistence.UserPublicKeyRepository;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class SignatureVerificationServiceTest {

    static CryptoService cryptoService;
    static PublicKey publicKey;
    static UserPublicKey userPublicKey;

    @BeforeClass
    public static void init(){
        cryptoService = new CryptoServiceImpl();
        Map<String, Object> keyStore = KeyUtils.keyStoreFromByteArray(KeyUtils.generateKeyStore());
        cryptoService.init((SecretKeySpec) keyStore.get("AES"), (SecretKeySpec) keyStore.get("HMAC"), (PrivateKey) keyStore.get("privateKey"));
        publicKey = (PublicKey) keyStore.get("publicKey");
        userPublicKey = new UserPublicKey(DatatypeConverter.printBase64Binary(((publicKey.getEncoded()))));
        userPublicKey.setUserId("AAAAA");
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void enlistWorksCorrectly(){
        UserPublicKeyRepository repository = Mockito.mock(UserPublicKeyRepository.class);
        when(repository.create(userPublicKey)).thenReturn(userPublicKey.getUserId());
        SignatureVerificationService service = new SignatureVerificationServiceImpl(repository);
        assertEquals(service.enlist(userPublicKey), userPublicKey.getUserId());
        verify(repository).create(userPublicKey);
    }

    @Test
    public void validMessageIsVerified(){
        UserPublicKeyRepository repository = Mockito.mock(UserPublicKeyRepository.class);
        when(repository.findOne(userPublicKey.getUserId())).thenReturn(userPublicKey);
        SignatureVerificationService service = new SignatureVerificationServiceImpl(repository);
        nl.safenote.model.Note note = new Note(UUID.randomUUID().toString(), "header");
        note.setContent("content");
        note = cryptoService.encipher(note);
        nl.safenote.model.Message<nl.safenote.model.Note> message = new Message<nl.safenote.model.Note>(note, System.currentTimeMillis()+5000);
        cryptoService.sign(message, userPublicKey.getUserId());

        nl.safenote.server.model.Message serverMessage = new nl.safenote.server.model.Message();
        serverMessage.setBody(message.getBody());
        serverMessage.setExpires(message.getExpires());
        serverMessage.setSignature(message.getSignature());
        assertEquals(service.verifySignature(serverMessage), userPublicKey.getUserId());
    }

    @Test
    public void invalidMessageIsRejected(){
        UserPublicKeyRepository repository = Mockito.mock(UserPublicKeyRepository.class);
        when(repository.findOne(userPublicKey.getUserId())).thenReturn(userPublicKey);
        SignatureVerificationService service = new SignatureVerificationServiceImpl(repository);
        nl.safenote.model.Note note = new Note(UUID.randomUUID().toString(), "header");
        note.setContent("content");
        note = cryptoService.encipher(note);
        nl.safenote.model.Message<nl.safenote.model.Note> message = new Message<nl.safenote.model.Note>(note, System.currentTimeMillis()+5000);
        cryptoService.sign(message, userPublicKey.getUserId());

        nl.safenote.server.model.Message serverMessage = new nl.safenote.server.model.Message();
        serverMessage.setBody(message.getBody());
        serverMessage.setExpires(message.getExpires());
        serverMessage.setSignature("AAAAAbad signature");
        exception.expect(SecurityException.class);
        service.verifySignature(serverMessage);
    }

    @Test
    public void expiredMessageIsRejected(){
        UserPublicKeyRepository repository = Mockito.mock(UserPublicKeyRepository.class);
        when(repository.findOne(userPublicKey.getUserId())).thenReturn(userPublicKey);
        SignatureVerificationService service = new SignatureVerificationServiceImpl(repository);
        nl.safenote.model.Note note = new Note(UUID.randomUUID().toString(), "header");
        note.setContent("content");
        note = cryptoService.encipher(note);
        nl.safenote.model.Message<nl.safenote.model.Note> message = new Message<nl.safenote.model.Note>(note, System.currentTimeMillis());
        cryptoService.sign(message, userPublicKey.getUserId());

        nl.safenote.server.model.Message serverMessage = new nl.safenote.server.model.Message();
        serverMessage.setBody(message.getBody());
        serverMessage.setExpires(message.getExpires());
        serverMessage.setSignature(message.getSignature());
        exception.expect(SecurityException.class);
        service.verifySignature(serverMessage);
    }
}
