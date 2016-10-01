//package nl.safenote.services;
//
//import org.junit.Test;
//import nl.safenote.mock.CryptoServiceMock;
//import nl.safenote.mock.NoteRepositoryMock;
//import nl.safenote.model.Header;
//import nl.safenote.model.Note;
//import nl.safenote.utils.search.SearchResult;
//
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.Assert.*;
//
//public class SearchServiceTest {
//
//    private final CryptoService cryptoService = new CryptoServiceMock();
//    private final NoteRepositoryMock noteRepository = new NoteRepositoryMock(cryptoService);
//    private final TextSearchEngine textSearchEngine = new SearchServiceImpl(noteRepository, cryptoService);
//
//    @Test
//    public void ensureAllNotesAreFoundWhenNoArgs(){
////        ArrayList<Header> found = (ArrayList<Header>) textSearchEngine.search("");
////        List<Header> allHeaders = noteRepository.findHeaders();
////        assertTrue(found.equals(allHeaders));
//    }
//
//    @Test
//    public void ensureMostRelevantNoteIsFirst(){
//        Header found = textSearchEngine.search("hello").get(0);
//        assertEquals("note1", found.getHeader());
//    }
//
//    @Test
//    public void ensureNoNotesAreReturnedWhenNoRelevantResults(){
//        assertTrue(textSearchEngine.search("gibberish").isEmpty());
//    }
//
//    @Test
//    public void ensureGetResultWorksAsExpected() throws Exception{
//        Note note = new Note();
//        note.setContent("test test test");
//        Class[] params = new Class[2];
//        params[0] = Note.class;
//        params[1] = String[].class;
//        Method getResult = SearchServiceImpl.class.getDeclaredMethod("getResult", params);
//        getResult.setAccessible(true);
//        SearchResult<Note> result = (SearchResult<Note>) getResult.invoke(textSearchEngine, note, new String[]{"test"});
//        assertEquals(result.getScore(), 3);
//    }
//}
