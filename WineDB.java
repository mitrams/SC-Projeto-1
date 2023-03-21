import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * wineDB
 */
public class WineDB {

    private Scanner sc;
    private FileWriter out;

    public WineDB(String filePath) throws IOException {
        File f = new File(filePath);

        out = new FileWriter(f);
        sc = new Scanner(f);
    }

    public WineDB(File f) throws IOException {
        out = new FileWriter(f);
        sc = new Scanner(f);
    }

    public Wine get(String wine) {
        String line;
        String[] wineInfo;
        System.out.println("Has another line: " + sc.hasNextLine());
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            wineInfo = line.split(";");

            if (wine.equals(wineInfo[0])) {
                return new Wine(wine, Integer.parseInt(wineInfo[1]), Integer.parseInt(wineInfo[2]), wineInfo[3]);
            }

        }

        return null;
    }

    public boolean put(String wine, int value, int quantity, String seller, File img) {
        if (this.get(wine) == null) {
            try {
                String line = wine + ";" 
                            + (value == -1? "" : value) + ";" 
                            + (quantity == -1? "" : quantity) + ";" 
                            + (seller == null? "" : seller) + ";" 
                            + img.getAbsolutePath();

                out.write(line);
                out.flush();
                System.out.println(line);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        return false;
    }

}