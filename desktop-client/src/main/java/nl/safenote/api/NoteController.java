package nl.safenote.api;

import nl.safenote.model.Header;
import nl.safenote.model.Note;
import nl.safenote.model.ContentType;
import nl.safenote.services.CryptoService;
import nl.safenote.services.NoteRepository;
import nl.safenote.services.SearchService;
import nl.safenote.services.SynchronizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Transactional
@RequestMapping(value="/", headers = "Accept=*/*", produces = MediaType.APPLICATION_JSON_VALUE)
public class NoteController {

    private final NoteRepository noteRepository;
    private final CryptoService cryptoService;
    private final SearchService searchService;
    private final SynchronizationService synchronizationService;

    @Autowired
    public NoteController(NoteRepository noteRepository, CryptoService cryptoService, SearchService searchService, SynchronizationService synchronizationService) {
        assert noteRepository !=null&&cryptoService!=null&&searchService!=null&&synchronizationService!=null;
        this.noteRepository = noteRepository;
        this.cryptoService = cryptoService;
        this.searchService = searchService;
        this.synchronizationService = synchronizationService;
    }

    @RequestMapping(value="headers", method = RequestMethod.GET)
    public List<Header> getHeaders(){
        return searchService.search("");
    }

    @RequestMapping(value="search", method = RequestMethod.GET)
    public List<Header> search(@RequestParam("q") String args){
        return searchService.search(args);
    }

    @RequestMapping(value="notes/{id}", method = RequestMethod.GET)
    public Note getNote(@PathVariable String id){

        return cryptoService.decipher(noteRepository.findOne(id), false);
    }

    @RequestMapping(value="notes/{id}", method = RequestMethod.PUT)
    public ResponseEntity updateNote(@PathVariable String id, @RequestBody Note newNote){
        Note note = cryptoService.encipher(newNote);
        noteRepository.update(note);
        synchronizationService.send(note);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value="notes", method = RequestMethod.POST)
    public String createNote(@RequestBody String header){
        String id = noteRepository.nextId();
        if(header.length()>50) header = header.substring(0, 50);
        Note note = new Note(id, header, ContentType.TEXT);
        Note safeNote = cryptoService.encipher(note);
        noteRepository.create(safeNote);
        synchronizationService.send(safeNote);
        return id;
    }

    @RequestMapping(value="notes/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteNote(@PathVariable String id){
        Note safeNote = noteRepository.findOne(id);
        safeNote.setEncrypted(true);
        noteRepository.delete(safeNote);
        synchronizationService.delete(safeNote);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "synchronize", method = RequestMethod.GET)
    public ResponseEntity synchronize(){
        synchronizationService.synchronize();
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value="about", method = RequestMethod.GET)
    public String info(){
        try {
            return new BufferedReader(new InputStreamReader(new ClassPathResource("/gpl.txt").getInputStream())).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new AssertionError(e.getCause());
        }
    }
}
