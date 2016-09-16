package edu.tamuk.varma.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
	private String chatName;
    BufferedReader in;
    PrintWriter out;

    // Constructs chat client by prompting for server-address and unique chat name 
    public ChatClient(){
    	System.out.println("Please input your preferred chat name..");
    	this.chatName = new Scanner(System.in).nextLine();
    }
     
    // Getter for server address
    private String getServerAddress() {
    	System.out.println("Please input server address.! default is 127.0.0.1");
    	String address = new Scanner(System.in).nextLine();
        return address = (address == "" ) ? "127.0.0.1" : address;
    }

    // Getter for chat name
    public String getName() {
    	return this.chatName;
    }
    
    // Connects to the server and loops for sending & receiving messages
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                out.println(getName());
            } else if (line.startsWith("NAMEACCEPTED")) {
            	System.out.println("Your chat name:" + getName() + " is Accepted!");
            	System.out.println("==============" + getName() + " chat window==============");
            	
            	// This thread acts as an event-listener waiting for the user to input chat on the terminal and then sends the message to the server
            	new Thread(new Runnable() {
            	    public void run() {
            	    	BufferedReader chatinput = new BufferedReader(new InputStreamReader(System.in));
            	    	String chat = "";
            	    	while(true){
            	    		try {
								while((chat = chatinput.readLine()) != null){
									out.println(chat);
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
            	    	}
            	    }
            	}).start();
            	
            } else if (line.startsWith("MESSAGE")) {
            	// Display the message only if its of another client
            	if(! line.substring(8).startsWith(getName()))
            		System.out.println(line.substring(8));
            } 
        }
    }

    // Creates new chatclients and runs them
    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.run();
    }
}
