import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WineCatalog{

    private Map<String,Wine> wines;
    private static WineCatalog INSTANCE = new WineCatalog();

    public static WineCatalog getCatalog() {
        return INSTANCE;
    }

    
    /**
     * 
     */
    public WineCatalog(){
        this.wines = new HashMap<>();

    }



    public void addWine(String wine, String img){
        Wine w = new Wine(wine, img);
        this.wines.put(wine, w);

    }

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



}