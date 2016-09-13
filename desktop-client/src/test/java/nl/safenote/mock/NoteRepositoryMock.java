package nl.safenote.mock;

import nl.safenote.model.Note;
import nl.safenote.services.CryptoService;
import nl.safenote.services.NoteRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NoteRepositoryMock implements NoteRepository {

    private Map<String, Note> notes;
    private int mockDataSize;

    public NoteRepositoryMock(CryptoService cryptoService){
        String[] headers = new String[]{"note1", "note2", "note3", "note4", "note5", "note6"};
        String[] content = new String[]{"hello hello test", "hello test", "hello", "test", "test test", "note"};
        this.notes = IntStream.range(0, 6).mapToObj(i -> createNote(headers[i], content[i])).map(note -> cryptoService.encipher(note)).collect(Collectors.toMap(Note::getId, n -> n));
        this.mockDataSize = headers.length;
        if(headers.length!=content.length&&headers.length!=notes.size())
            throw new AssertionError("bad mock data");
    }

    @Override
    public Note findOne(String id) {
        return notes.get(id);
    }

    @Override
    public List<Note> findAll() {
        return notes.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
    }

    @Override
    public void create(Note note) {
        notes.put(note.getId(), note);
    }

    @Override
    public Note update(Note note) {
        delete(note);
        create(note);
        return note;
    }

    @Override
    public void delete(Note note) {
        notes.remove(note.getId());
    }

    @Override
    public void delete(String id) {
        notes.remove(id);
    }

    @Override
    public void deleteAll() {
        this.notes = new HashMap<>();
    }

    @Override
    public String nextId() {
        return UUID.randomUUID().toString();
    }

    private Note createNote(String header, String content){
        Note note = new Note();
        note.setId(nextId());
        note.setHeader(header);
        note.setContent(content);
        return note;
    }

    public int getMockDataSize(){
        return this.mockDataSize;
    }
}
