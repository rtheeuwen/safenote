//package nl.safenote.services;
//
//
//import nl.safenote.utils.KeyUtils;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//import nl.safenote.utils.KeyUtils;
//
//import javax.crypto.spec.SecretKeySpec;
//
//import static org.junit.Assert.assertEquals;
//
//public class AbstractAesServiceTest {
//
//    @Rule
//    public ExpectedException exception = ExpectedException.none();
//
//    @Test
//    public void encryptionIsDoneCorrectly(){
//        SecretKeySpec key = KeyUtils.keyStoreFromByteArray(KeyUtils.generateKeyStore()).getA();
//        String test = "test";
//        ConcreteAesService aes = new ConcreteAesService();
//        assertEquals(test, new String(aes.aesDecipher(aes.aesEncipher(test.getBytes(), key), key)));
//    }
//
//    @Test
//    public void decryptionFailsWithInvalidKey(){
//        SecretKeySpec key = KeyUtils.keyStoreFromByteArray(KeyUtils.generateKeyStore()).getA();
//        String test = "test";
//        ConcreteAesService aes = new ConcreteAesService();
//        byte[] enciphered = aes.aesEncipher(test.getBytes(), key);
//        key = KeyUtils.keyStoreFromByteArray(KeyUtils.generateKeyStore()).getA();
//        exception.expect(SecurityException.class);
//        aes.aesDecipher(enciphered, key);
//    }
//
//    class ConcreteAesService extends AbstractAesService{
//
//    }
//}
