import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utilities {
    public static void receiveFile(InputStream is, File file) throws FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        byte[] buffer = new byte[1024];
        int count;
        try {
            while ((count = is.read(buffer)) > 0) {
                bos.write(buffer, 0, count);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            bos.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            bos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }


    public static void sendFile(OutputStream os, File file) throws FileNotFoundException {
        byte[] buffer = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        int count;
        try {
            while ((count = bis.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            os.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        try {
            bis.close();
        } catch (IOException e) {
            System.out.println("File reader not closed: " + e.getMessage());
        }
    }
    /* 
     * >InStream
     * >File 
     * 
     * reader
     * fWriter
     * 
     * buffer
     * 
     * !receive Filename
     * !receive fileSize
     * 
     * bytesRead
     * 
     * loop (bytesRead < fileSize) {
     *      !receive numberOfBytes
     *      !receive bytes to buffer
     *      !write from buffer to file
     *      !update bytesRead
     * }
     * 
    */

    /* 
     * >OutStream
     * >File
     * 
     * 
     * 
     * 
     * 
     */
}
