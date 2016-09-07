package api;

import beans.CryptoBean;
import model.Message;
import model.Note;
import model.UserPublicKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import persistence.NoteRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value="/", headers = "Accept=*/*", produces = MediaType.APPLICATION_JSON_VALUE)
public class MainController {

    private final NoteRepository noteRepository;
    private final CryptoBean cryptoBean;

    @Autowired
    public MainController(NoteRepository noteRepository, CryptoBean cryptoBean) {
        this.noteRepository = noteRepository;
        this.cryptoBean = cryptoBean;
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity invalidSignature(SecurityException e){
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping(value = "enlist", method = RequestMethod.POST, consumes = {"text/plain", "application/json"})
    public String enlist(@RequestBody UserPublicKey publicKey){
        String user = cryptoBean.enlist(publicKey);
        return cryptoBean.enlist(publicKey);
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = {"text/plain", "application/json"})
    public Message<Note> save(@RequestBody Message<Note> message){
            Note note = message.getBody();
            note.setUserId(cryptoBean.verifySignature(message));
            try{
                noteRepository.update(note);
            } catch(Exception e){
            noteRepository.create(note);
            }

        return message;
    }

    @RequestMapping(method = RequestMethod.DELETE, consumes = {"text/plain", "application/json"})
    public void delete(@RequestBody Message<Note> message){
        Note note = message.getBody();
        note.setUserId(cryptoBean.verifySignature(message));
        noteRepository.setDelete(note);
    }

    @RequestMapping(value = "time", method = RequestMethod.GET, consumes = {"text/plain", "application/json"})
    public Long getTime(){
        return System.currentTimeMillis();
    }

    @RequestMapping(value = "notes", method = RequestMethod.POST, consumes = {"text/plain", "application/json"})
    public List<Note> getNotes(@RequestBody Message<List<String>> message){
        return noteRepository.findNotes(message.getBody(), cryptoBean.verifySignature(message));
    }

    @RequestMapping(value = "checksums", method = RequestMethod.POST, consumes = {"text/plain", "application/json"})
    public Map<String, String> getChecksums(@RequestBody Message message){
            return noteRepository.findChecksums(cryptoBean.verifySignature(message));
    }

    @RequestMapping(value = "deleted", method = RequestMethod.POST, consumes = {"text/plain", "application/json"})
    public List<String> getDeletedIds(@RequestBody Message message){
            return noteRepository.findDeleted(cryptoBean.verifySignature(message));
    }
}
