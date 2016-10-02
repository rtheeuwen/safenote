package nl.safenote.app;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import com.google.gson.Gson;
import nl.safenote.controllers.AuthenticationController;
import nl.safenote.controllers.NoteController;
import nl.safenote.model.Note;
import nl.safenote.services.*;
import nl.safenote.utils.FileIO;
import nl.safenote.utils.textsearch.TextSearchEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;

import javax.crypto.Cipher;

class Launcher {

	private static Logger logger = LoggerFactory.getLogger("info");

	public static void main(String[] args) throws Exception {
		enableCrypto();
		Config config = new Config();
		Properties properties = config.properties();
		ExecutorService executorService = config.executorService();

		CryptoService cryptoService = instantiate(CryptoService.class);
		TextSearchEngine<Note> textSearchEngine = instantiate(TextSearchEngine.class);
		NoteRepository noteRepository = instantiate(NoteRepository.class, new Sql2o(config.dataSource(properties)));
		SynchronizationService synchronizationService = instantiate(SynchronizationService.class, properties, noteRepository, cryptoService, executorService, new Gson());
		AuthenticationService authenticationService = instantiate(AuthenticationService.class, cryptoService, synchronizationService);
		AuthenticationController authenticationController = new AuthenticationController(authenticationService);
		NoteController noteController = new NoteController(noteRepository, cryptoService, textSearchEngine, synchronizationService);

		try {
			new View(authenticationController, noteController).open(!FileIO.dataExists());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			executorService.shutdown();
		}
	}

	private static <T> T instantiate(Class clazz, Object... params) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
		Class<?> service = Class.forName(clazz.getCanonicalName() + "Impl");
		Constructor<?> constructor = service.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		return (T) constructor.newInstance(params);
	}

	private static void enableCrypto() {
		try {
			if (Cipher.getMaxAllowedKeyLength("AES") == 128) {
				Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
				field.setAccessible(true);
				Field modifiers = Field.class.getDeclaredField("modifiers");
				modifiers.setAccessible(true);
				modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
				field.set(null, Boolean.FALSE);
			}
		} catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException | NoSuchAlgorithmException e) {
			throw new AssertionError("You need to install the Java cryptography extension in order to run SafeNote.");
		}
	}
}
