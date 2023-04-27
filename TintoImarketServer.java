
/***************************************************************************
 *
 * Seguranca e Confiabilidade 2022/23
 * Grupo 54
 * Madalena Tomás 53464
 * Francisco Cardoso 57547
 * 
 ***************************************************************************/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

//Servidor myServer

public class TintoImarketServer {
	private UserCatalog uc = UserCatalog.getCatalog();
	private WineCatalog wc = WineCatalog.getCatalog();
	private Wallets wallets = Wallets.getCatalog();

	public static final String MSG_FILE = "Msg.txt";

	final static File serverFolder = new File("Server_Files");
	final static File imagesFolder = new File(serverFolder, "Images");
	final static File userLog = new File(serverFolder, "loginInfo");
	final static File pksFolder = new File(serverFolder, "Pks");
	final static Log logger = Log.getInstance();

	private String chipherpw;
	private String keystore;
	private String keystorepw;

	public static void main(String[] args) {
		if (!serverFolder.exists()) {
			if (!serverFolder.mkdirs()) {
				System.out.println("Failed to create server folder");
				System.exit(-1);
			}
		}

		if (!imagesFolder.exists()) {
			if (!imagesFolder.mkdirs()) {
				System.out.println("Failed to create images folder");
				System.exit(-1);
			}
		}

		if (!pksFolder.exists()) {
			if (!pksFolder.mkdirs()) {
				System.out.println("Failed to create certificates folder");
				System.exit(-1);
			}
		}

		if (!userLog.exists()) {
			try {
				userLog.createNewFile();
			} catch (IOException e1) {
				System.out.println("Failed to create wine database file");
				System.exit(-1);
			}
		}
		System.out.println("servidor: main");
		TintoImarketServer server = new TintoImarketServer();
		server.startServer(args);
	}

