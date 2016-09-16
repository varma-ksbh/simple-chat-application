package edu.tamuk.varma.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

/* A multithreaded chat room server. 
 Server listens for incoming connections from clients and spawns a Handler thread for each incoming requesting
 Each handler thread requests the client for an unique chat name and registers its output stream with global set
 The server broadcasts message to all the clients registered in the global outstream set.*/

public class ChatServer {

	// The port that the server listens on.
	private static final int PORT = 9001;

	// set of all unique client names in chat room.
	private static HashSet<String> names = new HashSet<String>();

	// Global set containing writers of all active chat-clients
	private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

	// A utility method to broadcast messages
	private synchronized static void broadcast(String name,
			String message_type, String message) {
		for (PrintWriter writer : writers) {
			writer.println(message_type + " " + name + ": " + message);
		}
	}

	// Public utility method to display no.of users online at any point of time
	public static void noof_users_online() {
		System.out.println("Current no.of online users:" + names.size());
	}

	//main method, continously listens on port and dpawns handler threads
	public static void main(String[] args) throws Exception {
		System.out.println("The chat server is running.");
		ServerSocket listener = new ServerSocket(PORT);
		try {
			while (true) {
				new Handler(listener.accept()).start();
			}
		} finally {
			listener.close();
		}
	}
	
	// Handler thread class to spawn and serve new users by registering and broadcasting
	private static class Handler extends Thread {
		private String name;
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;

		public Handler(Socket socket) {
			this.socket = socket;
		}

		//Service the thread client by repeatedly requesting a unique chat name and register the client
		public void run() {
			try {

				// Create character streams for the socket.
				in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);

				// Request a unique name and synchronously add the names to the global set
				while (true) {
					out.println("SUBMITNAME");
					name = in.readLine();
					if (name == null) {
						return;
					}
					synchronized (names) {
						if (!names.contains(name)) {
							names.add(name);
							noof_users_online(); //display count of users whenever a new user joins
							break;
						}
					}
				}

				out.println("NAMEACCEPTED"); // respond to client that his name is accepted
				writers.add(out); //add this users printwriter to the global set

				// Accept messages from this client and broadcast them.
				while (true) {
					String input = in.readLine();
					if (input == null) {
						return;
					}
					broadcast(name, "MESSAGE", input); // Message to be
														// broadcasted
				}
			} catch (IOException e) {
				System.out.println(e);
			} finally {
				// This user is going down! Remove its name and its print
				// writer from the sets, and close its socket.
				if (name != null) {
					names.remove(name);
				}
				if (out != null) {
					writers.remove(out);
					// broadcast to all users that this user has exited
					broadcast(name, "MESSAGE", "<<EXITED FROM THE CHAT>>"); 
				}
				try {
					socket.close();
					noof_users_online();
				} catch (IOException e) {
				}
			}
		}
	}
}