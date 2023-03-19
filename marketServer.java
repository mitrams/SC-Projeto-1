/***************************************************************************
 *
 *   Seguranca e Confiabilidade 2020/21
 *
 ***************************************************************************/

 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;

import java.util.Scanner;
 
 //Servidor myServer
 
 public class marketServer {
	
	private UserCatalog uc = UserCatalog.getCatalog();
 
	 public static File userLog;

 
	 public static void main(String[] args) {
		 userLog = new File("userLog.txt");
 
		 if (!userLog.exists()) {
			 try {
				 userLog.createNewFile();
			 } catch (IOException e1) {
				 e1.printStackTrace();
				 System.exit(-1);
			 }
		 }
		 System.out.println("servidor: main");
		 marketServer server = new marketServer();
		 server.startServer(args);
	 }
 
	 public void startServer(String[] args) {
		 ServerSocket sSoc = null;
 
		 int port = 12345;
 
		 if (args.length != 0) {
			 try {
				 port = Integer.parseInt(args[0]);
			 } catch (NumberFormatException e) {
				 e.printStackTrace(System.err);
			 }
		 }
 
		 try {
			 sSoc = new ServerSocket(port);
		 } catch (IOException e) {
			 System.err.println(e.getMessage());
			 System.exit(-1);
		 }
 
		 System.out.println("Server a correr.");
 
		 while (true) {
			 try {
				 Socket inSoc = sSoc.accept();
				 System.out.println("Cliente conectado");
				 ServerThread newServerThread = new ServerThread(inSoc, userLog);
				 newServerThread.start();
			 } catch (IOException e) {
				 e.printStackTrace();
				 break;
			 }
 
		 }
		 try {
			 sSoc.close();
		 } catch (IOException e) {
			 e.printStackTrace();
		 }
	 }
 
	 // Threads utilizadas para comunicacao com os clientes
	 class ServerThread extends Thread {
 
		 private Socket socket = null;
 
		 ObjectOutputStream out = null;
		 ObjectInputStream in = null;
		 
		 File userLog;
 
		 ServerThread(Socket inSoc, File userLog) throws FileNotFoundException, IOException {
			 socket = inSoc;
			 this.userLog = userLog;
 
			 System.out.println("thread do server para cada cliente");
		 }
 
		 public void run() {
			 try {
				 out = new ObjectOutputStream(socket.getOutputStream());
				 in = new ObjectInputStream(socket.getInputStream());
 
				 String user = null;
				 String passwd = null;

				 User u;
 
				 try {
					 user = (String) in.readObject();
					 passwd = (String) in.readObject();
					 System.out.println("thread: depois de receber a password e o user");
				 } catch (ClassNotFoundException e1) {
					 e1.printStackTrace();
				 }
 
				 // TODO: refazer
				 // este codigo apenas exemplifica a comunicacao entre o cliente e o servidor
				 // nao faz qualquer tipo de autenticacao
 
				 LoginInfo login_db = null;
				 try {
					 login_db = new LoginInfo(userLog);
				 } catch (Exception e) {
					 // TODO: handle exception
				 }
 
				 System.out.println("Recebi: (utilizador: " + user + ", password: " + passwd + ")");

				 if(uc.containsUser(user)) {
					if(uc.validateUser(user,passwd)) {
						u = uc.getUser(user);
						out.writeObject(true);
					}
					else {
						System.out.println("erro: Password errada");
					 	out.writeObject(false);
					 	return;
					}
				 }
				 else {
					uc.registerUser(user, passwd);
					System.out.println("Utilizador criado");
					out.writeObject(true);
				 }
				 
				 runCommands(in, out);
 
			//	 fin.close();
			//	 output.close();
				 out.close();
				 in.close();
 
				 socket.close();
 
			 } catch (IOException e) {
				 e.printStackTrace();
			 }
 
		 }
		 private void runCommands(ObjectInputStream inStream, ObjectOutputStream outStream) {

			Boolean b = false;
			while (!b) {
				try {
					char command = (char) inStream.readObject();
					switch (command) {
						case 'a':
							break;
						case 's':
							sellWine(inStream, outStream);
							break;
						case 'v':
							break;
						case 'b':
							buyWine(inStream, outStream );
							break;
						case 'w':
							User user = new User(getName(), getName(), command);
							wallet(inStream, outStream, user);
							break;
						case 'c':
							break;
						case 't':
							
							break;
						case 'r':	
							break;
					}
				} catch (IOException e) {
					System.err.println(e.getMessage());
					System.exit(-1);
				} catch (ClassNotFoundException e) {
					System.err.println(e.getMessage());
					System.exit(-1);
				}
			}
		}
	
		private void wallet(ObjectInputStream inStream, ObjectOutputStream outStream, User user) {
			
		
			int bal = user.getBalance();
			try {
				outStream.writeObject(bal);
			} catch (IOException e) {

				e.printStackTrace();
			}
		
		}

		private void buyWine(ObjectInputStream inStream, ObjectOutputStream outStream) {
			try {
				String wine = (String) inStream.readObject();
				String selerId = (String) inStream.readObject();
				int quantity = (int) inStream.readObject();
				System.out.println("recebi instrução buy " +wine + " "+ selerId + " "+ quantity);

				outStream.writeObject(0);

				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}

		}

		private void sellWine(ObjectInputStream inStream, ObjectOutputStream outStream) {
			try {
				String wine = (String) inStream.readObject();
				Float value = (Float) inStream.readObject();
				int quantity = (int) inStream.readObject();
				System.out.println("recebi instrução sell " +wine + " "+ value + " "+ quantity);

				outStream.writeObject(0);

				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}
		}

		private String receiveString(ObjectInputStream inStream) {
			 Object received = null;
 
			 try {
				 received = inStream.readObject();
			 } catch (Exception e) {
				 return null;
			 }
 
			 if (!(received instanceof String)) {
				 return null;
			 }
 
			 return (String) received;
		 }
 
		 private synchronized void addComm(ObjectInputStream in) {
 
		 }
 
		 /**
		 * @param wine
		 * @param value
		 * @param quantity
		 * @return
		 */
/* 		private synchronized String sellComm(String wine, int value, int quantity, String command) {
					
		
			String[] tokens = command.split(" ");
		
			ArrayList<String> sellerContent = getFileContent(SELLWINES);

			try {
				wine = String.valueOf(Integer.parseInt(tokens[1]));
				value = Integer.parseInt(tokens[2]);
				quantity = Integer.parseInt(tokens[3]);

			} catch (NumberFormatException nfe) {
				return "Error, amount is not numeric";
			}
			if (value <= 0 || quantity <=0 ) {
				return "Error, amount must be positive";
			}
			
			if (sellerContent == null) {
				return "Error fetching file content.";
			}
	
		
			for (Wine w : sellerContent) {
				if (!w.getId().equals(wine)) {
					return "Error, wine does not exist.";

				}else{
					if (w.getQuatity() >= quantity) {	
						String sellInfo = "Wine: " + wine + ", Value: " + value + ", Quantity: " + quantity;
						w.sellWines().add(sellInfo);
						w.quantity -= quantity;
					} else {
						return "Error: Not enough " + wine + " to sell.";
					}
				}
			}

			String seller = "WineId:" + wine + "Value:" + value+ ":" + "Quantity:" + quantity ;
			boolean write = appendToFile(SELLWINES, seller);
			if (!write) {
				return "Error writing to file.";
			}

				return "Successfully added " + quantity + " units of " + wine + " to sell list for " + value + " each.";
 
		 }
  */
		 private synchronized void viewComm(Wine wine) {
 
		 }
 
		 private synchronized void buyComm() {
 
		 }
 
		 private synchronized void walletComm() {
 
		 }
 
		 private synchronized void classifyComm() {
 
		 }
 
		 private synchronized void talkComm() {
 
		 }
 
		 private synchronized void readComm() {
 
		 }
 
	 }
 

	 class LoginInfo {
 
		 private FileWriter out;
 
		 Scanner sc = null;
 
		 public LoginInfo(File userInfo) throws FileNotFoundException {
			 sc = new Scanner(userInfo);
			 System.out.println(userInfo);
 
			 try {
				 out = new FileWriter(userInfo, true);
			 } catch (Exception e) {
				 // TODO: handle exception
				 System.out.println("Error: Falha na saída");
				 System.exit(-1);
			 }
		 }
 
		 public boolean put(String user, String password) {
			 if (this.get(user) == null) {
				 try {
					 String line = user + ":" + password + '\n';
					 out.write(line);
					 out.flush();
					 System.out.println(line);
				 } catch (IOException e) {
					 e.printStackTrace();
					 return false;
				 }
 
				 return true;
			 }
 
			 
			 return false;
		 }
 
		 public String get(String user) {
 
			 String line;
			 String[] userPass;
			 System.out.println("Nova linha: " + sc.hasNextLine());
			 while (sc.hasNextLine()) {
				 line = sc.nextLine();
				 System.out.println(line + " vs " + user);
				 System.out.flush();
				 userPass = line.split(";");
 
				 if (user.equals(userPass[0])) {
					 return userPass[1];
				 }
 
			 }
 
			 return null;
		 }
 
	 }
 
	 /**
	  * wineDB
	  */
	 public class WineDB {
 
		 private Scanner sc;
		 private FileWriter out;
 
		 public WineDB(String filePath) throws FileNotFoundException {
			 File f = new File(filePath);
 
			 sc = new Scanner(f);
		 }
 
		 public Wine get(String wine) {
			 String line;
			 String[] wineInfo;
			 System.out.println("Nova linha:  " + sc.hasNextLine());
			 while (sc.hasNextLine()) {
				 line = sc.nextLine();
				 wineInfo = line.split(";");
 
				 if (wine.equals(wineInfo[0])) {
					 return new Wine(wine, Integer.parseInt(wineInfo[1]), Integer.parseInt(wineInfo[2]), wineInfo[3]);
				 }
 
			 }
 
			 return null;
		 }
 
		 public boolean put(String wine, int value, int quantity, String filePath) {
			 if (this.get(wine) == null) {
				 try {
					 String line = wine + ";" + value + ";" + quantity + ";" + filePath;
					 out.write(line);
					 out.flush();
					 System.out.println(line);
				 } catch (IOException e) {
					 e.printStackTrace();
					 return false;
				 }
 
				 return true;
			 }
 
			 return false;
		 }
 
	 }
 }