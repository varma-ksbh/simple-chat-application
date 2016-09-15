package edu.tamuk.varma.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private String chatName;
    BufferedReader in;
    PrintWriter out;

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Return in the
     * listener sends the textfield contents to the server.  Note
     * however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED
     * message from the server.
     */
    public Client(){
    	System.out.println("Please input your preferred chat name..");
    	this.chatName = new Scanner(System.in).nextLine();
    }
     
    /**
     * Prompt for and return the address of the server.
     */
    private String getServerAddress() {
    	System.out.println("Please input server address.! default is 127.0.0.1");
        return new Scanner(System.in).nextLine();
    }

    /**
     * Prompt for and return the desired screen name.
     */
    public String getName() {
    	return this.chatName;
    }
    
    private void setName(String chatname){
    	this.chatName = chatname;
    }
    /**
     * Connects to the server then enters the processing loop.
     */
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Process all messages from server, according to the protocol.
        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                out.println(getName());
            } else if (line.startsWith("NAMEACCEPTED")) {
            	System.out.println("Your chat name:" + getName() + " is Accepted! Happy chatting!");
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
            	if(! line.substring(8).startsWith(getName()))
            		//System.out.println("You:" + line.substring(8 + getName().length()));
            		System.out.println(line.substring(8));
            } 
        }
    }

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.run();
    }
}
