// classe User para ser usada pelo servidor para poupar acesso a ficheiros
 public class User {
     
    private String name;
    //private float balance;
    private String password;
    private String filename;

    public User(String name, float balance) {
        this.name = name;
        //    this.balance = balance;
        this.filename = name;
    }
 
    public String getName() {
        return name;
    }
     
	public String getFilename() {
		return this.filename;
	}

    public String getPassword() {
    	return this.password;
    }
 }
 