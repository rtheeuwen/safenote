package nl.safenote.integrationtests;

import nl.safenote.app.Config;
import nl.safenote.controllers.AuthenticationController;
import nl.safenote.services.AuthenticationService;
import nl.safenote.services.CryptoService;
import nl.safenote.services.NoteRepository;
import nl.safenote.services.SynchronizationService;
import nl.safenote.utils.textsearch.TextSearchEngine;
import org.junit.*;
import org.junit.runners.MethodSorters;
import nl.safenote.controllers.NoteController;
import nl.safenote.model.Header;
import nl.safenote.model.Note;
import nl.safenote.testutils.TestHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest {

	private static String firstCreatedNoteId;
	private static String secondCreatedNoteId;
	private static Note firstCreatedNote;
	private static Note secondCreatedNote;


	static AuthenticationController authenticationController;
	static NoteController noteController;
	static ExecutorService executorService;

	@BeforeClass
	@AfterClass
	public static void delete() throws Exception {
		new TestHelper().clearFileSystem();
	}

	@BeforeClass
	public static void init() throws Exception {
		Config config = new Config();
		executorService = config.executorService();

		CryptoService cryptoService = instantiate(CryptoService.class);
		TextSearchEngine<Note> textSearchEngine = instantiate(TextSearchEngine.class);
		NoteRepository noteRepository = instantiate(NoteRepository.class, config.sql2o());
		SynchronizationService synchronizationService = instantiate(SynchronizationService.class, config.properties(), noteRepository, cryptoService, executorService, config.gson());
		AuthenticationService authenticationService = instantiate(AuthenticationService.class, cryptoService, synchronizationService);
		authenticationController = new AuthenticationController(authenticationService);
		noteController = new NoteController(noteRepository, cryptoService, textSearchEngine, synchronizationService);
	}

	@After
	public void shutdown() {
		executorService.shutdown();
	}

	private static <T> T instantiate(Class clazz, Object... params) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
		Class<?> service = Class.forName(clazz.getCanonicalName() + "Impl");
		Constructor<?> constructor = service.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		return (T) constructor.newInstance(params);
	}

	@Test
	public void A_UserCanGeneratePassword() {
		assertTrue(authenticationController.authenticate("password"));
	}

	@Test
	public void C_UserCanCreateNote() {
		firstCreatedNoteId = noteController.createNote();
	}

	@Test
	public void D_UserCanFindOneNote() {
		firstCreatedNote = noteController.getNote(firstCreatedNoteId);
		assertEquals(firstCreatedNote.getId(), firstCreatedNoteId);
		assertEquals(firstCreatedNote.getHeader(), "New note...");
	}

	@Test
	public void E_UserCanUpdateANote() {
		firstCreatedNote.setContent("content");
		Note copyOfFirst = (Note) firstCreatedNote.clone();
		copyOfFirst.setHeader("content");
		noteController.updateNote(firstCreatedNote);
		firstCreatedNote = noteController.getNote(firstCreatedNoteId);
		assertEquals(firstCreatedNote, copyOfFirst);
	}

	@Test
	public void F_UserCanCreateAnotherNote() {
		secondCreatedNoteId = noteController.createNote();
		secondCreatedNote = noteController.getNote(secondCreatedNoteId);
		secondCreatedNote.setContent("safenote");
		noteController.updateNote(secondCreatedNote);
	}

	@Test
	public void G_UserGetsAllHeaders() {
		List<Header> headers = noteController.getHeaders();
		assertEquals(headers.size(), 2);
		String firstId = headers.get(0).getId();
		String secondId = headers.get(1).getId();
		assertTrue(firstCreatedNoteId.equals(firstId) || firstCreatedNoteId.equals(secondId));
		assertTrue(secondCreatedNoteId.equals(firstId) || secondCreatedNoteId.equals(secondId));
	}

	@Test
	public void H_UserCanSearchForANote() {
		List<Header> headers = noteController.search("safenote");
		assertTrue(headers.get(0).getId().equals(secondCreatedNoteId));
	}

	@Test
	public void I_UserCanDeleteANote() {
		noteController.deleteNote(firstCreatedNoteId);
		List<Header> headers = noteController.getHeaders();
		assertTrue(headers.get(0).getId().equals(secondCreatedNoteId));
	}

	@Test
	public void J_UserCanSynchronize() {
		noteController.synchronize();
	}

	@Test
	public void K_UserCanGetInfo() throws Exception {
		String license = noteController.info();
		assertTrue(license.contains("GNU GENERAL PUBLIC LICENSE"));
	}

}
