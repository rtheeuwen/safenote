package nl.safenote.model;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class NoteTest {

    @Test
    public void equalsReturnsTrueWhenExpected(){
        Note one = createNote();
        assertReflexive(one);
        Note two = createNote();
        assertSymmetrical(one, two, true);
        assertTransitive(one, two, createNote(), true);
    }

    @Test
    public void equalsReturnsFalseWhenExpected(){
        Note one = createNote();
        Note two = createNote();
        Note three = createNote();
        three.setId(UUID.randomUUID().toString());

        assertSymmetrical(one, three, false);
        assertTransitive(one, two, three, false);

    }

    @Test
    public void equalsWorksForSubclass(){
        Note one = createNote();
        SubNote two = new SubNote(createNote());
        assertTrue(one.equals(two));
        assertTrue(two.equals(one));
    }

    @Test
    public void equalsDoesNotWorkForOtherClass(){
        assertFalse(createNote().equals("hello"));
    }

    private void assertReflexive(Note one){
        assertTrue(one.equals(one));
    }

    private void assertSymmetrical(Note one, Note two, boolean expected){
        if(expected) {
            assertTrue(one.equals(two));
            assertTrue(two.equals(one));
        } else {
            assertFalse(one.equals(two));
            assertFalse(two.equals(one));
        }
    }

    private void assertTransitive(Note one, Note two, Note three, boolean expected){
        if(expected) {
            if(one.equals(one.equals(two))&&two.equals(three))
            assertTrue(one.equals(three));
        } else {
            if(one.equals(one.equals(two))&&two.equals(three))
            assertFalse(one.equals(three));
        }
    }

    private Note createNote(){
        Note note = new Note("id", "header", Note.ContentType.TEXT);
        note.setContent("content");
        note.setModified("havetodothis");
        return note;
    }

    class SubNote extends Note{

        private String variable;

        public SubNote(Note note){
            this.setId(note.getId());
            this.setHeader(note.getHeader());
            this.setContent(note.getContent());
            this.setModified(note.getModified());
            this.setCreated(note.getCreated());
            this.variable = "test";
        }
    }
}
