
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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

//Servidor myServer

public class TintoImarketServer {
	private UserCatalog uc = UserCatalog.getCatalog();
	private WineCatalog wc = WineCatalog.getCatalog();
	private Wallets wallets = Wallets.getCatalog();
	public static final String MSG_FILE = "Msg.txt";

	final static File serverFolder = new File("Server_Files");
	final static File imagesFolder = new File(serverFolder, "Images");
	final static File userLog = new File(serverFolder, "loginInfo");

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

		InputStream is = null;
		OutputStream os = null;

		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;

		File userLog;

		ServerThread(Socket inSoc, File userLog) throws FileNotFoundException, IOException {
			socket = inSoc;
			this.userLog = userLog;

			System.out.println("thread do server para cada cliente");
		}

		@Override
		public void run() {
			try {
				is = socket.getInputStream();
				os = socket.getOutputStream();

				oos = new ObjectOutputStream(os);
				ois = new ObjectInputStream(is);

				String user = null;
				String passwd = null;

				User u;

				user = (String) ois.readObject();
				passwd = (String) ois.readObject();
				System.out.println("thread: depois de receber a password e o user");

				System.out.println("Recebi: (utilizador: " + user + ", password: " + passwd + ")");

				if (uc.containsUser(user)) {
					if (uc.validateUser(user, passwd)) {
						u = uc.getUser(user);
						oos.writeObject(true);
					} else {
						System.out.println("erro: Password errada");
						oos.writeObject(false);
						return;
					}
				} else {
					uc.registerUser(user, passwd);
					wallets.setBalance(user, 200);
					System.out.println("Utilizador criado");
					u = uc.getUser(user);
					oos.writeObject(true);
				}

				runCommands(u);

				// fin.close();
				// output.close();
				oos.close();
				ois.close();

				socket.close();

			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}

		}

		private void runCommands(User u) {

			Boolean b = false;
			while (!b) {
				try {
					char command = (char) ois.readObject();
					switch (command) {
						case 'e':
							System.out.println(u.getName() + "saiu");
							break;
						case 'a':
							addWine(ois, oos);
							break;
						case 's':
							sellWine(ois, oos, u);
							break;
						case 'v':
							break;
						case 'b':
							buyWine(ois, oos, u);
							break;
						case 'w':
							wallet(oos, u);
							break;
						case 'c':
							classifyWine(ois, oos);
							break;
						case 't':
							talk(ois, oos, u);
							break;
						case 'r':
							read(oos, u);
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
					String imgPath = (String) inStream.readObject();
					System.out.println("recebi instrução add " + name + " " + imgPath);
					
					
					wc.addWine(name, imagesFolder + "/" + imgPath);
					wc.writeFile();
					try {
						outStream.writeObject(0);
					} catch (IOException e) {
						e.printStackTrace();
					}
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

		/* private synchronized void addComm() {
			// Get wine id and image
			String wineId = null;
			File receivedFile = new File(imagesFolder, "image.tmp");
			String fileName = null;

			try {
				receivedFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}

			try {
				System.out.println("Vou ler o wine");
				wineId = (String) ois.readObject();

				System.out.println(wineId);
				fileName = (String) ois.readObject();
				System.out.println(fileName);

				long fileSize = (long) ois.readObject();

				Utilities.receiveFile(is, receivedFile, fileSize);

				File newFile;
				if (receivedFile.renameTo((newFile = new File(imagesFolder, wineId + '_' + fileName)))) {
					receivedFile = newFile;
				}

			} catch (Exception e) {
				try {
					oos.writeBoolean(false);
					oos.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				return;
			}

			if (wineId == null) {
				try {
					oos.writeBoolean(false);
					oos.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return;
			}

			System.out.println(receivedFile.getName());
			if (!wines.put(wineId, -1, -1, null, receivedFile)) {
				try {
					oos.writeBoolean(false);
					oos.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return;
			}

			try {
				oos.writeBoolean(true);
				oos.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		private synchronized void viewComm() {
			String id = null;
			try {
				id = (String) ois.readObject();
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("Failed to receive wineID from view: " + e.getMessage());
				return;
			}

			Wine wine = wines.get(id);

			if (wine == null) {
				// Wine not found
				try {
					oos.writeBoolean(false);
					oos.flush();
					System.out.println("Vinho " + id + " não encontrado");
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				return;
			}

			try {
				try {
					oos.writeBoolean(true);
					oos.flush();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}

				System.out.println("\tA enviar dados de wine");

				oos.writeObject(wine.quantity);
				oos.writeObject(wine.value);
				oos.writeObject(wine.seller);

				File image = new File(wine.imgPath);

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
		} */

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

	}

}