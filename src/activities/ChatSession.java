package activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ChatSession extends Thread {

	PrintWriter out;
	BufferedReader in;
	private Socket socket;
	private String usuari;
	private boolean logged;
	private ArrayList<ChatSession> chatSessions;
	private InetAddress ip;
	private DatagramSocket ds;
	private static String key = "contraseñaparaencriptarydesencriptar";
	
	public ChatSession(Socket clientSocket, ArrayList<ChatSession> chatSessions, DatagramSocket ds) {

		this.socket = clientSocket;
		this.chatSessions = chatSessions;
		this.ds = ds;
		this.logged = false;
	}

	@SuppressWarnings("deprecation")
	public void run() {
		try {
			this.out = new PrintWriter(this.socket.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

			MulticastSocket ms = new MulticastSocket(5474);
			ip = InetAddress.getByName("230.0.0.5");
			ms.joinGroup(ip);
			
			Thread threadReceiving = new Thread(new Runnable() {

				@Override
				public void run() {
					while(true) {
						try {
							
							byte[] bytes = new byte[256];
							DatagramPacket dp = new DatagramPacket(bytes, bytes.length);

							ms.receive(dp);
							
				            String message = new String(dp.getData(), 0, dp.getLength());
				            String decrypted = decrypt(message);
				            out.println(decrypted);
						
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
	       	
			});
			threadReceiving.start();
			
			// Envia missatge de benvinguda
			out.println("Benvingut al chat");
			out.println("Comandes: ");
			out.println("#login <user>");
			out.println("<messages>");
			out.println("#private <target user> <message>");
			out.println("#logout");
			String inputLine;
			String loginString = "#login";
			
		while(!logged) {
			do {
				inputLine = in.readLine();
				
			} while (!inputLine.startsWith(loginString));
			
			this.usuari = inputLine.substring(loginString.length()).trim();
			this.logged = true;
			String loggedIn = this.usuari + " logged in."; 
			
			sendMessageAll(loggedIn, ms);

			while ((inputLine = in.readLine()) != null && logged) {
				
				if(inputLine.startsWith("#private")){
					//inputLine = "#private Joel";
					int i = "#private".length();
					String [] words = inputLine.split(" ");
					int j = i + words[1].length() + 1;
					String username = inputLine.substring(i, j).trim();
					String message = inputLine.substring(j).trim();
					message = usuari + " whispering: " + message;
					this.sendWhisper(message, username);
				}
				else if(inputLine.startsWith("#logout")) {
					logged = false;
					System.out.println("You left");
					this.sendMessageAll(this.usuari + " left.", ms);
				} else {
					System.out.print(this.usuari + ": " + inputLine);
					this.sendMessageAll(this.usuari + ": " + inputLine, ms);
				}
				
			}
		}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			this.out.close();
			try {
				this.in.close();
				this.socket.close();
			} catch (IOException e) {
			}
		}
	}
	
	public synchronized boolean getLogged() {
		return this.logged;
	}
	
	private void sendWhisper(String message, String username) {
		for(ChatSession cs: chatSessions) {
			if (cs.usuari.equalsIgnoreCase(username.trim())){
				try {
					String encrypted = encrypt(message);
					cs.out.println(message);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		this.out.println(message);
	}

	private void sendMessageAll(String message, MulticastSocket ms) {
		
		try {
			String encrypted = encrypt(message);
			byte[] bytes = encrypted.getBytes();
	        DatagramPacket dp = new DatagramPacket(bytes, bytes.length, ip, 5474);
	        ds.send(dp);
	        
	        
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static String encrypt(String texto) throws Exception {
        SecretKeySpec clave = generarClaveSecreta(key);
        Cipher cifrador = Cipher.getInstance("AES");
        cifrador.init(Cipher.ENCRYPT_MODE, clave);
        byte[] textoEncriptado = cifrador.doFinal(texto.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(textoEncriptado);
    }

    public static String decrypt(String textoEncriptado) throws Exception {
        SecretKeySpec clave = generarClaveSecreta(key);
        Cipher cifrador = Cipher.getInstance("AES");
        cifrador.init(Cipher.DECRYPT_MODE, clave);
        byte[] textoBytes = Base64.getDecoder().decode(textoEncriptado);
        byte[] textoDesencriptado = cifrador.doFinal(textoBytes);
        return new String(textoDesencriptado, "UTF-8");
    }

    private static SecretKeySpec generarClaveSecreta(String clave) throws Exception {
        byte[] claveEncriptacion = clave.getBytes("UTF-8");
        byte[] claveHash = MessageDigest.getInstance("SHA-256").digest(claveEncriptacion);
        return new SecretKeySpec(claveHash, "AES");
    }
}
