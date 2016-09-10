package safenote.client.utilstest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import safenote.client.testutils.TestHelper;
import safenote.client.utils.FileIO;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class QRTest {


    @Before
    @After
    public void clearFileSystem() throws IOException {
        new TestHelper().clearFileSystem();

    }

    @Test
    public void qrCodeIsWrittenAndReadCorrectly(){
        String test = "test";
        FileIO.write(test.getBytes());
        String result = new String(FileIO.read());
        assertEquals(test, result);
    }
}
