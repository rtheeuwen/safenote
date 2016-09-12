package nl.safenote.utilstest;

import nl.safenote.model.Quadruple;
import nl.safenote.utils.KeyUtils;
import org.junit.Test;
import nl.safenote.utils.KeyUtils;

import javax.crypto.spec.SecretKeySpec;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import static org.junit.Assert.assertTrue;

public class KeyUtilsTest {

    @Test
    public void keysAreConvertedToByteArrayAndBackCorrectly(){
        byte[] keystore = KeyUtils.generateKeyStore();
        Quadruple<SecretKeySpec, SecretKeySpec, PrivateKey, PublicKey> quadruple = KeyUtils.keyStoreFromByteArray(KeyUtils.generateKeyStore());

    }
}
