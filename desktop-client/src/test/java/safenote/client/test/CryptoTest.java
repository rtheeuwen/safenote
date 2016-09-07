package safenote.client.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import safenote.client.app.Config;
import safenote.client.model.Note;
import safenote.client.services.AbstractAesService;
import safenote.client.services.AuthenticationService;
import safenote.client.services.CryptoService;
import safenote.client.utils.KeyUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static safenote.client.test.QRTest.delete;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( classes = {fakeConfig.class})
public class CryptoTest {

    @Autowired CryptoService cryptoService;
    @Autowired AuthenticationService authenticationService;

    @Before
    public void init(){
        Map<String, Object> keyMap = KeyUtils.keyStoreFromByteArray(KeyUtils.generateKeyStore());
        cryptoService.init((SecretKeySpec) keyMap.get("AES"), (SecretKeySpec) keyMap.get("HMAC"), (PrivateKey) keyMap.get("privateKey"));
    }

    @Before
    @After
    public void clearFileSystem() throws IOException {
        delete(new File(System.getProperty("user.home") + "/.safenote"));

    }

    @Test
    public void ensureJCEIsInstalled() throws NoSuchAlgorithmException {
        assertFalse(Cipher.getMaxAllowedKeyLength("AES")==128);
    }

    @Test
    public void encryptionIsDoneCorrectly(){
        SecretKeySpec key = (SecretKeySpec) KeyUtils.keyStoreFromByteArray(KeyUtils.generateKeyStore()).get("AES");
        String test = "test";
        ConcreteAesService aes = new ConcreteAesService();
        assertEquals(test, new String(aes.aesDecipher(aes.aesEncipher(test.getBytes(), key), key)));
    }
    
    @Test
    public void noteEncryptionIsDoneCorrectly(){
        Note note = new Note();
        String header = "header";
        String content = "content";
        note.setHeader(header);
        note.setContent(content);

        note = cryptoService.decipher(cryptoService.encipher(note), false);
        assertEquals(note.getHeader(), header);
        assertEquals(note.getContent(), content);
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
    public void compressionIsDoneCorrectly() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[2000];
        random.nextBytes(bytes);

        Class[] params = new Class[1];
        params[0] = byte[].class;
        Method compress = this.authenticationService.getClass().getDeclaredMethod("compress", params);
        compress.setAccessible(true);
        byte[] compressed = (byte[]) compress.invoke(this.authenticationService, bytes);

        Method decompress = this.authenticationService.getClass().getDeclaredMethod("decompress", params);
        decompress.setAccessible(true);
        byte[] decompressed = (byte[]) decompress.invoke(this.authenticationService, compressed);

        assertEquals(DatatypeConverter.printBase64Binary(bytes), DatatypeConverter.printBase64Binary(decompressed));
    }

    @Test
    public void storageEncryptionIsDoneCorrectly() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String test = "test";

        Class[] params = new Class[2];
        params[0] = byte[].class;
        params[1] = String.class;
        Method encipherStorage = this.authenticationService.getClass().getDeclaredMethod("encipherStorage", params);
        encipherStorage.setAccessible(true);
        byte[] enciphered = (byte[]) encipherStorage.invoke(this.authenticationService, test.getBytes(), "password");

        Method decipherStorage = this.authenticationService.getClass().getDeclaredMethod("decipherStorage", params);
        decipherStorage.setAccessible(true);
        byte[] deciphered = (byte[]) decipherStorage.invoke(this.authenticationService, enciphered, "password");

        assertEquals(test, new String(deciphered));
    }

    @Test
    public void authenticationServiceIntegrationTest(){
        this.authenticationService.authenticate("test");
        this.authenticationService.authenticate("test");
    }
}

class ConcreteAesService extends AbstractAesService{}

@Configuration
class fakeConfig extends Config{
    public fakeConfig(){
        super(null);
    }
}



