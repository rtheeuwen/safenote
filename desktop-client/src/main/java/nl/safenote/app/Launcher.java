package nl.safenote.app;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import nl.safenote.api.AuthenticationController;
import nl.safenote.api.NoteController;
import nl.safenote.model.Note;
import nl.safenote.services.CryptoService;
import nl.safenote.utils.FileIO;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.crypto.Cipher;


class Launcher {

    public static void main(String[] args) throws Exception {
            enableCrypto();

        long time = System.currentTimeMillis();
        LoadingScreen loadingScreen = new LoadingScreen();

        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(Config.class);
        loadingScreen.done();
        System.out.println("time elapsed: " + (System.currentTimeMillis() - time));
                Properties properties = new Properties();
            System.out.println("Crypto: " + applicationContext.getBean(CryptoService.class));
        System.out.println("Controller: " + applicationContext.getBean(NoteController.class));
        System.out.println("AuthContr: " + applicationContext.getBean(AuthenticationController.class));
                new View(applicationContext.getBean(AuthenticationController.class),
                        applicationContext.getBean(NoteController.class)).open(!FileIO.dataExists());

    }

    private static void enableCrypto(){
        try {
            if(Cipher.getMaxAllowedKeyLength("AES")==128) {
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
