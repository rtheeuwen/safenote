package nl.safenote.controllers;

import nl.safenote.model.Header;
import nl.safenote.model.Note;
import nl.safenote.services.CryptoService;
import nl.safenote.services.NoteRepository;
import nl.safenote.utils.textsearch.TextSearchEngine;
import nl.safenote.services.SynchronizationService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class NoteController {

    private final NoteRepository noteRepository;
    private final CryptoService cryptoService;
    private final TextSearchEngine<Note> textSearchEngine;
    private final SynchronizationService synchronizationService;

    public NoteController(NoteRepository noteRepository, CryptoService cryptoService, TextSearchEngine<Note> textSearchEngine, SynchronizationService synchronizationService) {
        assert noteRepository !=null&&cryptoService!=null&& textSearchEngine !=null&&synchronizationService!=null;
        this.noteRepository = noteRepository;
        this.cryptoService = cryptoService;
        this.textSearchEngine = textSearchEngine;
        this.synchronizationService = synchronizationService;
    }

    public List<Header> getHeaders(){
        return noteRepository.findHeaders().stream()
                .map(h -> {
                    h.setHeader(cryptoService.decipher(h.getHeader()));
                    return h;
                })
                .collect(Collectors.toList());
    }

    public List<Header> search(String args){
        return textSearchEngine.search(
                    noteRepository.findAllTextNotes()
                    .stream().map(note -> cryptoService.decipher(note))
                    .collect(Collectors.toList()), args
                )
              .stream().map(Header::new).collect(Collectors.toList());
    }

    public Note getNote(String id){
        return cryptoService.decipher(noteRepository.findOne(id));
    }

    public void updateNote(Note note){
        Note clone = (Note) note.clone();
        clone.updateHeader();
        cryptoService.encipher(clone);
        noteRepository.update(clone);
        synchronizationService.send(clone);
    }

    public String createNote(){
        String id = noteRepository.nextId();
        Note note = new Note(id, Note.ContentType.TEXT);
        cryptoService.encipher(note);
        noteRepository.create(note);
        return id;
    }

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
            return new BufferedReader(new InputStreamReader(Object.class.getResourceAsStream("/gpl.txt"))).lines().collect(Collectors.joining("\n"));
    }

}
