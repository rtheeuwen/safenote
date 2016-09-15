package nl.safenote.services;


import nl.safenote.server.model.Message;
import nl.safenote.server.model.SafeNote;
import nl.safenote.server.model.UserPublicKey;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import nl.safenote.server.persistence.UserPublicKeyRepository;

import javax.xml.bind.DatatypeConverter;
import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class SignatureVerificationServiceTest {

    static PrivateKey privateKey;
    static PublicKey publicKey;
    static UserPublicKey userPublicKey;

    @BeforeClass
    public static void init(){

        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            RSAKeyGenParameterSpec rsaKGenSpec = new   RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(rsaKGenSpec, random);
            KeyPair keyPair = kpg.generateKeyPair();
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new AssertionError(e);
        }
        userPublicKey = new UserPublicKey(DatatypeConverter.printBase64Binary(((publicKey.getEncoded()))));
        userPublicKey.setUserId("AAAAA");
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void enlistWorksCorrectly(){
        UserPublicKeyRepository repository = Mockito.mock(UserPublicKeyRepository.class);
        when(repository.enlist(userPublicKey)).thenReturn(userPublicKey.getUserId());
        SignatureVerificationService service = new SignatureVerificationServiceImpl(repository);
        assertEquals(service.enlist(userPublicKey), userPublicKey.getUserId());
        verify(repository).enlist(userPublicKey);
    }

    @Test
    public void validMessageIsVerified(){
        UserPublicKeyRepository repository = Mockito.mock(UserPublicKeyRepository.class);
        when(repository.findOne(userPublicKey.getUserId())).thenReturn(userPublicKey);
        SignatureVerificationService service = new SignatureVerificationServiceImpl(repository);
        SafeNote safeNote = new SafeNote(UUID.randomUUID().toString(), "header");
        safeNote.setContent("content");
        safeNote.setHash("hash");
        Message<SafeNote> message = new Message<>(safeNote, System.currentTimeMillis()+5000);
        sign(message, userPublicKey.getUserId());
        assertEquals(service.verifySignature(message), userPublicKey.getUserId());
    }

    @Test
    public void invalidMessageIsRejected(){
        UserPublicKeyRepository repository = Mockito.mock(UserPublicKeyRepository.class);
        when(repository.findOne(userPublicKey.getUserId())).thenReturn(userPublicKey);
        SignatureVerificationService service = new SignatureVerificationServiceImpl(repository);
        SafeNote safeNote = new SafeNote(UUID.randomUUID().toString(), "header");
        safeNote.setContent("content");
        safeNote.setHash("hash");
        Message<SafeNote> message = new Message<>(safeNote, System.currentTimeMillis()+5000);
        sign(message, userPublicKey.getUserId());
        message.setSignature("AAAAA bad signature");
        exception.expect(SecurityException.class);
        service.verifySignature(message);
    }

    @Test
    public void expiredMessageIsRejected(){
        UserPublicKeyRepository repository = Mockito.mock(UserPublicKeyRepository.class);
        when(repository.findOne(userPublicKey.getUserId())).thenReturn(userPublicKey);
        SignatureVerificationService service = new SignatureVerificationServiceImpl(repository);
        SafeNote safeNote = new SafeNote(UUID.randomUUID().toString(), "header");
        safeNote.setContent("content");
        safeNote.setHash("hash");
        Message<SafeNote> message = new Message<>(safeNote, System.currentTimeMillis());
        sign(message, userPublicKey.getUserId());
        exception.expect(SecurityException.class);
        service.verifySignature(message);
    }

    private Message sign(Message message, String userId) {
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
