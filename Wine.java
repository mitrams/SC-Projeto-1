import java.util.ArrayList;
import java.util.List;


/**
 * wine
 */
public class Wine {
    private String name;
    private int stars = 0;
    private String imgPath;
    private ArrayList<Listing> listings;

    public Wine(String name, String imgPath) {
        this.name = name;
        this.imgPath = imgPath;
        this.listings = new ArrayList<>();
    }

    public List<Listing> getListings() {
      return listings;
    }

	public String getName() {
		return name;
	}

	
	public String getImgPath() {
		return imgPath;
	}

  public void classify(int stars){
    this.stars=stars;
  }

  public int getStars() {
		return this.stars;
	}

  public void addListing(String seller, float value, int quantity){
    Listing listing = new Listing(seller, value, quantity);
    this.listings.add(listing);
  }

  public Listing getSellerListing(String seller) {
    for(Listing lc : this.listings) {
      if(lc.getSeller().equals(seller)) {
        return lc;
      }
    }
    return null;
  }


   // public String sellerId(){
     //   return sellerId();
   // }
}
