package nl.safenote.mock;

import nl.safenote.model.SafeNote;
import nl.safenote.model.Note;
import nl.safenote.services.CryptoService;
import nl.safenote.services.SafeNoteRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SafeNoteRepositoryMock implements SafeNoteRepository {

    private Map<String, SafeNote> notes;
    private int mockDataSize;

    public SafeNoteRepositoryMock(CryptoService cryptoService){
        String[] headers = new String[]{"note1", "note2", "note3", "note4", "note5", "note6"};
        String[] content = new String[]{"hello hello test", "hello test", "hello", "test", "test test", "note"};
        this.notes = IntStream.range(0, 6).mapToObj(i -> createNote(headers[i], content[i])).map(note -> cryptoService.encipher(note)).collect(Collectors.toMap(Note::getId, n -> n));
        this.mockDataSize = headers.length;
        if(headers.length!=content.length&&headers.length!=notes.size())
            throw new AssertionError("bad mock data");
    }

    @Override
    public SafeNote findOne(String id) {
        return notes.get(id);
    }

    @Override
    public List<SafeNote> findAll() {
        return notes.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
    }

    @Override
    public void create(SafeNote note) {
        notes.put(note.getId(), note);
    }

    @Override
    public Note update(SafeNote note) {
        delete(note);
        create(note);
        return note;
    }

    @Override
    public void delete(SafeNote note) {
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
        SafeNote note = new SafeNote();
        note.setId(nextId());
        note.setHeader(header);
        note.setContent(content);
        return note;
    }

    public int getMockDataSize(){
        return this.mockDataSize;
    }
}
