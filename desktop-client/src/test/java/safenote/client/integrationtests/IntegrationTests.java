package safenote.client.integrationtests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import safenote.client.api.AuthenticationController;
import safenote.client.api.NoteController;
import safenote.client.app.Config;
import safenote.client.model.Header;
import safenote.client.model.Note;
import safenote.client.testutils.TestHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( classes = {safenote.client.integrationtests.fakeConfig.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTests {

    public static String firstCreatedNoteId;
    public static String secondCreatedNoteId;
    public static Note firstCreatedNote;
    public static Note secondCreatedNote;


    @Autowired
    AuthenticationController authenticationController;

    @Autowired
    NoteController noteController;

    @BeforeClass
    @AfterClass
    public static void delete() throws Exception{
        new TestHelper().clearFileSystem();
    }

    @Test
    public void A_UserCanGeneratePassword(){
        authenticationController.authenticate("password");
    }

    @Test
    public void B_UserCanAuthenticate(){
        authenticationController.authenticate("password");
    }

    @Test
    public void C_UserCanCreateNote(){
        firstCreatedNoteId = noteController.createNote("Note1");
    }

    @Test
    public void D_UserCanFindOneNote(){
        firstCreatedNote = noteController.getNote(firstCreatedNoteId);
        assertEquals(firstCreatedNote.getId(), firstCreatedNoteId);
        assertEquals(firstCreatedNote.getHeader(), "Note1");
    }

    @Test
    public void E_UserCanUpdateANote(){
        firstCreatedNote.setContent("content");
        noteController.updateNote(firstCreatedNoteId, firstCreatedNote);
        Note retrieved = noteController.getNote(firstCreatedNoteId);
        assertTrue(firstCreatedNote.equals(retrieved));
    }

    @Test
    public void F_UserCanCreateAnotherNote(){
        secondCreatedNoteId = noteController.createNote("Note2");
        secondCreatedNote = noteController.getNote(secondCreatedNoteId);
        secondCreatedNote.setContent("safenote");
        noteController.updateNote(secondCreatedNoteId, secondCreatedNote);
    }

    @Test
    public void G_UserGetsAllHeaders(){
        List<Header> headers = noteController.getHeaders();
        assertEquals(headers.size(), 2);
        String firstId = headers.get(0).getId();
        String secondId = headers.get(1).getId();
        assertTrue(firstCreatedNoteId.equals(firstId)||firstCreatedNoteId.equals(secondId));
        assertTrue(secondCreatedNoteId.equals(firstId)||secondCreatedNoteId.equals(secondId));    }

    @Test
    public void H_UserCanSearchForANote(){
        List<Header> headers = noteController.search("safenote");
        assertTrue(headers.get(0).getId().equals(secondCreatedNoteId));
    }

    @Test
    public void I_UserCanDeleteANote(){
        noteController.deleteNote(firstCreatedNoteId);
        List<Header> headers = noteController.getHeaders();
        assertTrue(headers.get(0).getId().equals(secondCreatedNoteId));
    }

    @Test
    public void J_UserCanSynchronize(){
        noteController.synchronize();
    }

    @Test
    public void K_UserCanGetInfo() throws Exception{
        String license = noteController.info();
        assertTrue(license.contains("GNU GENERAL PUBLIC LICENSE"));
    }

}

@Configuration
class fakeConfig extends Config {
    public fakeConfig(){
        super(null);
    }
}