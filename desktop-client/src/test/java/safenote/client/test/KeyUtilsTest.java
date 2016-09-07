package safenote.client.test;

import org.junit.Test;
import safenote.client.utils.KeyUtils;

import javax.crypto.spec.SecretKeySpec;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import static org.junit.Assert.assertTrue;

public class KeyUtilsTest {

    @Test
    public void keysAreConvertedToByteArrayAndBackCorrectly(){
        byte[] keystore = KeyUtils.generateKeyStore();
        Map<String, Object> keyMap = KeyUtils.keyStoreFromByteArray(keystore);

        assertTrue(keyMap.get("AES") instanceof SecretKeySpec);
        assertTrue(keyMap.get("HMAC") instanceof SecretKeySpec);
        assertTrue(keyMap.get("privateKey") instanceof PrivateKey);
        assertTrue(keyMap.get("publicKey") instanceof PublicKey);
    }
}
