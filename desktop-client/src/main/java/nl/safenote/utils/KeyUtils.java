package nl.safenote.utils;

import nl.safenote.model.Quadruple;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public final class KeyUtils {

    private KeyUtils() {
        throw new AssertionError();
    }

    private static PrivateKey decodePrivateKey(byte[] encoded) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private static PublicKey decodePublicKey(byte[] encoded) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    public static byte[] generateKeyStore(SecureRandom secureRandom) {
        KeyPair keyPair = generateRsaKeyPair(secureRandom);
        return keyStoreToByteArray(generateAesKey(), generateHmacKey(), keyPair.getPrivate(), keyPair.getPublic());
    }

    private static byte[] keyStoreToByteArray(SecretKeySpec aes, SecretKeySpec hmac, PrivateKey privateKey, PublicKey publicKey) {
        byte[] privateBytes = privateKey.getEncoded();
        return new ByteStream(32, 64, 294, privateBytes.length)
                        .write(aes.getEncoded()) //32 bytes
                        .write(hmac.getEncoded()) //64 bytes
                        .write(publicKey.getEncoded()) //294 bytes
                        .write(privateBytes) //variable length 1218 or 1217 bytes
                        .read();
    }

    public static Quadruple<SecretKeySpec, SecretKeySpec, PublicKey, PrivateKey> keyStoreFromByteArray(byte[] total) {
        ByteStream byteStream = new ByteStream(total);
        return new Quadruple<>(
                new SecretKeySpec(byteStream.read(32), "AES"),
                new SecretKeySpec(byteStream.read(64), "HmacSHA512"),
                decodePublicKey(byteStream.read(294)),
                decodePrivateKey(byteStream.read())
        );
    }

    private static SecretKeySpec generateAesKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            return new SecretKeySpec(keyGenerator.generateKey().getEncoded(), "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private static SecretKeySpec generateHmacKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA512");
            keyGenerator.init(512);
            return new SecretKeySpec(keyGenerator.generateKey().getEncoded(), "HmacSHA512");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private static KeyPair generateRsaKeyPair(SecureRandom secureRandom) {
        try {
            RSAKeyGenParameterSpec rsaKGenSpec = new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(rsaKGenSpec, secureRandom);
            return kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new AssertionError(e);
        }
    }
}
