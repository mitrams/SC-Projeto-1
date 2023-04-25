import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.security.PublicKey;
public class UserCatalog {

    private static Map<String,User> users;
    private File userFile;
    private static UserCatalog INSTANCE = new UserCatalog();

    public static UserCatalog getCatalog() {
        return INSTANCE;
    }

    private UserCatalog() {
        users = new HashMap<>();
        userFile = new File("users.txt");
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
    
    /**
     * Register a User to the Server
     * @param name - name of the User
     * @param pk - Public key of the User
     * @throws FileNotFoundException
     */
    public void registerUser(String name, File folder, PublicKey pk) throws FileNotFoundException {
   
        try {
            User user = new User(name, 200);
            users.put(name, user);
            Utilities.writePk(pk, folder, user.getFilename());
            writeFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Writes text file with usernames and passwords
     */
    public void writeFile() {
        try {
            FileWriter out = new FileWriter(this.userFile);

            for (var entry: users.entrySet()) {
                User u = entry.getValue();
                out.write(entry.getKey()+":"+u.getFilename()+"\n");
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
                      //  registerUser(content[0],content[1]);
                    }
                    in.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
  

}
