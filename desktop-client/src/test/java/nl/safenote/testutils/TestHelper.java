package nl.safenote.testutils;


import java.io.File;
import java.io.IOException;

public class TestHelper {

    public void clearFileSystem() throws IOException {
        delete(new File(System.getProperty("user.home") + "/.safenote"));

    }

    private void delete(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                if (file.list().length == 0) {
                    file.delete();
                } else {
                    String files[] = file.list();
                    for (String temp : files) {
                        File fileDelete = new File(file, temp);
                        delete(fileDelete);
                    }
                    if (file.list().length == 0) {
                        file.delete();
                    }
                }
            } else {
                file.delete();
            }
        }
    }
}
