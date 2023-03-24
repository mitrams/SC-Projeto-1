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

        


    }

