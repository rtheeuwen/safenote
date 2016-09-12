package nl.safenote.services;

import nl.safenote.model.Quadruple;
import nl.safenote.utils.KeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import nl.safenote.utils.FileIO;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import static nl.safenote.utils.KeyUtils.keyStoreFromByteArray;

public interface AuthenticationService{

    void authenticate(String passphrase);
}

/**
 * Loads the local keystore, or in its absence generates a new one
 * @Author Roel Theeuwen
 * @Verion 1.0
 * @Since 2016-09-04
 */
@Service
class AuthenticationServiceImpl extends AbstractAesService implements AuthenticationService{

    private final CryptoService cryptoService;
    private final SynchronizationService synchronizationService;

    @Autowired
    AuthenticationServiceImpl(CryptoService cryptoService, SynchronizationService synchronizationService) {
        this.cryptoService = cryptoService;
        this.synchronizationService = synchronizationService;
    }

    @Override
    public void authenticate(String passphrase) {
        if(!FileIO.dataExists()){
            generate(passphrase);
        }else {
            load(passphrase);
        }
    }

    private void generate(String passphrase){
        byte[] keyStore = KeyUtils.generateKeyStore();
        FileIO.write(encipherStorage(keyStore, passphrase));
        initializeServices(keyStoreFromByteArray(keyStore));
    }

    private void load(String passphrase){
        initializeServices(keyStoreFromByteArray(decipherStorage(FileIO.read(), passphrase)));
    }

    private void initializeServices(Quadruple<SecretKeySpec, SecretKeySpec, PrivateKey, PublicKey> keyStore){
        if(!Objects.equals(keyStore.getA().getAlgorithm(), "AES") || !Objects.equals(keyStore.getB().getAlgorithm(), "HmacSHA256")) {
            throw new IllegalArgumentException("Invalid keys");
        }
            this.cryptoService.init(keyStore.getA(), keyStore.getB(), keyStore.getC());
            this.synchronizationService.enlist(DatatypeConverter.printBase64Binary(( keyStore.getD()).getEncoded()));
    }

    private byte[] encipherStorage(byte[] keyStore, String password){
        try {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            byte[] salt = new byte[128];
            secureRandom.nextBytes(salt);
            byte[] passBytes = password.getBytes();
            byte[] saltedPassword = new byte[128+passBytes.length];
            System.arraycopy(salt, 0, saltedPassword, 0, 128);
            System.arraycopy(passBytes, 0, saltedPassword, 128, passBytes.length);
            byte[] digest = digest(saltedPassword);
            SecretKeySpec key = new SecretKeySpec(Arrays.copyOfRange(digest, 0, 32), "AES");
            byte[] enciphered = aesEncipher(compress(keyStore), key);
            byte[] output = new byte[128 + enciphered.length];
            System.arraycopy(salt, 0, output, 0, 128);
            System.arraycopy(enciphered, 0, output, 128, enciphered.length);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private byte[] decipherStorage(byte[] cipherText, String password){
        byte[] salt = Arrays.copyOfRange(cipherText, 0, 128);
        byte[] passBytes = password.getBytes();
        byte[] digest = new byte[salt.length + passBytes.length];
        System.arraycopy(salt, 0, digest, 0, 128);
        System.arraycopy(passBytes, 0, digest, 128, passBytes.length);
        digest = digest(digest);
        SecretKeySpec key = new SecretKeySpec(Arrays.copyOfRange(digest, 0, 32), "AES");
        return decompress(aesDecipher(Arrays.copyOfRange(cipherText, 128, cipherText.length), key));
    }

    private byte[] digest(byte[] password){
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(password);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
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
            byte[] buffer = new byte[4000];
            int len;
            while ((len = inputStream.read(buffer)) > 0)
                byteArrayOutputStream.write(buffer, 0, len);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
