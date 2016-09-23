package nl.safenote.utils;

import nl.safenote.model.Quadruple;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(32 + 64 + 294 + privateBytes.length)) {
            byteArrayOutputStream.write(aes.getEncoded()); //32 source
            byteArrayOutputStream.write(hmac.getEncoded()); //64 source
            byteArrayOutputStream.write(publicKey.getEncoded()); //294 source
            byteArrayOutputStream.write(privateBytes); //variable length 1218 or 1217 source
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static Quadruple<SecretKeySpec, SecretKeySpec, PublicKey, PrivateKey> keyStoreFromByteArray(byte[] total) {
        ByteSequence byteSequence = new ByteSequence(total);
        return new Quadruple<>(
                new SecretKeySpec(byteSequence.take(32), "AES"),
                new SecretKeySpec(byteSequence.take(64), "HmacSHA512"),
                decodePublicKey(byteSequence.take(294)),
                decodePrivateKey(byteSequence.takeRemaining())
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

    //helper class for accessing fragments of a byte array sequentially
    //saves a lot of double checking time and mistakes
    private static class ByteSequence {

        private final byte[] source;
        int index;

        ByteSequence(byte[] source) {
            if (source == null)
                throw new NullPointerException();
            this.source = source;
        }

        byte[] take(int len) {
            if (index == source.length)
                throw new IllegalArgumentException("Source is depleted.");
            if (len < 0 || (len + index > source.length))
                throw new IllegalArgumentException(source.length - index + " bytes remaining in source");
            byte[] out = Arrays.copyOfRange(source, index, index + len);
            index += len;
            return out;
        }

        byte[] takeRemaining() {
            if (source.length == index)
                throw new IllegalArgumentException("Source is depleted");
            return take(source.length - index);
        }
    }
}
