
 import java.io.IOException;

 
 
 
 // classe User para ser usada pelo servidor para poupar acesso a ficheiros
 public class User {
     
     private String name;
     //private float balance;
     private String password;

     public User(String name, String password, float balance) throws IOException {
         this.name = name;
     //    this.balance = balance;
         this.password = password;
  
     }
 
     public String getName() {
         return name;
     }
 
    // public float getBalance() {
    //     return balance;
    // }

     public String getPassword() {
    	return this.password;
    }

    public boolean validatePassword(String password) {
        return (password.equals(this.password));
    }

    // public void setBalance(float bal) {
    //     this.balance = bal;
    // }

    // public void changeBalance(float bal) {
    //     this.balance+=bal;
    // }


 }
 