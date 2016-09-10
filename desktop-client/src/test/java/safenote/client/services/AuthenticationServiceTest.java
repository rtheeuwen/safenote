package safenote.client.services;


import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import safenote.client.testutils.TestHelper;
import safenote.client.utils.FileIO;
import safenote.client.utils.KeyUtils;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class AuthenticationServiceTest {

    @BeforeClass
    @AfterClass
    public static void cleanUp() throws IOException {
        new TestHelper().clearFileSystem();
    }

    @Mock
    CryptoService cryptoService;

    @Mock
    SynchronizationService synchronizationService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final String password = "password";

    @Test
    public void compressionIsDoneCorrectly() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AuthenticationService authenticationService = new AuthenticationServiceImpl(cryptoService, synchronizationService);
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[2000];
        random.nextBytes(bytes);

        Class[] params = new Class[1];
        params[0] = byte[].class;
        Method compress = AuthenticationServiceImpl.class.getDeclaredMethod("compress", params);
        compress.setAccessible(true);
        byte[] compressed = (byte[]) compress.invoke(authenticationService, bytes);

        Method decompress = authenticationService.getClass().getDeclaredMethod("decompress", params);
        decompress.setAccessible(true);
        byte[] decompressed = (byte[]) decompress.invoke(authenticationService, compressed);

        assertEquals(DatatypeConverter.printBase64Binary(bytes), DatatypeConverter.printBase64Binary(decompressed));
    }

    @Test
    public void storageEncryptionIsDoneCorrectly() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AuthenticationService authenticationService = new AuthenticationServiceImpl(cryptoService, synchronizationService);
        String plainText = "test";

        Class[] params = new Class[2];
        params[0] = byte[].class;
        params[1] = String.class;
        Method encipherStorage = AuthenticationServiceImpl.class.getDeclaredMethod("encipherStorage", params);
        encipherStorage.setAccessible(true);
        byte[] enciphered = (byte[]) encipherStorage.invoke(authenticationService, plainText.getBytes(), "password");

        Method decipherStorage = AuthenticationServiceImpl.class.getDeclaredMethod("decipherStorage", params);
        decipherStorage.setAccessible(true);
        byte[] deciphered = (byte[]) decipherStorage.invoke(authenticationService, enciphered, "password");

        assertEquals(plainText, new String(deciphered));
    }


    @Test
    public void generationIsDoneCorrectly() throws Exception{
        AuthenticationService authenticationService = new AuthenticationServiceImpl(cryptoService, synchronizationService);
        authenticationService.authenticate(password);
        verifyCorrectBehavior(authenticationService);
    }

    @Test
    public void authenticationIsSuccessfulWithCorrectPassword() throws Exception{
        AuthenticationService authenticationService = new AuthenticationServiceImpl(cryptoService, synchronizationService);
        authenticationService.authenticate(password);
        verifyCorrectBehavior(authenticationService);
    }

    @Test
    public void authenticationFailsWithIncorrectPassword(){
        AuthenticationService authenticationService = new AuthenticationServiceImpl(cryptoService, synchronizationService);

        exception.expect(SecurityException.class);
        authenticationService.authenticate("incorrect");
    }

    private void verifyCorrectBehavior(AuthenticationService authenticationService) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class[] params = new Class[2];
        params[0] = byte[].class;
        params[1] = String.class;
        Method decipherStorage = AuthenticationServiceImpl.class.getDeclaredMethod("decipherStorage", params);
        decipherStorage.setAccessible(true);
        byte[] deciphered = (byte[]) decipherStorage.invoke(authenticationService, FileIO.read(), password);
        Map<String, Object> keyStore = KeyUtils.keyStoreFromByteArray(deciphered);

        verify(synchronizationService).enlist(DatatypeConverter.printBase64Binary(((PublicKey) keyStore.get("publicKey")).getEncoded()));
        verify(synchronizationService).synchronize();
        verify(cryptoService).init((SecretKeySpec) keyStore.get("AES"), (SecretKeySpec) keyStore.get("HMAC"), (PrivateKey) keyStore.get("privateKey"));
    }
}