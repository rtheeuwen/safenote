package nl.safenote.services;


import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;

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
            byte[] cipherText = new byte[32 + plainText.length >> 4 << 4]; //the input byte[] is padded until the length is a multiple of 16
            byte[] initializationVector = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
            System.arraycopy(initializationVector, 0, cipherText, 0, 16);
            cipher.doFinal(plainText, 0, plainText.length, cipherText, 16);
            return cipherText;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidKeyException | InvalidParameterSpecException |
                IllegalBlockSizeException | BadPaddingException | ShortBufferException e) {
            throw new SecurityException(e);
        }
    }

    public final byte[] aesDecipher(byte[] cipherText, SecretKeySpec key){
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(cipherText, 0, 16));
            return cipher.doFinal(cipherText, 16, cipherText.length - 16);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidAlgorithmParameterException |
                InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new SecurityException(e);
        }
    }
}

