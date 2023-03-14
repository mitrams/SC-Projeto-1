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
			Socket socket = new Socket("127.0.0.1", 23456);

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

				switch(cmd[0].charAt(0)) {
					case ('e'):
					exit = true;
						break;
					case ('a'):
						System.out.println("goto add");
						break;
					case ('s'):
						System.out.println("goto sell");
						break;
					case ('v'):
						System.out.println("goto view");
						break;
					case ('b'):
						System.out.println("goto buy");
						break;
					case ('c'):
						System.out.println("goto classify");
						break;
					case ('t'):
						talk(cmd);
						break;
					case ('r'):
						read(cmd);
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
