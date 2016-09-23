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

    private KeyUtils(){
        throw new AssertionError();
    }

    private static PrivateKey decodePrivateKey(byte[] encoded){
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private static PublicKey decodePublicKey(byte[] encoded){
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    public static byte[] generateKeyStore(SecureRandom secureRandom){
        KeyPair keyPair = generateRsaKeyPair(secureRandom);
        return keyStoreToByteArray(generateAesKey(), generateHmacKey(), keyPair.getPrivate(), keyPair.getPublic());
    }

    private static byte[] keyStoreToByteArray(SecretKeySpec aes, SecretKeySpec hmac, PrivateKey privateKey, PublicKey publicKey){
        byte[] aesBytes = aes.getEncoded();
        byte[] hmacBytes = hmac.getEncoded();
        byte[] privateBytes = privateKey.getEncoded();
        byte[] publicBytes = publicKey.getEncoded();
        byte[] total = new byte[32+64+294+privateBytes.length];
        System.arraycopy(aesBytes, 0, total, 0, 32);
        System.arraycopy(hmacBytes, 0, total, 32, 64);
        System.arraycopy(publicBytes, 0, total, 96, 294);
        System.arraycopy(privateBytes, 0, total, 390, privateBytes.length);
        return total;
    }

    public static Quadruple<SecretKeySpec, SecretKeySpec, PrivateKey, PublicKey> keyStoreFromByteArray(byte[] total){
        return new Quadruple<>(new SecretKeySpec(Arrays.copyOfRange(total, 0, 32), "AES"),
                new SecretKeySpec(Arrays.copyOfRange(total, 32, 64), "HmacSHA512"),
                decodePrivateKey(Arrays.copyOfRange(total, 390, total.length)),
                decodePublicKey(Arrays.copyOfRange(total, 96, 390))
        );
    }

    private static SecretKeySpec generateAesKey(){
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            return new SecretKeySpec(keyGenerator.generateKey().getEncoded(),"AES");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private static SecretKeySpec generateHmacKey(){
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA512");
            keyGenerator.init(512);
            return new SecretKeySpec(keyGenerator.generateKey().getEncoded(), "HmacSHA512");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private static KeyPair generateRsaKeyPair(SecureRandom secureRandom){
        try {
            RSAKeyGenParameterSpec rsaKGenSpec = new   RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(rsaKGenSpec, secureRandom);
            return kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new AssertionError(e);
        }
    }
}
