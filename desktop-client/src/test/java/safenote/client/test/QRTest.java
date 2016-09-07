package safenote.client.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import safenote.client.utils.FileIO;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class QRTest {


    @Before
    @After
    public void clearFileSystem() throws IOException {
        delete(new File(System.getProperty("user.home") + "/.safenote"));

    }

    public static void delete(File file) {
        if(file.isDirectory()){
            if(file.list().length==0){
                file.delete();
            }else{
                String files[] = file.list();
                for (String temp : files) {
                    File fileDelete = new File(file, temp);
                    delete(fileDelete);
                }
                if(file.list().length==0){
                    file.delete();
                }
            }
        }else{
            file.delete();
        }
    }

    @Test
    public void qrCodeIsWrittenAndReadCorrectly(){
        String test = "test";
        FileIO.write(test.getBytes());
        String result = new String(FileIO.read());
        assertEquals(test, result);
    }
}
