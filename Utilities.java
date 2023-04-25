import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.PublicKey;

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

        try {
            while ((count = bis.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }
        } finally {
            fis.close();
            bis.close();
        }
    }

    public static void writePk(PublicKey pk, File folder, String filename) throws IOException {
        File filePk = new File(folder, filename);
        FileOutputStream fos = new FileOutputStream(filePk);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        try {
            oos.writeObject(pk);
        } finally {
            oos.close();
            fos.close();
        }
    }

    public static PublicKey readPk(File folder, String filename) throws IOException {
        PublicKey pk=null;
        File filePk = new File(folder, filename);
        FileInputStream fis = new FileInputStream(filePk);
        ObjectInputStream ois = new ObjectInputStream(fis);    

        try {
            pk = (PublicKey)ois.readObject();
        }
        catch (Exception e) {
			e.printStackTrace();
            ois.close();
            fis.close();
            return null;
        }
        ois.close();
        fis.close();
        return pk;
    }
}