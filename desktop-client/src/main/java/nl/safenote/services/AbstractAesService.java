package nl.safenote.services;


import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


public abstract class AbstractAesService {

    protected final SecureRandom secureRandom;

    public AbstractAesService(SecureRandom secureRandom){
        assert secureRandom!=null;
        this.secureRandom = secureRandom;
    }

    public final byte[] aesEncipher(byte[] plainText, SecretKeySpec key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] nonce = new byte[12];
            secureRandom.nextBytes(nonce);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);
            byte[] cipherText = new byte[28 + plainText.length];
            System.arraycopy(nonce, 0, cipherText, 0, 12);
            cipher.doFinal(plainText, 0, plainText.length, cipherText, 12);
            return cipherText;
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException  e) {
            throw new SecurityException("Encryption error");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidAlgorithmParameterException | ShortBufferException e){
            throw new AssertionError(e);
        }
    }

    public final byte[] aesDecipher(byte[] cipherText, SecretKeySpec key){
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, cipherText, 0, 12);
            cipher.init(Cipher.DECRYPT_MODE, key,gcmParameterSpec);
            return cipher.doFinal(cipherText, 12, cipherText.length - 12);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new SecurityException("Decryption error");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidAlgorithmParameterException e){
            throw new AssertionError(e);
        }
    }

    public final String aesEncipher(String plainText, SecretKeySpec key){
        return(DatatypeConverter.printBase64Binary(this.aesEncipher(plainText.getBytes(StandardCharsets.UTF_8), key)));
    }

    public final String aesDecipher(String cipherText, SecretKeySpec key){
        return new String(this.aesDecipher(DatatypeConverter.parseBase64Binary(cipherText), key), StandardCharsets.UTF_8);
    }
}

