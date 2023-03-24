import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * wineDB
 */
public class WineDB {

    private Scanner sc;
    private FileWriter out;
    private File file;
    FileInputStream inputStream;

    public WineDB(String filePath) throws IOException {
        file = new File(filePath);

        if (!file.exists()) {
            file.createNewFile();
        }

        this.inputStream = new FileInputStream(file);

        out = new FileWriter(file, StandardCharsets.UTF_8, true);
        sc = new Scanner(inputStream);
    }

    public WineDB(File f) throws IOException {
        this.file = f;

        if (!file.exists()) {
            file.createNewFile();
        }

        out = new FileWriter(f, StandardCharsets.UTF_8, true);

        this.inputStream = new FileInputStream(file);
        sc = new Scanner(inputStream);
    }

    public synchronized Wine get(String wine) {
        String line;
        String[] wineInfo;
        // System.out.println("Has another line: " + sc.hasNextLine());
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            wineInfo = line.split(";");

            if (wine.equals(wineInfo[0])) {
                try {
                    inputStream.getChannel().position(0);
                    sc = new Scanner(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return new Wine(wine, wineInfo[1].equals("") ? -1 : Float.parseFloat(wineInfo[1]),
                        wineInfo[2].equals("") ? -1 : Integer.parseInt(wineInfo[2]), wineInfo[3], wineInfo[4]);
            }

        }

        try {
            inputStream.getChannel().position(0);
            sc = new Scanner(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public synchronized boolean put(String wine, int value, int quantity, String seller, File img) {
        if (this.get(wine) != null) {
            return false;
        }

        try {
            String line = wine + ";"
                    + (value == -1 ? "" : value) + ";"
                    + (quantity == -1 ? "" : quantity) + ";"
                    + (seller == null ? "" : seller) + ";"
                    + img.getAbsolutePath()
                    + '\n';

            out.write(line);
            out.flush();
            System.out.println(line);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}