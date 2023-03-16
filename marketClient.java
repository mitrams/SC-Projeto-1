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

			while (true) {
				System.out.print("> ");

				String fileName = sc.next();

				if (fileName.equals("exit")) {
					break;
				}

				File f = new File("Client_Files/" + fileName);

				if (!f.exists() || !f.canRead() || !f.isFile()) {
					System.out.println("--> \'" + fileName + "\' não encontrado");
					continue;
				}

				outStream.writeObject(fileName);

				try {
					outStream.writeObject(f);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(-1);
				}

				break;

				/*
				 * outStream.writeLong(f.length());
				 * FileInputStream fin = new FileInputStream(f);
				 * InputStream input = new BufferedInputStream(fin);
				 * byte[] buffer = new byte[1024];
				 * 
				 * long bytesRead = 0;
				 * while ((bytesRead = input.read(buffer)) != -1) {
				 * outStream.writeLong(bytesRead);
				 * outStream.write(buffer);
				 * }
				 * 
				 * outStream.writeLong(0);
				 */

				// input.close();

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

}
