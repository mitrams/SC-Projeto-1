import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utilities {
    public static void receiveFile(InputStream is, File file, long numBytes) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        byte[] buffer = new byte[1024];
        int count;
        long totalBytesRead = 0;
        try {
            while (totalBytesRead < numBytes) {
                long remainingBytes = numBytes - totalBytesRead;

                count = remainingBytes <= buffer.length ? is.read(buffer, 0, (int) remainingBytes) : is.read(buffer);

                bos.write(buffer, 0, count);
                totalBytesRead += count;

                // System.out.println("ReadTotal: " + totalBytesRead + " vs " + numBytes + " bytes");

            }
        } finally {
            bos.flush();
            fos.flush();

            bos.close();
            fos.close();
        }

    }

    public static void sendFile(OutputStream os, File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        byte[] buffer = new byte[1024];
        int count;
        long maxBytes = file.length();
        long readBytes = 0;

        try {
            while ((count = bis.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }
        } finally {
            fis.close();
            bis.close();
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
     * !receive numberOfBytes
     * !receive bytes to buffer
     * !write from buffer to file
     * !update bytesRead
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
