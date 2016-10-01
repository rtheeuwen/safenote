package nl.safenote.utils;

import nl.safenote.model.KeyStore;

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
        return new ByteStream(
                aes.getEncoded(), //32 bytes
                hmac.getEncoded(), //64 bytes
                publicKey.getEncoded(), //294 bytes
                privateBytes //variable length 1218 or 1217 bytes
        ).assertSize(32, 64, 294, privateBytes.length).read();
    }

    public static KeyStore keyStoreFromByteArray(byte[] total) {
        ByteStream byteStream = new ByteStream(total);
        return new KeyStore(
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

    /**
     * Provides an alternative to repeated System.arraycopies and/or ByteArrayOutputStream class.
     * Prevents mistakes when manually concatenating and slicing byte arrays.
     * Utility class for sequentially reading fragments from an underlying source (byte[]),
     * Can also be used to 'concatenate' a bunch of byte[] in the constructor
     */
    private final static class ByteStream {

        private final byte[] source;
        private int index;

        ByteStream(byte[]... source){
            if(source.length==1)
                this.source = source[0];
            else
                this.source = new byte[Arrays.stream(source).mapToInt(b -> b.length).sum()];
                int writeIndex = 0;
                for(byte[] bytes:source){
                    System.arraycopy(bytes, 0, this.source, writeIndex, bytes.length);
                    writeIndex += bytes.length;
                }
        }

        byte[] read(int len) {
            if(index == source.length)
                throw new IllegalArgumentException("Source is depleted.");
            if(len < 0 || (len + index > source.length))
                throw new IllegalArgumentException(source.length - index + " bytes remaining in source");
            if(len==source.length)
                return source;
            return Arrays.copyOfRange(source, index, index += len);
        }

        byte[] read() {
            if(source.length == index)
                throw new IllegalArgumentException("Source is depleted");
            if(index ==0)
                return source;
            else
                return read(source.length - index);
        }

        ByteStream assertSize(int... size){
            assert source.length==Arrays.stream(size).sum();
            return this;
        }
    }
}
