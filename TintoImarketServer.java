/***************************************************************************
 *
 * Seguranca e Confiabilidade 2022/23
 * Grupo 54
 * Madalena Tomás 53464
 * Francisco Cardoso 57547
 ***************************************************************************/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

 
 //Servidor myServer
 
public class TintoImarketServer{
	private UserCatalog uc = UserCatalog.getCatalog();
	private WineCatalog wc = WineCatalog.getCatalog();
	private Wallets wallets = Wallets.getCatalog();
	public static final File userLog = new File("userLog.txt");
	public static final String MSG_FILE = "Msg.txt";

 
	public static void main(String[] args) {
		 if (!userLog.exists()) {
			 try {
				 userLog.createNewFile();
			 } catch (IOException e1) {
				 e1.printStackTrace();
				 System.exit(-1);
			 }
		 }
		 System.out.println("servidor: main");
		 TintoImarketServer server = new TintoImarketServer();
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
 
		 boolean running = true;
		 while (running) {
			 try {
				 Socket inSoc = sSoc.accept();
				 System.out.println("Cliente conectado");
				 ServerThread newServerThread = new ServerThread(inSoc, userLog);
				 newServerThread.start();
			 } catch (IOException e) {
				 e.printStackTrace();
				 running = false;
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
 
		 @Override
		 public void run() {
			 try {
				out = new ObjectOutputStream(socket.getOutputStream());
				in = new ObjectInputStream(socket.getInputStream());
 
				String user = null;
				String passwd = null;

				User u;
 
				user = (String) in.readObject();
				passwd = (String) in.readObject();
				System.out.println("thread: depois de receber a password e o user");
 				
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
				} else {
					uc.registerUser(user, passwd);
					wallets.setBalance(user, 200);
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
 
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
 
		 }
		 private void runCommands(ObjectInputStream inStream, ObjectOutputStream outStream, User u) {

			Boolean b = false;
			while (!b) {
				try {
					char command = (char) inStream.readObject();
					switch (command) {
						case 'e':
							System.out.println(u.getName() + "saiu");
							break;
						case 'a':
							addWine(inStream, outStream);
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
							wallet(outStream, u);
							break;
						case 'c':
							classifyWine(inStream, outStream);
							break;
						case 't':
							talk(inStream, outStream, u);
							break;
						case 'r':
							read(outStream, u);	
							break;
					}
				} catch (IOException | ClassNotFoundException e) {
					System.err.println(e.getMessage());
					System.exit(-1);
				}
			}
		}
	
		private void classifyWine(ObjectInputStream inStream, ObjectOutputStream outStream) {
			try {
				String name = (String) inStream.readObject();
				int stars = (int) inStream.readObject();
				System.out.println("recebi instrução classify " +name + " "+ stars);

				if(!wc.validateWine(name)) {
					outStream.writeObject(1);
					return;
				}
				Wine w = wc.getWine(name);
				w.classify(stars);
				wc.writeFile();
				outStream.writeObject(0);		
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}
			wc.print();
		}

		private void wallet(ObjectOutputStream outStream, User user) {
			float bal = wallets.getBalance(user.getName());
			try {
				outStream.writeObject(bal);
			} catch (IOException e) {

				e.printStackTrace();
			}
		
		}

		
		private void addWine(ObjectInputStream inStream, ObjectOutputStream outStream) {
			try {
				String name = (String) inStream.readObject();
				String imgPath = (String) inStream.readObject();
				System.out.println("recebi instrução add " +name + " "+ imgPath);

				if(!wc.validateWine(name)) {
					wc.addWine(name,imgPath);
					wc.writeFile();
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
				if(wallets.getBalance(u.getName())<trxValue) {
					outStream.writeObject(4);
					return;
				}
				l.sellQuantity(quantity);
				wallets.changeBalance(u.getName(),-trxValue);
				wallets.changeBalance(uc.getUser(seller).getName(),trxValue);
				wc.writeFile();
				outStream.writeObject(0);		
			} catch (IOException e) {
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
				wc.writeFile();

				outStream.writeObject(0);

				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}
			wc.print();
		}

		private void talk(ObjectInputStream inStream, ObjectOutputStream outStream, User u) {
			try {
				// Recebe o nome do utilizador que vai receber a mensagem
				String toWhom = (String) inStream.readObject();

				UserCatalog users = UserCatalog.getCatalog();
				// Verifica se o utilizador existe
				if(users.containsUser(toWhom)) {
					outStream.writeObject(true);
					// Cria o ficheiro que vai ficar com as mensagens
					File myObj = new File(toWhom+MSG_FILE);
					myObj.createNewFile();

					// Escreve as mensagens no ficheiro
					String[] msg = (String[]) inStream.readObject();
					FileWriter myWriter = new FileWriter(toWhom+MSG_FILE, true);
					myWriter.write(u.getName()+":");
					for(int i=0; i<msg.length; i++) {
						myWriter.write(msg[i]+" ");
					}
					myWriter.write('\n');
					myWriter.close();
				} else {
					outStream.writeObject(false);
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		
		}

		private void read(ObjectOutputStream outStream, User u) {
			String name = u.getName();
			
			try {
				File file = new File(name+MSG_FILE);
				if (file.exists()) {
					outStream.writeObject("Estas sao todas as mensagens novas");
					Scanner myReader = new Scanner(file);
					Path path = Paths.get(name+MSG_FILE);

					long numMsg = Files.lines(path).count();
					outStream.writeObject(numMsg);

					while (myReader.hasNextLine()) {
						String data = myReader.nextLine();
						outStream.writeObject(data);
					}
					myReader.close();

					// Apaga o ficheiro com as mensagens lidas
					Files.delete(path);
				} else {
					outStream.writeObject("Nao ha mensagens novas!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			} 
			
		}
 
	 }
 

	
 }