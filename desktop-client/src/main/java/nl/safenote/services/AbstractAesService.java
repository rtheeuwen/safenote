package nl.safenote.services;


import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;


public abstract class AbstractAesService {

	public final byte[] aesEncipher(byte[] plainText, SecretKeySpec key) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] initializationVector = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
			byte[] cipherText = new byte[32 + plainText.length >> 4 << 4];
			System.arraycopy(initializationVector, 0, cipherText, 0, 16);
			cipher.doFinal(plainText, 0, plainText.length, cipherText, 16);
			return cipherText;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException |
				ShortBufferException | InvalidParameterSpecException | IllegalBlockSizeException e) {
			throw new AssertionError(e);
		} catch (InvalidKeyException | BadPaddingException e) {
			throw new SecurityException("Encryption error");
		}
	}

	public final byte[] aesDecipher(byte[] cipherText, SecretKeySpec key) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(cipherText, 0, 16));
			return cipher.doFinal(cipherText, 16, cipherText.length - 16);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException |
				InvalidAlgorithmParameterException | IllegalBlockSizeException e) {
			throw new AssertionError(e);
		} catch (InvalidKeyException | BadPaddingException e) {
			throw new SecurityException("Decryption error");
		}
	}

	public final String aesEncipher(String plainText, SecretKeySpec key) {
		return (DatatypeConverter.printBase64Binary(this.aesEncipher(plainText.getBytes(StandardCharsets.UTF_8), key)));
	}

	public final String aesDecipher(String cipherText, SecretKeySpec key) {
		return new String(this.aesDecipher(DatatypeConverter.parseBase64Binary(cipherText), key), StandardCharsets.UTF_8);
	}
}

