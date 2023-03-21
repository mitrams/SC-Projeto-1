import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
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
			while (!exit) {
				System.out.print("> ");

				String input = sc.nextLine();
				String[] cmd = input.split(" ");

				switch(cmd[0].charAt(0)) {
					case ('e'):
						exit = true;
						break;
					case ('h'):
						help();
						break;
					case ('a'):
						addWine(inStream, outStream, cmd);
						break;
					case ('s'):
						sellWine(inStream, outStream, cmd);
						break;
					case ('v'):
						System.out.println("goto view");
						break;
					case ('b'):
						buyWine(inStream, outStream, cmd);
						break;
					case ('w'):
						wallet(inStream, outStream, cmd);
						break;
					case ('c'):
						System.out.println("goto classify");
						break;
					case ('t'):
						talk(inStream, outStream, cmd);
						break;
					case ('r'):
						read(inStream, outStream, cmd);
						break;
					default:
						System.out.println("--> \'" + cmd[0] + "\' nao encontrado");
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

	private static void wallet(ObjectInputStream inStream, ObjectOutputStream outStream, String[] cmd) {
		try {
			outStream.writeObject(cmd[0].charAt(0));
			float bal = (float) inStream.readObject();
			System.out.println("Saldo: " + bal);
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void addWine(ObjectInputStream inStream, ObjectOutputStream outStream, String[] cmd)  {
		try{
			if(cmd.length !=3){
				System.out.println("Erro: Insira o input assim sell <wine> <value> <quantity>.");
				return;
			}

			outStream.writeObject(cmd[0].charAt(0));
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

			outStream.writeObject(cmd[0].charAt(0));
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

			outStream.writeObject(cmd[0].charAt(0));
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

	public static void talk(ObjectInputStream inStream, ObjectOutputStream outStream, String[] cmd){
		try{
			outStream.writeObject(cmd[0].charAt(0));
			String toWhom = cmd[1];
			outStream.writeObject(toWhom);
			outStream.writeObject(Arrays.copyOfRange(cmd, 2, cmd.length));
			System.out.println("Mensagem enviada!");

		}  catch (IOException e){
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
	}

	public static void read(ObjectInputStream inStream, ObjectOutputStream outStream, String[] cmd){
		try {
			if(cmd.length != 1) {
				System.out.println("Numero de argumentos errado");
				return;
			}
			outStream.writeObject(cmd[0].charAt(0));
			File msg = (File) inStream.readObject();

			// Abre o ficheiro e escreve as mensagens novas
			File myObj = msg;
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				System.out.println(data);
			}
			myReader.close();
		}  catch (IOException | ClassNotFoundException e){
			System.err.println(e.getMessage());
			System.exit(-1);
		}
			
	}
}
