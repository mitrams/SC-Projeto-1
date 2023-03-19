import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class marketClient {

	public static void main(String[] args) throws IOException {

		try {
			Socket socket = new Socket("127.0.0.1", 12345);

			ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());

			boolean ans = false;
			// while (!ans) {
			// }
			ans = login(inStream, outStream);

			if (!ans) {
				System.out.println("Wrong username or password, please try again");
				System.exit(-1);
			}

			System.out.println("Welcome!");

			Scanner sc = new Scanner(System.in);
			boolean exit = false;
			while (exit == !true) {
				System.out.print("> ");

				String input = sc.nextLine();
				String[] cmd = input.split(" ");

				switch(cmd[0]) {
					case ("e"):
					case ("exit"):
						exit = true;
						break;
					case ("a"):
					case ("add"):
					//outStream.writeObject('e');
					
						System.out.println("goto add");
						break;
					case ("s"):
					case ("sell"):
						System.out.println("goto sell");
						sellWine(inStream,outStream,cmd);
						break;
					case ("v"):
					case ("view"):
						System.out.println("goto view");
						break;
					case ("b"):
					case ("buy"):
						System.out.println("goto buy");
						buyWine(inStream,outStream,cmd);
						break;
					case ("w"):
					case ("wallet"):
						System.out.println("goto wallet");
						wallet(inStream,outStream,cmd);
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
			inStream.close();
			outStream.close();
			sc.close();
			socket.close();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void wallet(ObjectInputStream inStream, ObjectOutputStream outStream, String[] cmd) {
		try {
			outStream.writeObject('w');
			int bal = (int) inStream.readObject();
			System.out.println("Saldo: " + bal);
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void buyWine(ObjectInputStream inStream, ObjectOutputStream outStream, String[] cmd) {
		try{
			if(cmd.length !=4){
				System.out.println("Erro: Insira o input assim buy <wine> <seller> <quantity>.");
				return;
			}

			outStream.writeObject('b');
			String wine = cmd[1];
			String sellerId = cmd[2];
	
			int quantity =  Integer.parseInt(cmd[3]);
			if(quantity<0){
				System.out.println("A quantidade de venda têm ser maior que zero.");
				return;
			}
			outStream.writeObject(wine);
			outStream.writeObject(sellerId);
			outStream.writeObject(quantity);
			int n = (int) inStream.readObject();
			if (n == 0)
				System.out.println("\n Vinho comprado com sucesso.");
			if (n == 1)
				System.out.println("\nErro: Este vinho não existe.");
			if (n == 2)
				System.out.println("\nErro: Esse comprador não têm esse vinho à venda.");
			if (n == 3)
				System.out.println("\nErro: Não vendedor não têm essa quantidade à venda");

		} catch (IOException | ClassNotFoundException e){
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	private static void sellWine(ObjectInputStream inStream, ObjectOutputStream outStream, String[] cmd)  {
		try{
			if(cmd.length !=4){
				System.out.println("Erro: Insira o input assim sell <wine> <value> <quantity>.");
				return;
			}

			outStream.writeObject('s');
			String wine = cmd[1];
			Float value =  Float.parseFloat(cmd[2]);
			if(value<0){
				System.out.println("Preço de venda do vinho não pode ser negativo.");
				return;
			}
			
			int quantity =  Integer.parseInt(cmd[3]);
			if(quantity<0){
				System.out.println("A quantidade de venda têm ser maior que zero.");
				return;
			}
			outStream.writeObject(wine);
			outStream.writeObject(value);
			outStream.writeObject(quantity);
			int n = (int) inStream.readObject();
			if (n == 0)
				System.out.println("\n Vinho adicionado ao catálogo com sucesso.");
			if (n == 1)
				System.out.println("\nErro: Este vinho não existe.");

		} catch (IOException | ClassNotFoundException e){
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
	}

	private static void help() {
		System.out.println("\n----------------------------------------\n");
        System.out.println("Menu:\n");
        System.out.println("add");
        System.out.println("sell");
        System.out.println("view ");
        System.out.println("buy");
        System.out.println("wallet");
        System.out.println("classify");
        System.out.println("talk");
        System.out.println("read");
        System.out.println("exit");
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
		
		sc.close();
		return false;
	}

	public static void talk(String[] cmd){
		if(cmd.length != 3) {
			System.out.println("Numero de argumentos errado");
			return;
		}
		System.out.println("Talk");
	}

	public static void read(String[] cmd){
		if(cmd.length != 1) {
			System.out.println("Numero de argumentos errado");
			return;
		}
		System.out.println("Read");
	}
}