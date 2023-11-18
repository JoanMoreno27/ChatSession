package activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class ChatClient {
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws UnknownHostException, IOException {
		String hostName = "127.0.0.1";
        int portNumber = 5000;
        
        Socket socket = new Socket(hostName, portNumber);
        
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Scanner input = new Scanner(System.in);
        
		// Al connectar, rebrem la benvinguda
 	   	String benvinguda = in.readLine();
 	   	System.out.println(benvinguda);
        
        Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while(true) {
					String missatge = input.nextLine();
					try {
						String decrypt = ChatSession.decrypt(missatge);
					} catch (Exception e) {
						
					}
					out.println(missatge);
				}
			}
        	
        });
        t.start();
        
       while(true) {
    	   String missatge = in.readLine();
    	   System.out.println(missatge);
       }
        

	}
}
