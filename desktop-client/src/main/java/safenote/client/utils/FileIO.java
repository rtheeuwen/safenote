package safenote.client.utils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;


/**
 * Provides file operations
 * @Author Roel Theeuwen
 * @Verion 1.0
 * @Since 2016-09-04
 */
public final class FileIO {

    private FileIO(){
        throw new AssertionError();
    }

    private static final String homedir = System.getProperty("user.home") + "/.safenote";

    public static boolean dataExists(){
        return new File(homedir + "/key.png").exists();
    }


    /**
     * Generates and writes a QR code for the local keystore.
     * This QR code can be interpreted by a mobile device easily and provides a safe way to transfer cryptographic keys
     * without using a network.
     * @param data Keystore
     */
    public static void write(byte[] data) {

        File keyStore = new File(homedir + "/key.png");
        if (!dataExists()) {
            assert keyStore.mkdirs();
        }
        int size = 700;
        try {
            Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(DatatypeConverter.printBase64Binary(data), BarcodeFormat.QR_CODE, size,
                    size, hintMap);

            int width = bitMatrix.getWidth();
            BufferedImage image = new BufferedImage(width, width,
                    BufferedImage.TYPE_INT_RGB);
            image.createGraphics();

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, width);
            graphics.setColor(Color.BLACK);

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < width; j++) {
                    if (bitMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            ImageIO.write(image, "png", keyStore);
        } catch (WriterException | IOException e) {
            throw new AssertionError();
        }
    }

    /**
     * The keystore is stored as a QR-code, this method is called during application startup
     * @return keystore
     */
    public static byte[] read() {
        try {
            String filePath = homedir + "/key.png";
            Map<DecodeHintType, Object> hintMap = new EnumMap<>(DecodeHintType.class);
            hintMap.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            hintMap.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                    new BufferedImageLuminanceSource(
                            ImageIO.read(new FileInputStream(filePath)))));
            Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);
            return DatatypeConverter.parseBase64Binary(qrCodeResult.getText());
        } catch (IOException | NotFoundException e) {
            throw new AssertionError();
        }
    }


    public static void export(String path) throws IOException {
        FileChannel source;
        FileChannel destination;
        if(path.endsWith("/")){
            path = path.substring(0, path.length()-1);
        }
        source = new FileInputStream(new File(homedir + "/key.png")).getChannel();
        destination = new FileOutputStream(new File(path+"/key.png")).getChannel();
        destination.transferFrom(source, 0, source.size());
    }

    public static void importData(String path) throws IOException{

        if(path.endsWith("/")){
            path = path.substring(0, path.length()-1);
        }

        File keyFile = new File(path + "/key");
        if(!keyFile.exists()){
            throw new FileNotFoundException();
        }
        FileChannel source;
        FileChannel destination;
        source = new FileInputStream(keyFile).getChannel();
        File keyStoreDir = new File(System.getProperty("user.home") + "/.safenote");
        if (!keyStoreDir.exists()) {
            assert keyStoreDir.mkdirs();
        }
        File keyStore = new File(keyStoreDir.getAbsolutePath() + "/key");
        if (!keyStore.exists()) {
            assert keyStore.createNewFile();
        }
        destination = new FileOutputStream(keyStore).getChannel();
        destination.transferFrom(source, 0, source.size());
    }

    public static void delete(){
        File file = new File(System.getProperty("user.home") + "/.safenote/key");
        file.delete();
    }
}
