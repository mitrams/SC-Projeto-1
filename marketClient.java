import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class marketClient {

	static InputStream is;
	static OutputStream os;

	static ObjectInputStream ois;
	static ObjectOutputStream oos;

	static Scanner sc;

	final static String imageFolder = "Client_Files";

	public static void main(String[] args) throws IOException {

		try {
			Socket socket = new Socket("127.0.0.1", 12345);

			System.out.println("Inicializar I/O");

			os = socket.getOutputStream();
			is = socket.getInputStream();
			
			oos = new ObjectOutputStream(os);
			ois = new ObjectInputStream(is);
			
			System.out.println("I/O Pronto");
			

			boolean ans = false;

			System.out.println("A iniciar Login");
			ans = login(ois, oos);

			if (!ans) {
				System.out.println("Wrong username or password, please try again");
				System.exit(-1);
			}

			System.out.println("Welcome!");

			sc = new Scanner(System.in);
			boolean exit = false;
			while (exit == !true) {
				System.out.print("> ");

				String input = sc.nextLine();
				String[] cmd = input.split(" ");

				switch (cmd[0]) {
					case ("e"):
					case ("exit"):
						oos.writeObject("exit");
						exit = true;
						break;
					case ("a"):
					case ("add"):
						System.out.println("goto add");
						if (!add(cmd)) {
							System.out.println("Failed to add new wine");
						} else {
							System.out.println("Wine added successfully");
						}
						break;
					case ("s"):
					case ("sell"):
						System.out.println("goto sell");
						break;
					case ("v"):
					case ("view"):
						System.out.println("goto view");
						view(cmd);
						break;
					case ("b"):
					case ("buy"):
						System.out.println("goto buy");
						break;
					case ("c"):
					case ("classify"):
						System.out.println("goto classify");
						break;
					case ("t"):
					case ("talk"):
						talk(cmd);
						break;
					case ("r"):
					case ("read"):
						read(cmd);
						break;
					case ("h"):
					case ("help"):
						help();
						break;
					default:
						System.out.println("--> \'" + cmd[0] + "\' - Comando desconhecido");
				}
			}
			ois.close();
			oos.close();
			sc.close();

			os.close();
			is.close();

			socket.close();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void help() {
		System.out.println("Comandos possíveis:\n\tadd\n\tview\n\tsell\n\tbuy\n\twallet\n\tclassify\n\ttalk\n\tread");
	}

	public static boolean login(ObjectInputStream in, ObjectOutputStream out) {
		Scanner sc = new Scanner(System.in);

		System.out.print("username: ");
		String user = sc.next();

		System.out.print("password: ");
		String pass = sc.next();

		try {
			out.writeObject(user);
			out.writeObject(pass);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			return (boolean) in.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	public static void talk(String[] cmd) {
		if (cmd.length != 3) {
			System.out.println("Numero de argumentos errado");
			return;
		}
		System.out.println("Talk");
	}

	public static void read(String[] cmd) {
		if (cmd.length != 1) {
			System.out.println("Numero de argumentos errado");
			return;
		}
		System.out.println("Read");
	}

	public static boolean add(String[] cmd) {
		if (!cmd[0].equals("add") && !cmd[0].equals("a")) {
			System.out.println("Comando desconhecido");
			return false;
		}

		if (cmd.length != 3) {
			System.out.println("Número de argumentos errado");
			return false;
		}

		String wineId = cmd[1];
		File image = new File("Client_Files/" + cmd[2]);

		if (!image.exists()) {
			System.out.println("Ficheiro \"" + image.getName() + "\" não encontrado");
			return false;
		}

		try {
			oos.writeObject("add"); // sinalizar o tipo de comando

			oos.writeObject(wineId); // enviar o id do vinho
			oos.writeObject(image.getName());
			oos.writeObject(image.length());
			Utilities.sendFile(os, image);  // enviar a imagem do vinho

			// outStream.writeObject(image);

			System.out.println("waiting confirmation");
			if (!ois.readBoolean()) {
				// Wine not added
				return false;
			}
			

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean view(String[] cmd) {
		if (!cmd[0].equals("view") && !cmd[0].equals("v")) {
			System.out.println("Comando desconhecido");
			return false;
		}

		if (cmd.length != 2) {
			System.out.println("Número de argumentos errado");
			return false;
		}

		try {
			oos.writeObject("view"); // sinalizar o tipo de comando
			oos.writeObject(cmd[1]); // enviar o id do vinho
			if (!ois.readBoolean()) {
				System.out.println("Vinho não encontrado");
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}		

		try {
			// Wine wine = (Wine) ois.readObject();
			int quantity = (int) ois.readObject();
			int value = (int) ois.readObject();
			
			String seller = (String) ois.readObject();
			
			String imgName = (String) ois.readObject();

			long imgLength = (long) ois.readObject();

			Utilities.receiveFile(is, new File(imageFolder, imgName), imgLength);
			
			String line = "\nNome: " + cmd[1] 
			+ "\nQuantidade: " + (quantity != -1? quantity:"N/A") 
			+ "\nValor: " + (value != -1? value:"N/A") 
			+ "\nVendedor: " + (!seller.equals("")? seller:"N/A")
			+ "\nNome do ficheiro: " + imgName;
			
			System.out.println(line);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;

	}

}
