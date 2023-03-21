/**
 * user
 */

 import java.io.IOException;
 import java.util.LinkedHashMap;
 
 
 
 // classe User para ser usada pelo servidor para poupar acesso a ficheiros
 public class User {
     
     private String name;
     private float balance;
     private LinkedHashMap<Integer,Wine> seller;
     private String password;

     public User(String name, String password, float balance) throws IOException {
         this.name = name;
         this.balance = balance;
         this.password = password;
         this.seller = new LinkedHashMap<>();
 
     }
 
     public String getName() {
         return name;
     }
 
     public float getBalance() {
         return balance;
     }

     public String getPassword() {
    	return this.password;
    }

    public boolean validatePassword(String password) {
        return (password.equals(this.password));
    }

    public void setBalance(float bal) {
        this.balance = bal;
    }

    public void changeBalance(float bal) {
        this.balance+=bal;
    }

    public LinkedHashMap<Integer,Wine> getWines() {
        return seller;
    }

    // public void putSeller(String wine, int quantity, int value, User user){
    //     Wine w = new Wine(wine, user, this, amount, id);
    //     seller.put(id, p);
    // }
 }
 