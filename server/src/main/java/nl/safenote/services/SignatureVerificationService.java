package nl.safenote.services;

import nl.safenote.server.model.Message;
import nl.safenote.server.model.Note;
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
        return userPublicKeyRepository.create(publicKey);
    }

    @Override
    public String verifySignature(Message message) {
        if(message.getExpires()<=System.currentTimeMillis()) throw new SecurityException("Message is expired");
        try {
            String messageSignature = message.getSignature();
            String userId = messageSignature.substring(0, 5);
            byte[] claimedSignature = DatatypeConverter.parseBase64Binary(messageSignature.substring(5));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(userPublicKeyRepository.findOne(userId).getPublicKey()));
            PublicKey publicKey = kf.generatePublic(keySpec);
            Object messageBody = message.getBody();
            byte[] data;
            if(messageBody!=null&&messageBody instanceof Note){
                Note note = (Note) messageBody;
                data = (note.getContent() + note.getHeader() + message.getExpires()).getBytes();
            } else {
                data = Long.valueOf(message.getExpires()).toString().getBytes();
            }
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(data);
            signature.verify(claimedSignature);
            return userId;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
            throw new SecurityException();
        }
    }
}