package nl.safenote.services;

import nl.safenote.server.model.Message;
import nl.safenote.server.model.SafeNote;
import nl.safenote.server.model.UserPublicKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import nl.safenote.server.persistence.UserPublicKeyRepository;

import javax.xml.bind.DatatypeConverter;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public interface SignatureVerificationService {

    String enlist(UserPublicKey publicKey);
    String verifySignature(Message message);
}

@Service
class SignatureVerificationServiceImpl implements SignatureVerificationService {


    private final UserPublicKeyRepository userPublicKeyRepository;

    @Autowired
    public SignatureVerificationServiceImpl(UserPublicKeyRepository userPublicKeyRepository) {
        this.userPublicKeyRepository = userPublicKeyRepository;
    }

    @Override
    public String enlist(UserPublicKey publicKey) {
        return userPublicKeyRepository.enlist(publicKey);
    }

    @Override
    public String verifySignature(Message message) {
        if(message.getExpires()<=System.currentTimeMillis()) throw new SecurityException("Message is expired");
        try {
            String userId = message.getSignee();
            byte[] claimedSignature = DatatypeConverter.parseBase64Binary(message.getSignature());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(userPublicKeyRepository.findOne(userId).getPublicKey()));
            PublicKey publicKey = kf.generatePublic(keySpec);
            Object messageBody = message.getBody();
            byte[] data;
            if(messageBody!=null&&messageBody instanceof SafeNote){
                SafeNote safeNote = (SafeNote) messageBody;
                data = (safeNote.getContent() +
                        safeNote.getHeader() +
                        message.getExpires()).getBytes();
            } else {
                data = Long.valueOf(message.getExpires()).toString().getBytes();
            }
            Signature signature = Signature.getInstance("SHA512withRSA");
            signature.initVerify(publicKey);
            signature.update(data);
            signature.verify(claimedSignature);
            return userId;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
            throw new SecurityException();
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}