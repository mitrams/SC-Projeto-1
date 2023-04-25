/***************************************************************************
 *
 * Seguranca e Confiabilidade 2022/23
 * Grupo 54
 * Madalena Tomás 53464
 * Francisco Cardoso 57547
 ***************************************************************************/

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Scanner;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TintoImarket {

	final static File userFolder = new File("Client_Files");
	
	static InputStream is = null;
	static OutputStream os = null;

	static ObjectInputStream inStream = null;
	static ObjectOutputStream outStream = null;

	private static String truststore;
	private static String keystore;
	private static String keystorepw;

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, ClassNotFoundException {
		SSLSocket socket = null;
		Scanner sc = null;

		if (!userFolder.exists()) {
			userFolder.mkdirs();
		}

//		System.out.println(args.length);

		String user = null;

		if(args.length != 5){
			System.out.println("Formato incorreto: Utilizar TintolMarket <serverAdress> <truststore> <keystore> <password-keystore> <userID>");
			System.exit(-1);
		}
		
		String[] hostPortPair = args[0].split(":");

		int port = -1;
		
		if (hostPortPair.length >= 2) {
			if (!hostPortPair[1].equals("")) {
				try {
					port = Integer.parseInt(hostPortPair[1]);
				} catch (NumberFormatException e) {
					System.out.println("Número de porta com o formato errado, a utilizar a porta 12345");
					port = 12345;
				}
			}
		} else {
			port = 12345;
		}
		truststore = args[1];
		keystore = args[2];
		keystorepw = args[3];
		user = args[4];
		PrivateKey pk=null;
		Certificate cer=null;

//		System.out.println("Before try:" + args[1]+" "+args[2]+" "+args[3]+" "+args[4]);

		try {
			FileInputStream ksFile = new FileInputStream(keystore);
			KeyStore kStore = KeyStore.getInstance("PKCS12");
//			System.out.println("Before load");
			kStore.load(ksFile, keystorepw.toCharArray());
//			System.out.println("After load " + keystorepw);
			pk = (PrivateKey) kStore.getKey(user, keystorepw.toCharArray());
			if(pk==null) {
				System.out.println("utilizador "+user+" não encontrado na keystore");
				System.exit(-1);
			}
			cer = (Certificate) kStore.getCertificate(user);
			System.setProperty("javax.net.ssl.trustStore", truststore);
			System.setProperty("javax.net.ssl.trustStorePassword",keystorepw);
			System.out.println("truststore:" + truststore);
			SocketFactory sf = SSLSocketFactory.getDefault();
		
			System.out.println("address: "+hostPortPair[0]+":"+port);
			socket = (SSLSocket) sf.createSocket(hostPortPair[0], port);
			System.out.println("2");
		} catch (UnknownHostException e) {
			System.out.println("Host '" + hostPortPair[0] + "' desconhecido");
			System.exit(-1);
		} catch (IllegalArgumentException e) {
			System.out.println("Porta '" + port + "' está fora dos limites");
			System.exit(-1);
		} catch (Exception e) {
			System.out.println("Falha na conexão");
			e.printStackTrace();
			System.exit(-1);
		}

		try {

			is = socket.getInputStream();
			os = socket.getOutputStream();

			outStream = new ObjectOutputStream(os);
			inStream = new ObjectInputStream(is);
			sc = new Scanner(System.in);

			boolean ans = false;


//			System.out.println("Before login");

			ans = login(user, pk, cer);
			
			if (!ans) {
				System.out.println("Utilizador ou palavra-passe incorretos");
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

				switch(cmd[0].charAt(0)) {
					case ('e'):
						exit = true;
						outStream.writeObject('e');
						break;
					case ('a'):
						addWine(inStream,outStream,cmd);
						break;
					case ('s'):
						sellWine(inStream,outStream,cmd);
						break;
					case ('v'):
						viewWine(inStream, outStream, cmd);
						break;
					case ('b'):
						buyWine(inStream,outStream,cmd);
						break;
					case ('w'):
						wallet(inStream,outStream);
						break;
					case ('c'):
						classifyWine(inStream, outStream, cmd);
						break;
					case ('t'):
						talk(inStream, outStream, cmd);
						break;
					case ('r'):
						read(inStream, outStream, cmd);
						break;
					case ('h'):
						help();
						break;
					default:
						System.out.println("--> \'" + cmd[0] + "\' - Comando desconhecido");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			inStream.close();
			outStream.close();
			sc.close();
			socket.close();		
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

	private static void wallet(ObjectInputStream inStream, ObjectOutputStream outStream) {
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

			
			String wine = cmd[1];
			String imgName = cmd[2];
			
			File image = new File(userFolder, imgName);
			
			if (!image.exists()) {
				System.out.println("Ficheiro '" + imgName + "' não foi encontrado");
				return;
			}

			char comm = 'a';
			outStream.writeObject(comm);
			
			outStream.writeObject(wine);
			
			int n = (int) inStream.readObject();
			if (n == 0) {
				outStream.writeObject(imgName);
				System.out.println("A enviar ficheiro");
				outStream.writeObject(image.length());
				System.out.println("receber confirmação");
				Utilities.sendFile(outStream, image);
				System.out.println("\t Vinho adicionado ao catálogo com sucesso.");
			}
			if (n == 1)
				System.out.println("\tErro: Este vinho já existia anteriormente no catálogo.");

		} catch (IOException | ClassNotFoundException e){
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
	}

	public static boolean viewWine(ObjectInputStream ois, ObjectOutputStream oos, String[] cmd) {
		if (!cmd[0].equals("view") && !cmd[0].equals("v")) {
			System.out.println("Comando desconhecido");
			return false;
		}

		if (cmd.length != 2) {
			System.out.println("Número de argumentos errado");
			return false;
		}

		try {
			oos.writeObject('v'); // sinalizar o tipo de comando
			oos.writeObject(cmd[1]); // enviar o nome do vinho

			if (!(boolean) ois.readObject()) {
				System.out.println("Vinho não encontrado");
				return false;
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		try {
			// Wine wine = (Wine) ois.readObject();

			int numberOfListings = (int) ois.readObject();

			System.out.println("Listings num == " + numberOfListings);

			for (int i = 0; i < numberOfListings; i++) {
				int quantity = (int) ois.readObject();
				float value = (float) ois.readObject();
				
				String seller = (String) ois.readObject();
				
				String line = "\nNome: " + cmd[1] 
				+ "\nQuantidade: " + (quantity != -1? quantity:"N/A") 
				+ "\nValor: " + (value != -1? value + "€":"N/A") 
				+ "\nVendedor: " + (!seller.equals("")? seller:"N/A");
				
				System.out.println(line);
			}

			System.out.println("");

			String imgName = (String) ois.readObject();

			long imgLength = (long) ois.readObject();

			Utilities.receiveFile(is, new File(userFolder, imgName), imgLength);

			System.out.println("Nome da imagem: " + imgName);

			return true;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return false;
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

	private static void talk(ObjectInputStream inStream, ObjectOutputStream outStream, String[] cmd){
		try{
			// Manda o comando para o server
			outStream.writeObject(cmd[0].charAt(0));
			
			// Manda par ao server o nome a quem e mandada a mensagem
			String toWhom = cmd[1];
			outStream.writeObject(toWhom);
			// Apenas manda a mensagem caso o tulizador exista
			if((boolean) inStream.readObject()) {
				outStream.writeObject(Arrays.copyOfRange(cmd, 2, cmd.length));
				System.out.println("Mensagem enviada!");
			} else {
				System.out.println("O utlizador "+toWhom+" nao existe");
			}
		}  catch (IOException | ClassNotFoundException e){
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
	}

	private static void read(ObjectInputStream inStream, ObjectOutputStream outStream, String[] cmd){
		try {
			// O comando read nao recebe argumentos
			if(cmd.length != 1) {
				System.out.println("Numero de argumentos errado");
				return;
			}
			// Manda o comando para o server
			outStream.writeObject(cmd[0].charAt(0));
			
			// Recebe do server se existem mensagens para ler ou nao
			String msgState = (String) inStream.readObject();
			if(msgState.equals("Estas sao todas as mensagens novas")) {
				// Recebe do server as mensagens para ler
				long numMsg = (long) inStream.readObject();
				for(int i=0; i<numMsg; i++) {
					System.out.println((String) inStream.readObject());
				}
			}

			// Relata o estado das mensagens 
			System.out.println(msgState);
		}  catch (IOException | ClassNotFoundException e){
			System.err.println(e.getMessage());
			System.exit(-1);
		}
			
	}

	private static void help() {
		System.out.println("----------------------------------------\n");
        System.out.println("(h)elp:\t\tInformacao sobre todos os comandos disponiveis");
        System.out.println("(a)dd:\t\tAdicionar um vinho");
        System.out.println("(s)ell:\t\tColocar um vinho a venda");
        System.out.println("(v)iew:\t\tObter informacoes sobre um vinho");
        System.out.println("(b)uy:\t\tComprar um vinho");
        System.out.println("(w)allet:\tObter informacao sobre o saldo do utilizador");
        System.out.println("(c)lassify:\tAtribuir uma classificacao de 1 a 5 a um vinho");
        System.out.println("(t)alk:\t\tEnviar uma mensagem a outro utilizador");
        System.out.println("(r)ead:\t\tLer as novas mensagens recebidas");
        System.out.println("(e)xit:\t\tSair do programa");
		System.out.println("\n----------------------------------------\n");
	}

	public static boolean login(String user, PrivateKey pk, Certificate cer) throws NoSuchAlgorithmException, IOException, ClassNotFoundException {
		long nonce=0;
		char flag=' ';

		try {
			outStream.writeObject(user);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			nonce=(long)inStream.readObject();
			flag=(char)inStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		SignedObject signedNonce=null;
		try {
			Signature signature = Signature.getInstance("MD5withRSA");
			signedNonce = new SignedObject(nonce, pk, signature);
		} catch (InvalidKeyException | SignatureException e) {
			System.out.println("\nErro a gerar assinatura para nonce\n");
			return false;
		}

		if(flag=='c') {
			try {
				outStream.writeObject(signedNonce);
				return (boolean)inStream.readObject();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		if(flag=='d') {
			try {
				outStream.writeObject(nonce);
				outStream.writeObject(signedNonce);
				outStream.writeObject(cer);
				return (boolean)inStream.readObject();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		System.out.println("\nRecebida flag desconhecida\n");
		return false;	
	}

}