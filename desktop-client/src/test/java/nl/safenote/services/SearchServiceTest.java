package nl.safenote.services;

import org.junit.Test;
import nl.safenote.mock.CryptoServiceMock;
import nl.safenote.mock.SafeNoteRepositoryMock;
import nl.safenote.model.Header;
import nl.safenote.model.Note;
import nl.safenote.model.Result;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class SearchServiceTest {

    private final CryptoService cryptoService = new CryptoServiceMock();
    private final SafeNoteRepositoryMock noteRepository = new SafeNoteRepositoryMock(cryptoService);
    private final SearchService searchService = new SearchServiceImpl(noteRepository, cryptoService);

    @Test
    public void ensureAllNotesAreFoundWhenNoArgs(){
        ArrayList<Header> found = (ArrayList<Header>) searchService.search("");
        ArrayList<Header> allHeaders = (ArrayList<Header>) noteRepository.findAll().stream().sorted((a, b) -> (int)(b.getCreated()-a.getCreated())).map(note -> new Header(note.getId(), cryptoService.decipher(note, true).getHeader())).collect(Collectors.toList());
        assertTrue(found.equals(allHeaders));
    }

    @Test
    public void ensureMostRelevantNoteIsFirst(){
        Header found = searchService.search("hello").get(0);
        assertEquals("note1", found.getHeader());
    }

    @Test
    public void ensureNoNotesAreReturnedWhenNoRelevantResults(){
        assertTrue(searchService.search("gibberish").isEmpty());
    }

    @Test
    public void ensureGetResultWorksAsExpected() throws Exception{
        Note note = new Note();
        note.setContent("test test test");
        Class[] params = new Class[2];
        params[0] = Note.class;
        params[1] = String[].class;
        Method getResult = SearchServiceImpl.class.getDeclaredMethod("getResult", params);
        getResult.setAccessible(true);
        Result<Note> result = (Result<Note>) getResult.invoke(searchService, note, new String[]{"test"});
        assertEquals(result.getScore(), 3);
    }
}
