
/***************************************************************************
 *
 *   Seguranca e Confiabilidade 2020/21
 *
 ***************************************************************************/

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

//Servidor myServer

public class marketServer {

	final static File serverFolder = new File("Server_Files");
	final static File imagesFolder = new File(serverFolder, "Images");
	final static File userLog = new File(serverFolder, "loginInfo");

	static WineDB wines;

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

		try {
			wines = new WineDB(new File(serverFolder, "wineInfo"));
		} catch (IOException e) {
			System.out.println("Failed to initialize wine database");
			System.exit(-1);
		}

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

		System.out.println("Abri a porta " + port);

		while (true) {
			try {
				Socket inSoc = sSoc.accept();
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

			System.out.println("\tNova thread");
		}

		public void run() {
			try {
				out = new ObjectOutputStream(socket.getOutputStream());
				in = new ObjectInputStream(socket.getInputStream());

				String user = null;
				String passwd = null;

				try {
					user = (String) in.readObject();
					passwd = (String) in.readObject();
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

				System.out.println("Received: (user: " + user + ", pass: " + passwd + ")");

				// Ir buscar a palavra-passe
				String dbPass = login_db.get(user);

				boolean newUser = false;

				if (dbPass == null) {
					login_db.put(user, passwd);
					newUser = true;
				} else if (!dbPass.equals(passwd)) {
					System.out.println("Wrong password: " + dbPass);
					out.writeObject(false);
					return;
				}

				// Confirmar o login
				out.writeObject(true);

				String input = receiveString(in);

				if (input == null) {
					return;
				}

				switch (input) {
					case "a":
					case "add":
						addComm();
						break;

					case "view":
						viewComm();
						break;

					default:
						break;
				}

				/* System.out.println("Recebido nome do ficheiro: " + input);

				String filePath = "Server_Files/" + input;

				File f = new File(filePath);

				// System.out.println(filePath);

				try {
					if (f.createNewFile()) {
						System.out.println("Ficheiro criado");
					} else {
						System.out.println("Ficheiro já existe");
					}

				} catch (IOException e) {
					System.out.println("Ficheiro ou pasta inexistente: " + filePath);
					System.exit(-1);
				}

				File recFile = null;
				try {
					recFile = (File) in.readObject();
				} catch (Exception e) {
					return;
				}

				FileReader fin = new FileReader(recFile);
				FileWriter output = new FileWriter(f);

				char[] content = new char[1024];
				int bytesRead = fin.read(content);

				System.out.println(content);

				output.write(content, 0, bytesRead);

				fin.close();
				output.close(); */
				out.close();
				in.close();

				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private String receiveString(ObjectInputStream in) {
			Object received = null;

			try {
				received = in.readObject();
			} catch (Exception e) {
				return null;
			}

			if (!(received instanceof String)) {
				return null;
			}

			return (String) received;
		}

		private synchronized void addComm() {
			// Get wine id and image
			String wineId = null;
			File receivedFile = null;

			try {
				wineId = in.readUTF();
				receivedFile = (File) in.readObject();
			} catch (Exception e) {
				try {
					out.writeBoolean(false);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				return;
			}

			if (wineId == null && receivedFile == null) {
				try {
					out.writeBoolean(false);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return;
			}

			File image = new File(imagesFolder, receivedFile.getName());

			try (
					InputStream fin = new BufferedInputStream(
							new FileInputStream(receivedFile));
					OutputStream fout = new BufferedOutputStream(
							new FileOutputStream(image))) {

				byte[] buffer = new byte[1024];
				int lengthRead;
				while ((lengthRead = fin.read(buffer)) > 0) {
					fout.write(buffer, 0, lengthRead);
					fout.flush();
				}
			} catch (Exception e) {
				System.out.println("Error copying image contents: " + e.getMessage());
				try {
					out.writeBoolean(false);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return;
			}

			if (!wines.put(wineId, -1, -1, null, image)) {
				try {
					out.writeBoolean(false);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return;
			}

			try {
				out.writeBoolean(true);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

		private synchronized void sellComm() {

		}

		private synchronized void viewComm() {
			String id = null;
			try {
				id = in.readUTF();
			} catch (IOException e) {
				System.out.println("Failed to receive wineID from view: " + e.getMessage());
				return;
			}

			if (id == null) {
				// Empty id
				return;
			}

			Wine wine = wines.get(id);

			if (wine == null) {
				// Wine not found
				return;
			}

			try {
				System.out.println(wine);
				out.writeObject(wine);
			} catch (IOException e) {
				System.out.println("Failed to send wine Object from view: " + e.getMessage());
				return;
			}

			


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

	/*
	 * LoginInfo
	 */
	class LoginInfo {

		private FileWriter out;

		Scanner sc = null;

		public LoginInfo(File userInfo) throws FileNotFoundException {
			sc = new Scanner(userInfo);
			// System.out.println(userInfo);

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
					String line = user + ";" + password + '\n';
					out.write(line);
					out.flush();
					// System.out.println(line);
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
			while (sc.hasNextLine()) {
				line = sc.nextLine();
				System.out.println("Login: " + line);
				System.out.flush();
				userPass = line.split(";");

				if (user.equals(userPass[0])) {
					return userPass[1];
				}

			}

			return null;
		}

	}

}
