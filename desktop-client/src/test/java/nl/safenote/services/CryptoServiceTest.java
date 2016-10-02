package nl.safenote.services;

import nl.safenote.model.KeyStore;
import nl.safenote.utils.KeyUtils;
import org.junit.Before;
import org.junit.Test;
import nl.safenote.model.Message;
import nl.safenote.model.Note;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

import static org.junit.Assert.assertEquals;

public class CryptoServiceTest {

	private final CryptoService cryptoService = new CryptoServiceImpl();
	private PublicKey publicKey;

	@Before
	public void init() {
		KeyStore keyStore = KeyUtils.keyStoreFromByteArray(KeyUtils.generateKeyStore(new SecureRandom()));
		cryptoService.init(keyStore.getAes(), keyStore.getHmac(), keyStore.getPrivateKey());
		this.publicKey = keyStore.getPublicKey();
	}


	@Test
	public void noteEncryptionIsDoneCorrectly() {
		doEncryption("header", "content");
	}

	@Test
	public void noteEncryptionIsDoneCorrectlyForNoteWithoutContent() {
		doEncryption("header", "");
	}

	@Test
	public void headerOnlyDecryptionIsDoneCorrectly() {
		Note note = new Note("id", Note.ContentType.TEXT);
		note.setHeader("header");
		note.setContent("content");

		cryptoService.encipher(note);
		cryptoService.decipher(note);
		assertEquals(note.getHeader(), "header");
	}

	@Test
	public void checksumIsDoneCorrectly() {
		Note note = new Note("id", Note.ContentType.TEXT);
		note.setHeader("header");
		note.setContent("content");
		assertEquals(cryptoService.checksum(note), cryptoService.checksum(note));
	}

	@Test
	public void signingIsDoneCorrectly() throws Exception {
		Note note = new Note("id", Note.ContentType.TEXT);
		note.setHeader("header");
		note.setContent("content");
		cryptoService.encipher(note);

		Message<Note> message = new Message<>(note, 0, "signee");
		cryptoService.sign(message);

		byte[] data = (Long.valueOf(message.getExpires()).toString() + message.getSignee()).getBytes(StandardCharsets.UTF_8);
		byte[] claimedSignature = DatatypeConverter.parseBase64Binary(message.getSignature());
		Signature signature = Signature.getInstance("SHA512withRSA");
		signature.initVerify(publicKey);
		signature.update(data);
		signature.verify(claimedSignature);
	}

	private void doEncryption(String header, String content) {
		Note note = new Note("id", Note.ContentType.TEXT);
		note.setHeader(header);
		note.setContent(content);
		cryptoService.encipher(note);
		cryptoService.decipher(note);
		assertEquals(note.getContent(), content);
	}
}
