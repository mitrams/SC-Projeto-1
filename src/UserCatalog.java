import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;

public class UserCatalog {

    private static Map<String,User> users;
    private File userFile;
    private static UserCatalog INSTANCE = new UserCatalog();

    private static final int SALT_SIZE = 8;
    private static final int ITERATIONS = 65536;
    private String fileHash = null;

    public static UserCatalog getCatalog() {
        return INSTANCE;
    }

    private UserCatalog() {
        users = new HashMap<>();
        userFile = new File("users.txt");
        try {
            if(!userFile.createNewFile()){
                fileHash = getFileHash(userFile);
                readFile();
            } else {
                encryptFile("sc", userFile);
                System.out.println("Ficheiro de Users criado.\n");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
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
            if(!getFileHash(userFile).equals(fileHash)) {
                System.out.println("O ficheiro foi corompido");
                return;
            }
            decryptFile("sc", userFile);
            FileWriter out = new FileWriter(this.userFile);

            for (var entry: users.entrySet()) {
                User u = entry.getValue();
                out.write(entry.getKey()+":"+u.getFilename()+"\n");
            }
            out.close();
            encryptFile("sc", userFile);

         } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Read from the User File
     */ 
    private void readFile() {
        try {
            if(!getFileHash(userFile).equals(fileHash)) {
                System.out.println("O ficheiro foi corompido");
                return;
            }
            decryptFile("sc", userFile);
            Scanner in = new Scanner(userFile);
            while (in.hasNextLine()) {
                String line = in.nextLine();
                String[] content = line.split(":");
                System.out.println(content[0]+" "+content[1]);
                //  registerUser(content[0],content[1]);
            }
            in.close();
            encryptFile("sc", userFile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void encryptFile(String password, File file) throws Exception {
         File tempFile = new File("temp.txt");
         String inputFile = file.getName();
         String outputFile = tempFile.getName();

        // Generate salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_SIZE];
        random.nextBytes(salt);
        
        // Generate key and initialization vector (IV) using password and salt
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, 128);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        SecretKey secretKey = keyFactory.generateSecret(pbeKeySpec);
        byte[] keyBytes = secretKey.getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        // Create cipher object and initialize with key and IV
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
        
        // Read input file and encrypt to output file
        FileInputStream inputStream = new FileInputStream(inputFile);
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        outputStream.write(salt);
        outputStream.write(iv);
        
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byte[] encrypted = cipher.update(buffer, 0, len);
            outputStream.write(encrypted);
        }
        byte[] encrypted = cipher.doFinal();
        outputStream.write(encrypted);
        
        // Close streams
        inputStream.close();
        outputStream.close();
        copyContent(tempFile, file);
        tempFile.delete();
        fileHash = getFileHash(userFile);
    }
    
    public void decryptFile(String password, File file) throws Exception {
        File tempFile = new File("temp.txt");
        String inputFile = file.getName();
        String outputFile = tempFile.getName();

        // Read salt and IV from input file
        FileInputStream inputStream = new FileInputStream(inputFile);
        byte[] salt = new byte[SALT_SIZE];
        inputStream.read(salt);
        byte[] iv = new byte[16];
        inputStream.read(iv);
        
        // Generate key from password and salt
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, 128);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        SecretKey secretKey = keyFactory.generateSecret(pbeKeySpec);
        byte[] keyBytes = secretKey.getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        
        // Create cipher object and initialize with key and IV
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        
        // Read input file and decrypt to output file
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byte[] decrypted = cipher.update(buffer, 0, len);
            outputStream.write(decrypted);
        }
        byte[] decrypted = cipher.doFinal();
        outputStream.write(decrypted);
        
        // Close streams
        inputStream.close();
        outputStream.close();
        copyContent(tempFile, file);
        tempFile.delete();
    }

    private static void copyContent(File a, File b) {
        try {
            FileInputStream in = new FileInputStream(a);
            FileOutputStream out = new FileOutputStream(b);
            int n;
            // read() function to read the
            // byte of data
            while ((n = in.read()) != -1) {
                out.write(n);
            }
            in.close();
            out.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private String getFileHash(File file) throws NoSuchAlgorithmException, IOException {
        byte[] data = Files.readAllBytes(Paths.get(file.getPath()));
        byte[] hash = MessageDigest.getInstance("MD5").digest(data);
        return new BigInteger(1, hash).toString(16);
    }

}
