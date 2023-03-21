import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import javax.swing.text.html.HTMLDocument.Iterator;

public class UserCatalog {

    private Map<String,User> users;
    private File userFile;
    private File userDB;
    private static UserCatalog INSTANCE = new UserCatalog();

    public static UserCatalog getCatalog() {
        return INSTANCE;
    }

    private UserCatalog() {
        users = new HashMap<>();
        userFile = new File("userLog.txt");
       // userDB = new File("userDB.txt");
        readFile();
    }

    /**
     * Verify if the User exists in the Server
     * @param username - User to be found
     * @return true if he exists in the Server
     */
    public boolean containsUser(String username) {
        return users.containsKey(username);  
    }

    /**
     * Get the wanted User
     * @param username - User wanted
     * @return
     */
    public User getUser(String username) {
        return users.get(username);
    }

    public boolean validateUser(String username, String password) {
        if (this.containsUser(username)) {
            return users.get(username).validatePassword(password);
        }
        else
            return false;

    }
    

    /**
     * Register a User to the Server
     * @param name - name of the User
     * @param password - Password of the User
     * @throws FileNotFoundException
     */
    public void registerUser(String name, String password) throws FileNotFoundException {
   
        try {
            User user = new User(name, password, 200);
            users.put(name, user);
            writeFile();
        //    refreshFile(user);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Writes texy file with usernames and passwords
     */
    private void writeFile() {
        System.out.println("WriteFile");
        try {
            FileWriter out = new FileWriter(this.userFile);

            for (var entry: users.entrySet()) {
                User u = entry.getValue();
                out.write(entry.getKey()+":"+u.getPassword()+"\n");
            }
            out.close();

         } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read from the User File
     */ 
    private void readFile() {
        try {
            if(userFile.createNewFile()) {
                System.out.println("Ficheiro de Users criado.\n");
                return;
            }
            else {
                    Scanner in = new Scanner(userFile);
                    while (in.hasNextLine()) {
                        String line = in.nextLine();
                        String[] content = line.split(":");
                        System.out.println(content[0]+" "+content[1]);
                        registerUser(content[0],content[1]);
                    }
                    in.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

  

}