	public void startServer(String[] args) {
		SSLServerSocket sSoc = null;

		int port = 12345;

		if (args.length == 4) {
			try {
				port = Integer.parseInt(args[0]);

				if (port < 0 || port > 65535) {
					
				}

			} catch (NumberFormatException e) {
				System.out.println("Formato incorreto da porta, a utilizar a porta '12345'");
			} catch (IllegalArgumentException e) {
				System.out.println("Porta fora dos valores válidos, a utilizar a porta '12345'");
			}
			this.chipherpw = args[1];
			this.keystore = args[2];
			this.keystorepw = args[3];
		}else{
			System.out.println("Formato incorreto: Utilizar TintolmarketServer <port> <password-cifra> <keystore> <password-keystore>");
			System.exit(-1);
		}

		try {
			System.setProperty("javax.net.ssl.keyStore", keystore);
			System.setProperty("javax.net.ssl.keyStorePassword", keystorepw);
			ServerSocketFactory ssf = SSLServerSocketFactory.getDefault( );
			sSoc = (SSLServerSocket) ssf.createServerSocket(port);
			
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

		File userLog;

		ServerThread(Socket inSoc, File userLog) throws FileNotFoundException, IOException {
			socket = inSoc;
			this.userLog = userLog;

			System.out.println("thread do server para cada cliente");
		}

		@Override
		public void run() {
			try {
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				
				Random rd = new Random();
				long nonce = 10000000 + rd.nextInt(90000000);
				
				String user = null;
				user = (String) in.readObject();
			//	String username =  u.getName();
				if (!uc.containsUser(user)) {
					out.writeObject(nonce);
					char flag = 'd';
					out.writeObject(flag);
					long receivednonce = (long)in.readObject();
					if (receivednonce!=nonce) {
						System.out.println("Recebido nonce diferente");
						out.writeObject(false);
					}
					else {
						SignedObject signedObject = (SignedObject)in.readObject();
						Certificate cer = (Certificate)in.readObject();
						PublicKey publicKey = cer.getPublicKey( );
						Signature sig = Signature.getInstance("MD5withRSA");
						if(!signedObject.verify(publicKey, sig)) {
							System.out.println("Recebida assinatura inválida");
							out.writeObject(false);
						} else {
							Long unsignedNonce = (Long) signedObject.getObject();
							if(unsignedNonce==nonce) {
								uc.registerUser(user, pksFolder, publicKey);
								wallets.setBalance(user, 200);
								System.out.println("Utilizador criado");
								out.writeObject(true);
								runCommands(in, out, uc.getUser(user));
							} else {
								System.out.println("Recebido nonce assinado diferente");
								out.writeObject(false);
							}
						}
					}						
				} else{
					out.writeObject(nonce);
					char flag = 'c';
					out.writeObject(flag);
					SignedObject signedObject = (SignedObject)in.readObject();

					PublicKey pk = Utilities.readPk(pksFolder, uc.getUser(user).getFilename());
					
					Signature sig = Signature.getInstance("MD5withRSA");
					if(!signedObject.verify(pk, sig)) {
						System.out.println("Recebida assinatura inválida");
						out.writeObject(false);
					} else {
						Long unsignedNonce = (Long) signedObject.getObject();
						if(unsignedNonce==nonce) {
							System.out.println("Utilizador logged in");
							out.writeObject(true);
							runCommands(in, out, uc.getUser(user));
						} else {
							System.out.println("Recebido nonce assinado diferente");
							out.writeObject(false);
						}
					}				
				}
				out.close();
				in.close();
				socket.close();
			} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (SignatureException e) {
				e.printStackTrace();
			}
		}

		private void runCommands(ObjectInputStream inStream, ObjectOutputStream outStream, User u) {
			while (true) {
				try {
					System.out.println("Waiting for commands...");
					char command = (char) inStream.readObject();

					switch (command) {
						case 'e':
							System.out.println(u.getName() + " saiu");
							return;
						case 'a':
							System.out.println("ADD");
							addWine(inStream, outStream);
							break;
						case 's':
							sellWine(inStream, outStream, u);
							break;
						case 'v':
							viewWine(inStream, outStream, u);
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
					return;
				}
			}
		}

		private void classifyWine(ObjectInputStream inStream, ObjectOutputStream outStream) {
			try {
				String name = (String) inStream.readObject();
				int stars = (int) inStream.readObject();
				System.out.println("recebi instrução classify " + name + " " + stars);

				if (!wc.validateWine(name)) {
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
				
				if (!wc.validateWine(name)) {
					outStream.writeObject(0);

					String imgName = (String) inStream.readObject();
					System.out.println("recebi instrução add " + name + " " + imgName);
					
					File image = null;
					try {
						long fileSize = (long) inStream.readObject();
						image = new File(imagesFolder, name + '_' + imgName);
						Utilities.receiveFile(inStream, image, fileSize);


					} catch (IOException e) {
						e.printStackTrace();

						if (image != null) {
							image.delete();
						}
					}

					wc.addWine(name, imagesFolder + "/" + imgName);
					wc.writeFile();
					
				} else {
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
				System.out.println("recebi instrução buy " + name + " " + seller + " " + quantity);

				if (!wc.validateWine(name)) {
					outStream.writeObject(1);
					return;
				}
				Wine w = wc.getWine(name);
				Listing l = w.getSellerListing(seller);
				if (l == null) {
					outStream.writeObject(2);
					return;
				}
				if (l.getQuantity() < quantity) {
					outStream.writeObject(3);
					return;
				}
				float trxValue = quantity * l.getValue();
				if (wallets.getBalance(u.getName()) < trxValue) {
					outStream.writeObject(4);
					return;
				}
				l.sellQuantity(quantity);
				wallets.changeBalance(u.getName(), -trxValue);
				wallets.changeBalance(uc.getUser(seller).getName(), trxValue);
				wc.writeFile();

				logger.write("BUY:: " + w.getName() + " " + quantity + " " + l.getValue() + " " + u.getName());

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
				System.out.println("recebi instrução sell " + name + " " + value + " " + quantity);

				if (!wc.validateWine(name)) {
					try {
						outStream.writeObject(1);
						return;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				wc.addListing(name, u.getName(), value, quantity);
				wc.writeFile();

				logger.write("SELL:: " + name + " " + quantity + " " + value + " " + u.getName());
				
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
				if (users.containsUser(toWhom)) {
					outStream.writeObject(true);
					// Cria o ficheiro que vai ficar com as mensagens
					File myObj = new File(toWhom + MSG_FILE);
					myObj.createNewFile();

					// Escreve as mensagens no ficheiro
					String[] msg = (String[]) inStream.readObject();
					FileWriter myWriter = new FileWriter(toWhom + MSG_FILE, true);
					myWriter.write(u.getName() + ":");
					for (int i = 0; i < msg.length; i++) {
						myWriter.write(msg[i] + " ");
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
				File file = new File(name + MSG_FILE);
				if (file.exists()) {
					outStream.writeObject("Estas sao todas as mensagens novas");
					Scanner myReader = new Scanner(file);
					Path path = Paths.get(name + MSG_FILE);

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

		private synchronized void viewWine(ObjectInputStream ois, ObjectOutputStream oos, User u) {
			System.out.println("Got to view");

			String id = null;
			try {
				id = (String) ois.readObject();
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("Failed to receive wineID from view: " + e.getMessage());
				return;
			}

			if (!wc.validateWine(id)) {
				// Wine not found
				try {
					oos.writeObject(false);
					oos.flush();
					System.out.println("Vinho " + id + " não encontrado");
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				return;
			}
	
			try {
				
				oos.writeObject(true);				

				Wine wine = wc.getWine(id);
				
				System.out.println("\tA enviar dados de wine");

				List<Listing> lists =  wine.getListings();

				oos.writeObject(lists.size());

				for (Listing list : lists) {
					oos.writeObject(list.getQuantity()); // quantity
					oos.writeObject(list.getValue()); // value
					oos.writeObject(list.getSeller()); // seller
				}
	
				File image = new File(wine.getImgPath());
				
				String imgName = image.getName();
				int separatorIndex = imgName.indexOf("_");
				imgName = imgName.substring(separatorIndex + 1);
	
				oos.writeObject(imgName);
	
				oos.writeObject(image.length());
	
				Utilities.sendFile(oos, image);
				
			} catch (IOException e) {
				System.out.println("Failed to send wine Object from view: " + e.getMessage());
				return;
			}
		}
	}


}