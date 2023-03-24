
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WineCatalog  implements Serializable{

    private Map<String,Wine> wines;
    private static WineCatalog INSTANCE = new WineCatalog();
    private String wineCatalogDB;

    public static WineCatalog getCatalog() {
        return INSTANCE;
    }

    public WineCatalog(){
        this.wines = new HashMap<>();
        wineCatalogDB =  "wineCatalogDB.txt";
        readFile();
    }

    /**
     * Adds a new {@code Wine} to the catalog
     * @param wine name of the wine
     * @param img path to the image of the wine
     * 
     */
    public void addWine(String wine, String img){
        Wine w = new Wine(wine, img);
        this.wines.put(wine, w);
    }


    /**
     * @param name
     * @return Whether or not 'name' is the name of an existing Wine
     */
    public boolean validateWine(String name) {
        return wines.containsKey(name);  
    }

    public Wine getWine(String name){
        return wines.get(name);
    }

    /**
     * Classifica o vinho de nome name com stars
     * retorna false se vinho nao existir
     * @param name
     * @param stars
     * @return
     */
    public boolean classifyWine(String name, int stars){
        if(!validateWine(name)){
            return false;
        }
        Wine w = wines.get(name);
        w.classify(stars);
        return true;
    }

    public void addListing(String name, String seller, float value, int quantity ){
        Wine w = getWine(name);
        w.addListing(seller, value, quantity);
    }

    public void print() {
        for (var entry: wines.entrySet()) {
            Wine w = entry.getValue();
            System.out.println(entry.getKey()+":"+w.getImgPath()+":"+w.getStars());
            List<Listing> l = w.getListings();
            for(Listing lc : l) {
                System.out.println(lc.getSeller()+":"+lc.getValue()+":"+lc.getQuantity());
            }
        }
    }

    public void writeFile() {
		try {
		
			FileOutputStream objDB = new FileOutputStream(this.wineCatalogDB);
            ObjectOutputStream out = new ObjectOutputStream(objDB);
			out.writeObject(wines);
			out.close();
            objDB.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
        /**
     * Read from the User File
     */ 
    private void readFile() {
        try {
            FileInputStream inFile = new FileInputStream(this.wineCatalogDB);
            ObjectInputStream in = new ObjectInputStream(inFile);

            try {
                wines= (Map<String, Wine>) in.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            in.close();
            inFile.close();
        }
        catch (IOException e ) {
            if(e instanceof FileNotFoundException){
                System.out.println("Não foi encontrado o ficheiro wineCatalogDb.txt");
            }else{
                System.out.println("Erro a processar o ficheiro wineCatalogDb.txt");
            }
        }
    }

}