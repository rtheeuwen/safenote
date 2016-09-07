package safenote.client.services;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;

/**
 * Convenience class, provides a simple abstraction layer for AES encryption in CBC mode. CBC mode uses a pseudo random
 * Initialization vector to ensure the same plaintext enchiphered multiple times never results in the same ciphertext.
 * This allows for the same key to be used multiple times. PKCS7 padding is used, however java incorrectly refers to
 * this as PKCS5.
 * The initialization vector is prepended to ciphertext upon encryption so that clients need to worry about a single
 * byte[] only.
 * @Author Roel Theeuwen
 * @Verion 1.0
 * @Since 2016-09-04
 */
public abstract class AbstractAesService {

    public AbstractAesService(){

    }

    public final byte[] aesEncipher(byte[] plainText, SecretKeySpec key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            AlgorithmParameters algorithmParameters = cipher.getParameters();
            byte[] initializationVector = algorithmParameters.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] cipherText = cipher.doFinal(plainText);
            byte[] out = new byte[initializationVector.length + cipherText.length];
            System.arraycopy(initializationVector, 0, out, 0, 16);
            System.arraycopy(cipherText, 0, out, 16, cipherText.length);
            return out;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidKeyException | InvalidParameterSpecException |
                IllegalBlockSizeException | BadPaddingException e) {
            throw new SecurityException("Encryption error.");
        }
    }

    public final byte[] aesDecipher(byte[] cipherText, SecretKeySpec key){
        try {
            byte[] initializationVector = Arrays.copyOfRange(cipherText, 0, 16);
            byte[] enciphered = Arrays.copyOfRange(cipherText, 16, cipherText.length);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(initializationVector));
            return cipher.doFinal(enciphered);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidAlgorithmParameterException |
                InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            throw new AssertionError("Local data is compromised.");
        }
    }
}
