package safenote.client.environment;

import org.junit.Test;

import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertFalse;

public class EnvironmentTest {

    @Test
    public void ensureJCEIsInstalled() throws NoSuchAlgorithmException {
        assertFalse(Cipher.getMaxAllowedKeyLength("AES")==128);
    }
}
