
public class Transaction {

    private TransactionType type;
    private String wineID;
    private long quantity;
    private Float value;
    private String userID;

    public Transaction(TransactionType type, String wineID, long quantity, Float value, String userID) {
        this.type = type;
        this.wineID = wineID;
        this.quantity = quantity;
        this.value = value;
        this.userID = userID;
    }

    public TransactionType getType() {
        return type;
    }
    public void setType(TransactionType type) {
        this.type = type;
    }
    public String getWineID() {
        return wineID;
    }
    public void setWineID(String wineID) {
        this.wineID = wineID;
    }
    public long getQuantity() {
        return quantity;
    }
    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }
    public Float getValue() {
        return value;
    }
    public void setValue(Float value) {
        this.value = value;
    }
    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }

    

}
