/***************************************************************************
 *
 * Seguranca e Confiabilidade 2022/23
 * Grupo 54
 * Madalena Tomás 53464
 ***************************************************************************/

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TintoImarketClient {

	public static void main(String[] args) throws IOException {

		try {
			Socket socket = new Socket("127.0.0.1", 12345);

			ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());

			boolean ans = false;
			// while (!ans) {
			// }
			Scanner sc = new Scanner(System.in);
			ans = login(inStream, outStream, sc);
			

			if (!ans) {
				System.out.println("Wrong username or password, please try again");
				System.exit(-1);
			}

			System.out.println("Welcome!");
			help();

			//sc = new Scanner(System.in);
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
						addWine(inStream,outStream,cmd);
						break;
					case ("s"):
					case ("sell"):
						sellWine(inStream,outStream,cmd);
						break;
					case ("v"):
					case ("view"):
						System.out.println("goto view");
						break;
					case ("b"):
					case ("buy"):
						buyWine(inStream,outStream,cmd);
						break;
					case ("w"):
					case ("wallet"):
						wallet(inStream,outStream,cmd);
						break;
					case ("c"):
					case ("classify"):
						classifyWine(inStream, outStream, cmd);
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void classifyWine(ObjectInputStream inStream, ObjectOutputStream outStream, String[] cmd) {
		try{
			if(cmd.length !=3){
				System.out.println("Erro: Insira o input assim classify <wine> <stars>.");
				return;
			}

			int stars =  Integer.parseInt(cmd[2]);
			if(stars<1 || stars>5){
				System.out.println("A classificação tem de ser de 1 a 5 estrelas.");
				return;
			}

			outStream.writeObject('c');
			String wine = cmd[1];
	
			outStream.writeObject(wine);
			outStream.writeObject(stars);
			int n = (int) inStream.readObject();
			if (n == 0)
				System.out.println("\n Vinho classificado com sucesso.");
			if (n == 1)
				System.out.println("\nErro: Este vinho não existe.");
		} catch (IOException | ClassNotFoundException e){
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	private static void wallet(ObjectInputStream inStream, ObjectOutputStream outStream, String[] cmd) {
		try {
			outStream.writeObject('w');
			float bal = (float) inStream.readObject();
			System.out.println("Saldo: " + bal);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		
	}

	private static void addWine(ObjectInputStream inStream, ObjectOutputStream outStream, String[] cmd)  {
		try{
			if(cmd.length !=3){
				System.out.println("Erro: Insira o input assim add <wine> <image>.");
				return;
			}

			outStream.writeObject('a');
			String wine = cmd[1];
			String imgPath = cmd[2];
		
			outStream.writeObject(wine);
			outStream.writeObject(imgPath);
			int n = (int) inStream.readObject();
			if (n == 0)
				System.out.println("\n Vinho adicionado ao catálogo com sucesso.");
			if (n == 1)
				System.out.println("\nErro: Este vinho já existia anteriormente no catálogo.");

		} catch (IOException | ClassNotFoundException e){
			System.err.println(e.getMessage());
			System.exit(-1);
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
				System.out.println("\nErro: O vendedor não têm essa quantidade à venda");
			if (n == 4)
				System.out.println("\nErro: O seu saldo não é suficiente para fazer a compra");	

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
				System.out.println("\n Instrução de venda adicionada com sucesso.");
			if (n == 1)
				System.out.println("\nErro: Este vinho não existe no catálogo.");

		} catch (IOException | ClassNotFoundException e){
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
	}

	private static void help() {
		//System.out.println("----------------------------------------\n");
        System.out.println("\n(h)elp:\t\tInformacao sobre todos os comandos disponiveis");
        System.out.println("(a)dd:\t\tAdicionar um vinho");
        System.out.println("(s)ell:\t\tColocar um vinho a venda");
        System.out.println("(v)iew:\t\tObter informacoes sobre um vinho");
        System.out.println("(b)uy:\t\tComprar um vinho");
        System.out.println("(w)allet:\tObter informacao sobre o saldo do utilizador");
        System.out.println("(c)lassify:\tAtribuir uma classificacao de 1 a 5 a um vinho");
        System.out.println("(t)alk:\t\tEnviar uma mensagem a outro utilizador");
        System.out.println("(r)ead:\t\tLer as novas mensagens recebidas");
        System.out.println("(e)xit:\t\tSair do programa\n");
	}

	public static boolean login(ObjectInputStream in, ObjectOutputStream out, Scanner sc) {
		
		System.out.print("username: ");
		String user = sc.nextLine();

		System.out.print("password: ");
		String pass = sc.nextLine();


		try {
			out.writeObject(user);
			out.writeObject(pass);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			return (boolean) in.readObject();
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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