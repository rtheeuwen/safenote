package nl.safenote.services;

import org.junit.Test;
import nl.safenote.mock.CryptoServiceMock;
import nl.safenote.mock.NoteRepositoryMock;
import nl.safenote.model.Header;
import nl.safenote.model.Note;
import nl.safenote.model.Result;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TextSearchServiceTest {

    private final CryptoService cryptoService = new CryptoServiceMock();
    private final NoteRepositoryMock noteRepository = new NoteRepositoryMock(cryptoService);
    private final TextSearchService textSearchService = new TextSearchServiceImpl(noteRepository, cryptoService);

    @Test
    public void ensureAllNotesAreFoundWhenNoArgs(){
        ArrayList<Header> found = (ArrayList<Header>) textSearchService.search("");
        ArrayList<Header> allHeaders = (ArrayList<Header>) noteRepository.findAll().stream().sorted((a, b) -> (int)(b.getCreated()-a.getCreated())).map(note -> new Header(note.getId(), cryptoService.decipher(note).getHeader())).collect(Collectors.toList());
        assertTrue(found.equals(allHeaders));
    }

    @Test
    public void ensureMostRelevantNoteIsFirst(){
        Header found = textSearchService.search("hello").get(0);
        assertEquals("note1", found.getHeader());
    }

    @Test
    public void ensureNoNotesAreReturnedWhenNoRelevantResults(){
        assertTrue(textSearchService.search("gibberish").isEmpty());
    }

    @Test
    public void ensureGetResultWorksAsExpected() throws Exception{
        Note note = new Note();
        note.setContent("test test test");
        Class[] params = new Class[2];
        params[0] = Note.class;
        params[1] = String[].class;
        Method getResult = TextSearchServiceImpl.class.getDeclaredMethod("getResult", params);
        getResult.setAccessible(true);
        Result<Note> result = (Result<Note>) getResult.invoke(textSearchService, note, new String[]{"test"});
        assertEquals(result.getScore(), 3);
    }
}
