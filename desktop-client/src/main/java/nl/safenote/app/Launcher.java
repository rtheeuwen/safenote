package nl.safenote.app;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import nl.safenote.utils.FileIO;

import javax.crypto.Cipher;
import java.io.*;


class Launcher {

    public static void main(String[] args) throws Exception {
            enableCrypto();
            try {
                Properties properties = new Properties();
                properties.load(new ClassPathResource("/application.properties").getInputStream());
                int embeddedport = Integer.valueOf(properties.getProperty("embeddedport"));
                new Launcher().launch(properties);
                new Window().initializeApplication("http://localhost:"+embeddedport + "/safenote/" + (FileIO.dataExists()?"login.html":"newkey.html"));
            } catch (IOException e) {
                throw new AssertionError(e);
            }
    }

    private void launch(Properties properties) throws Exception {
        LoadingScreen loadingScreen = new LoadingScreen();
        Server server = new Server(Integer.valueOf(properties.getProperty("embeddedport")));
        server.setHandler(getServletContextHandler(getContext(properties.getProperty("profile"))));
        server.start();
        loadingScreen.done();
    }

    private static ServletContextHandler getServletContextHandler(WebApplicationContext context) {
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setErrorHandler(null);
        contextHandler.setContextPath("/");
        contextHandler.addServlet(new ServletHolder(new DispatcherServlet(context)), "/*");
        contextHandler.addEventListener(new ContextLoaderListener(context));
        return contextHandler;
    }

    private static WebApplicationContext getContext(String profile) {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(Config.class);
        context.getEnvironment().setActiveProfiles(profile);
        return context;
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
