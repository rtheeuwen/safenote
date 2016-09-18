package nl.safenote.api;

import nl.safenote.model.Header;
import nl.safenote.model.Note;
import nl.safenote.services.CryptoService;
import nl.safenote.services.NoteRepository;
import nl.safenote.services.SearchService;
import nl.safenote.services.SynchronizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteController {

    private final NoteRepository noteRepository;
    private final CryptoService cryptoService;
    private final SearchService searchService;
    private final SynchronizationService synchronizationService;

    @Autowired
    public NoteController(NoteRepository noteRepository, CryptoService cryptoService, SearchService searchService, SynchronizationService synchronizationService) {
        assert noteRepository !=null&&cryptoService!=null&& searchService !=null&&synchronizationService!=null;
        this.noteRepository = noteRepository;
        this.cryptoService = cryptoService;
        this.searchService = searchService;
        this.synchronizationService = synchronizationService;
    }

    public List<Header> getHeaders(){
        return noteRepository.findHeaders().stream()
                .map(h -> h.setHeader(cryptoService.decipher(h.getHeader())))
                .collect(Collectors.toList());
    }

    public List<Header> search(String args){
        return searchService.search(args);
    }

    public Note getNote(String id){

        return cryptoService.decipher(noteRepository.findOne(id));
    }

    public void updateNote(String id, Note note){
        if(!noteRepository.isUpdateable(note))
            throw new IllegalArgumentException("This type of content cannot be updated");
        cryptoService.encipher(note);
        noteRepository.update(note);
        synchronizationService.send(note);
    }

    public String createNote(String header){
        if(header.isEmpty())
            header = "nameless note";
        String id = noteRepository.nextId();
        if(header.length()>50) header = header.substring(0, 50);
        Note note = new Note(id, header, Note.ContentType.TEXT);
        cryptoService.encipher(note);
        noteRepository.create(note);
        synchronizationService.send(note);
        return id;
    }

    @Transactional
    public void deleteNote(String id){
        Note note = noteRepository.findOne(id);
        note.setEncrypted(true);
        noteRepository.delete(note);
        synchronizationService.delete(note);
    }

    public void synchronize(){
        synchronizationService.synchronize();
    }

    public String info(){
        try {
            return new BufferedReader(new InputStreamReader(new ClassPathResource("/gpl.txt").getInputStream())).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new AssertionError(e.getCause());
        }
    }
}
