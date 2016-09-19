package nl.safenote.services;

import nl.safenote.model.Quadruple;
import nl.safenote.utils.KeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import nl.safenote.utils.FileIO;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
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
            byte[] salt = new byte[32];
            secureRandom.nextBytes(salt);
            SecretKeySpec key = deriveKey(password, salt);
            byte[] enciphered = aesEncipher(compress(keyStore), key);
            byte[] output = new byte[32 + enciphered.length];
            System.arraycopy(salt, 0, output, 0, 32);
            System.arraycopy(enciphered, 0, output, 32, enciphered.length);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private byte[] decipherStorage(byte[] cipherText, String password){
        byte[] salt = Arrays.copyOfRange(cipherText, 0, 32);
        SecretKeySpec key = deriveKey(password, salt);
        return decompress(aesDecipher(Arrays.copyOfRange(cipherText, 32, cipherText.length), key));
    }

    private SecretKeySpec deriveKey(String password, byte[] salt){
        try {
            return new SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                    .generateSecret(new PBEKeySpec(password.toCharArray(), salt, 800000, 256)).getEncoded(), "AES");
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
