package nl.safenote.services;

import nl.safenote.model.Pair;
import nl.safenote.model.Quadruple;
import nl.safenote.utils.KeyUtils;
import nl.safenote.utils.FileIO;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import static nl.safenote.utils.KeyUtils.keyStoreFromByteArray;

public interface AuthenticationService{

    void authenticate(String passphrase);
}


class AuthenticationServiceImpl extends AbstractAesService implements AuthenticationService{

    private final CryptoService cryptoService;
    private final SynchronizationService synchronizationService;

    AuthenticationServiceImpl(CryptoService cryptoService, SynchronizationService synchronizationService) {
        assert cryptoService!= null&&synchronizationService!=null;
        this.cryptoService = cryptoService;
        this.synchronizationService = synchronizationService;
    }

    @Override
    public void authenticate(String passphrase) {
        assert passphrase!= null;
        if(FileIO.dataExists()) load(passphrase);
        else generate(passphrase);
    }

    private void generate(String passphrase){
        try {
            SecureRandom secureRandom = SecureRandom.getInstance("NativePRNGNonBlocking");
            byte[] keyStore = KeyUtils.generateKeyStore(secureRandom);
            FileIO.write(encipherStorage(keyStore, passphrase, secureRandom));
            initializeServices(keyStoreFromByteArray(keyStore));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private void load(String passphrase){
        initializeServices(keyStoreFromByteArray(decipherStorage(FileIO.read(), passphrase)));
    }

    private void initializeServices(Quadruple<SecretKeySpec, SecretKeySpec, PublicKey, PrivateKey> keyStore){
        if(!Objects.equals(keyStore.getA().getAlgorithm(), "AES") || !Objects.equals(keyStore.getB().getAlgorithm(), "HmacSHA512")) {
            throw new IllegalArgumentException("Invalid keys");
        }
            this.cryptoService.init(keyStore.getA(), keyStore.getB(), keyStore.getD());
            this.synchronizationService.enlist(keyStore.getC());
    }

    private byte[] encipherStorage(byte[] keyStore, String password, SecureRandom secureRandom){
        try {
            byte[] salt = new byte[32];
            secureRandom.nextBytes(salt);
            Pair<SecretKeySpec, SecretKeySpec> keys = deriveKeys(password, salt);
            byte[] enciphered = super.aesEncipher(compress(keyStore), keys.getA());
            byte[] output = new byte[32 + enciphered.length + 32];
            System.arraycopy(salt, 0, output, 0, 32);
            System.arraycopy(enciphered, 0, output, 32, enciphered.length);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keys.getB());
            mac.update(output, 0, 32 + enciphered.length);
            mac.doFinal(output, 32 + enciphered.length);
            return output;
        } catch (NoSuchAlgorithmException | InvalidKeyException | ShortBufferException e) {
            throw new AssertionError(e);
        }
    }

    private byte[] decipherStorage(byte[] cipherText, String password){
        try {
            byte[] salt = Arrays.copyOfRange(cipherText, 0, 32);
            Pair<SecretKeySpec, SecretKeySpec> keys = deriveKeys(password, salt);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keys.getB());
            mac.update(cipherText, 0, cipherText.length-32);
            byte[] claimedMac = Arrays.copyOfRange(cipherText, cipherText.length-32, cipherText.length);
            if(!(Arrays.equals(claimedMac, mac.doFinal())))
                throw new SecurityException("Invalid password or invalid MAC"); //mac secret derived from key differs from correct mac secret, or data is altered
            return decompress(super.aesDecipher(Arrays.copyOfRange(cipherText, 32, cipherText.length - 32), keys.getA()));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new AssertionError(e);
        }
    }

    private Pair<SecretKeySpec, SecretKeySpec> deriveKeys(String password, byte[] salt){
        try {
            byte[] k = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
                    .generateSecret(new PBEKeySpec(password.toCharArray(), salt, 800000, 512)).getEncoded();
            return new Pair<>(new SecretKeySpec(Arrays.copyOfRange(k, 0, 32), "AES"),
                    new SecretKeySpec(Arrays.copyOfRange(k, 32, 64), "HMACSha256"));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Compression is required because a QR-code is generated for the local keystore. By using compression, less data
     * have to be contained in the QR-code, making it easier for a camera to read the code.
     * @param input uncomrpessed
     * @return compressed
     */
    private static byte[] compress(byte[] input) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try(OutputStream outputStream = new DeflaterOutputStream(byteArrayOutputStream)) {
            outputStream.write(input);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static byte[] decompress(byte[] bytes) {
        InputStream inputStream = new InflaterInputStream(new ByteArrayInputStream(bytes));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0)
                byteArrayOutputStream.write(buffer, 0, length);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
