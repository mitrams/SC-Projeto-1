import java.util.ArrayList;

/**
 * wine
 */
public class Wine {
    String id;
    int value;
    int quantity;
    String imgPath;
    private ArrayList<String> sellWines;

    public Wine(String id, int value, int quantity, String imgPath) {
        this.id = id;
        this.value = value;
        this.quantity = quantity;
        this.imgPath = imgPath;

        this.sellWines = new ArrayList<>();
    }

	public String getId() {
		return id;
	}
	public int getValue() {
		return value;
	}
	
    public int getQuatity() {
		return quantity;
	}
	
	public String getImgpath() {
		return imgPath;
	}

  public ArrayList<String> sellWines() {
		return sellWines;
	}

   // public String sellerId(){
     //   return sellerId();
   // }
}
