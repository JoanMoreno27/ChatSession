package activities;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.*;

public class ChatServer {

	public static void main(String[] args) throws IOException {
		
		ArrayList<ChatSession> chatSessions = new ArrayList<ChatSession>();
		
		try {
			ServerSocket server = new ServerSocket(5000);
			DatagramSocket ds = new DatagramSocket(5473);
			while(true) {
				Socket newSocket = server.accept();
				ChatSession newSession = new ChatSession(newSocket, chatSessions, ds);
				chatSessions.add(newSession);
				newSession.start();	
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
