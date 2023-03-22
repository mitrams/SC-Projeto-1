import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Wallets implements Serializable{
    private Map<String,Float> userWallets;
    private static Wallets INSTANCE = new Wallets();
    private String userWalletsDB;

    public static Wallets getCatalog() {
        return INSTANCE;
    }

    public Wallets(){
        this.userWallets = new HashMap<>();
        userWalletsDB =  "WalletsDB.txt";
        readFile();
    }

    public boolean validateWallet(String user) {
        return this.userWallets.containsKey(user);  
    }

    public float getBalance(String user) {
        if(!validateWallet(user))
            return 0;
        else
            return this.userWallets.get(user);
    }

    public void setBalance(String user, float balance) {
        this.userWallets.put(user, balance);
        writeFile();
    }

    public void changeBalance(String user, float balance) {
        this.userWallets.put(user,getBalance(user)+balance);
        writeFile();
    }

    public void writeFile() {
		try {
			FileOutputStream objDB = new FileOutputStream(this.userWalletsDB);
            ObjectOutputStream out = new ObjectOutputStream(objDB);
			out.writeObject(userWallets);
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
            FileInputStream inFile = new FileInputStream(this.userWalletsDB);
            ObjectInputStream in = new ObjectInputStream(inFile);

            try {
                userWallets=  (Map<String,Float>) in.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            in.close();
            inFile.close();
        }
        catch (IOException e ) {
            if(e instanceof FileNotFoundException){
                System.out.println("Não foi encontrado o ficheiro WalletsDB.txt");
            }else{
                System.out.println("Erro a processar o ficheiro WalletsDB.txt");
            }
        }
    }

}


