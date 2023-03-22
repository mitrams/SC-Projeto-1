import java.io.Serializable;

public class Listing  implements Serializable {
    private  String seller;
    private Float value;
    private int quantity;

    public Listing(String seller, Float value, int quantity){
        this.seller= seller;
        this.value = value;
        this.quantity = quantity;
    }

	public String getSeller() {
		return seller;
	}
    
	public Float getValue() {
		return value;
	}
    
	public int getQuantity() {
		return quantity;
	}

	public void sellQuantity(int quantity) {
		this.quantity-=quantity;
	}


}
