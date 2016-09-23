package nl.safenote.controllers;

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
                .map(h -> {
                    String header = cryptoService.decipher(h.getHeader());
                    h.setHeader("".equals(header)?"New note...":header);
                    return h;
                })
                .collect(Collectors.toList());
    }

    public List<Header> search(String args){
        return searchService.search(args);
    }

    public Note getNote(String id){
        return cryptoService.decipher(noteRepository.findOne(id));
    }

    public void updateNote(Note note){
        if(!noteRepository.isUpdateable(note))
            throw new IllegalArgumentException("This type of content cannot be updated");

        String content = note.getContent();
        int index = content.indexOf("\n");
        index = index!=-1?index:content.indexOf(" ");
        index = index!=-1?index:content.length()<=10?content.length():10;
        index = index>35?35:index;
        note.setHeader(content.substring(0, index));

        cryptoService.encipher(note);
        noteRepository.update(note);
        synchronizationService.send(note);
    }

    public String createNote(){
        String id = noteRepository.nextId();
        Note note = new Note(id, Note.ContentType.TEXT);
        cryptoService.encipher(note);
        noteRepository.create(note);
        return id;
    }

    @Transactional
    public void deleteNote(String id){
        Note note = noteRepository.findOne(id);
        note.setEncrypted(true);
        noteRepository.delete(note);
        synchronizationService.delete(note);
    }

    public boolean synchronize(){
        return synchronizationService.synchronize();
    }

    public String info(){
        try {
            return new BufferedReader(new InputStreamReader(new ClassPathResource("/gpl.txt").getInputStream())).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new AssertionError(e.getCause());
        }
    }
}
