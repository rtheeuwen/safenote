package nl.safenote.server.api;

import nl.safenote.services.SignatureVerificationService;
import nl.safenote.server.model.Message;
import nl.safenote.server.model.SafeNote;
import nl.safenote.server.model.UserPublicKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import nl.safenote.server.persistence.SafeNoteRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value="/", headers = "Accept=*/*", produces = MediaType.APPLICATION_JSON_VALUE)
public class MainController {

    private final SafeNoteRepository safeNoteRepository;
    private final SignatureVerificationService signatureVerificationService;

    @Autowired
    public MainController(SafeNoteRepository safeNoteRepository, SignatureVerificationService signatureVerificationService) {
        this.safeNoteRepository = safeNoteRepository;
        this.signatureVerificationService = signatureVerificationService;
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity invalidSignature(SecurityException e){
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping(value = "enlist", method = RequestMethod.POST, consumes = {"text/plain", "application/json"})
    public String enlist(@RequestBody UserPublicKey publicKey){
        String user = signatureVerificationService.enlist(publicKey);
        return signatureVerificationService.enlist(publicKey);
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = {"text/plain", "application/json"})
    public Message<SafeNote> save(@RequestBody Message<SafeNote> message){
        SafeNote safeNote = message.getBody();
        safeNote.setUserId(signatureVerificationService.verifySignature(message));
        safeNoteRepository.save(safeNote);
        return message;
    }

    @RequestMapping(method = RequestMethod.DELETE, consumes = {"text/plain", "application/json"})
    public void delete(@RequestBody Message<SafeNote> message){
        SafeNote safeNote = message.getBody();
        safeNote.setUserId(signatureVerificationService.verifySignature(message));
        safeNoteRepository.setDelete(safeNote);
    }

    @RequestMapping(value = "time", method = RequestMethod.GET, consumes = {"text/plain", "application/json"})
    public Long getTime(){
        return System.currentTimeMillis();
    }

    @RequestMapping(value = "notes", method = RequestMethod.POST, consumes = {"text/plain", "application/json"})
    public List<SafeNote> getNotes(@RequestBody Message<List<String>> message){
        return safeNoteRepository.findNotes(message.getBody(), signatureVerificationService.verifySignature(message));
    }

    @RequestMapping(value = "checksums", method = RequestMethod.POST, consumes = {"text/plain", "application/json"})
    public Map<String, String> getChecksums(@RequestBody Message message){
            return safeNoteRepository.findChecksums(signatureVerificationService.verifySignature(message));
    }

    @RequestMapping(value = "deleted", method = RequestMethod.POST, consumes = {"text/plain", "application/json"})
    public List<String> getDeletedIds(@RequestBody Message message){
            return safeNoteRepository.findDeleted(signatureVerificationService.verifySignature(message));
    }
}
