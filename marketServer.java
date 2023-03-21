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

	private WineCatalog wc = WineCatalog.getCatalog();
 
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
					u = uc.getUser(user);
					out.writeObject(true);
				 }
				 
				 runCommands(in, out, u);
 
			//	 fin.close();
			//	 output.close();
				 out.close();
				 in.close();
 
				 socket.close();
 
			 } catch (IOException e) {
				 e.printStackTrace();
			 }
 
		 }
		 private void runCommands(ObjectInputStream inStream, ObjectOutputStream outStream, User u) {

			Boolean b = false;
			while (!b) {
				try {
					char command = (char) inStream.readObject();
					switch (command) {
						case 'a':
							addWine(inStream, outStream, u);
							break;
						case 's':
							sellWine(inStream, outStream, u);
							break;
						case 'v':
							break;
						case 'b':
							buyWine(inStream, outStream, u);
							break;
						case 'w':
							wallet(inStream, outStream, u);
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
			
		
			float bal = user.getBalance();
			try {
				outStream.writeObject(bal);
			} catch (IOException e) {

				e.printStackTrace();
			}
		
		}

		private void addWine(ObjectInputStream inStream, ObjectOutputStream outStream, User u) {
			try {
				String name = (String) inStream.readObject();
				String imgPath = (String) inStream.readObject();
				System.out.println("recebi instrução add " +name + " "+ imgPath);

				if(!wc.validateWine(name)) {
					wc.addWine(name,imgPath);
					try {
						outStream.writeObject(0);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else {
					try {
						outStream.writeObject(1);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}
		}

		private void buyWine(ObjectInputStream inStream, ObjectOutputStream outStream, User u) {
			try {
				String name = (String) inStream.readObject();
				String seller = (String) inStream.readObject();
				int quantity = (int) inStream.readObject();
				System.out.println("recebi instrução buy " +name + " "+ seller + " "+ quantity);

				if(!wc.validateWine(name)) {
					outStream.writeObject(1);
					return;
				}
				Wine w = wc.getWine(name);
				Listing l = w.getSellerListing(seller);
				if(l==null) {
					outStream.writeObject(2);
					return;
				}
				if(l.getQuantity()<quantity) {
					outStream.writeObject(3);
					return;
				}
				float trxValue=quantity*l.getValue();
				if(u.getBalance()<trxValue) {
					outStream.writeObject(4);
					return;
				}
				l.sellQuantity(quantity);
				u.changeBalance(-trxValue);
				uc.getUser(seller).changeBalance(trxValue);
				outStream.writeObject(0);		
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}
			wc.print();

		}

		private void sellWine(ObjectInputStream inStream, ObjectOutputStream outStream, User u) {
			try {
				String name = (String) inStream.readObject();
				Float value = (Float) inStream.readObject();
				int quantity = (int) inStream.readObject();
				System.out.println("recebi instrução sell " +name + " "+ value + " "+ quantity);

				if(!wc.validateWine(name)) {
					try {
						outStream.writeObject(1);
						return;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				wc.addListing(name, u.getName(), value, quantity);

				outStream.writeObject(0);

				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}
			wc.print();
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
 

	
 }