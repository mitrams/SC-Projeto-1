
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

		File userLog;

		ServerThread(Socket inSoc, File userLog) throws FileNotFoundException, IOException {
			socket = inSoc;
			this.userLog = userLog;

			System.out.println("thread do server para cada cliente");
		}

		public void run() {
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				String user = null;
				String passwd = null;

				try {
					user = (String) inStream.readObject();
					passwd = (String) inStream.readObject();
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

				System.out.println("Received: (user: " + user + ", pass: " + passwd + ")");

				// Ir buscar a palavra-passe
				String dbPass = login_db.get(user);

				boolean newUser = false;

				if (dbPass == null) {
					login_db.put(user, passwd);
					newUser = true;
				} else if (!dbPass.equals(passwd)) {
					System.out.println("Wrong password: " + dbPass);
					outStream.writeObject(false);
					return;
				}

				// Confirmar o login
				outStream.writeObject(true);

				System.out.println("À espera do nome do ficheiro");

				String fileName = receiveString(inStream);

				if (fileName == null) {
					return;
				}

				System.out.println("Recebido nome do ficheiro: " + fileName);

				String filePath = "Server_Files/" + fileName;

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
					recFile = (File) inStream.readObject();
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
				output.close();
				outStream.close();
				inStream.close();

				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
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

		private void receiveTextFile(ObjectInputStream inStream) {

		}

		private void addComm() {
			
		}

		private void sellComm() {
			
		}

		private void viewComm() {
			
		}

		private void buyComm() {
			
		}

		private void walletComm() {
			
		}

		private void classifyComm() {
			
		}

		private void talkComm() {
			
		}

		private void readComm() {
			
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
			System.out.println("Has another line: " + sc.hasNextLine());
			while (sc.hasNextLine()) {
				line = sc.nextLine();
				System.out.println(line + " vs " + user);
				System.out.flush();
				userPass = line.split(":");

				if (user.equals(userPass[0])) {
					return userPass[1];
				}

			}

			return null;
		}

	}

}
