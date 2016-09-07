package safenote.client.utils;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
            e.printStackTrace(); throw new AssertionError();
        }
    }

    private static PublicKey decodePublicKey(byte[] encoded){
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace(); throw new AssertionError();
        }
    }

    public static byte[] generateKeyStore(){
        KeyPair keyPair = generateRsaKeyPair();
        return keyStoreToByteArray(generateAesKey(), generateHmacKey(), keyPair.getPrivate(), keyPair.getPublic());
    }

    private static byte[] keyStoreToByteArray(SecretKeySpec aes, SecretKeySpec hmac, PrivateKey privateKey, PublicKey publicKey){
        byte[] aesBytes = aes.getEncoded();
        byte[] hmacBytes = hmac.getEncoded();
        byte[] privateBytes = privateKey.getEncoded();
        byte[] publicBytes = publicKey.getEncoded();
        byte[] total = new byte[32+256+294+privateBytes.length];
        System.arraycopy(aesBytes, 0, total, 0, 32);
        System.arraycopy(hmacBytes, 0, total, 32, 256);
        System.arraycopy(publicBytes, 0, total, 288, 294);
        System.arraycopy(privateBytes, 0, total, 582, privateBytes.length);
        return total;
    }

    public static Map<String, Object> keyStoreFromByteArray(byte[] total){
        Map<String, Object> keyStore = new HashMap<>();
        keyStore.put("AES", new SecretKeySpec(Arrays.copyOfRange(total, 0, 32), "AES"));
        keyStore.put("HMAC", new SecretKeySpec(Arrays.copyOfRange(total, 32, 288), "HmacSHA256"));
        keyStore.put("publicKey", decodePublicKey(Arrays.copyOfRange(total, 288, 582)));
        keyStore.put("privateKey", decodePrivateKey(Arrays.copyOfRange(total, 582, total.length)));
        return keyStore;
    }

    private static SecretKeySpec generateAesKey(){
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            return new SecretKeySpec(keyGenerator.generateKey().getEncoded(),"AES");
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e.getMessage());
        }
    }

    private static SecretKeySpec generateHmacKey(){
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte[] bytes = new byte[256];
            random.nextBytes(bytes);
            return new SecretKeySpec(bytes, "HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e.getMessage());
        }
    }

    private static KeyPair generateRsaKeyPair(){
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            RSAKeyGenParameterSpec rsaKGenSpec = new   RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(rsaKGenSpec, random);
            return kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new SecurityException(e.getMessage());
        }
    }
}
