package assignment7;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;


public class MultiThreadServer extends Application 
{ // Text area for displaying contents 
	private TextArea ta = new TextArea(); 

	private static ArrayList<ChatRoom> chatRooms = new ArrayList<ChatRoom>();
	private static ArrayList<User> users = new ArrayList<User>();

	// Number a client 
	private int clientNo = 0; 

	@Override // Override the start method in the Application class 
	public void start(Stage primaryStage) { 
		// Create a scene and place it in the stage 
		Scene scene = new Scene(new ScrollPane(ta), 450, 200); 
		primaryStage.setTitle("MultiThreadServer"); // Set the stage title 
		primaryStage.setScene(scene); // Place the scene in the stage 
		primaryStage.show(); // Display the stage 

		new Thread( () -> { 
			try {  // Create a server socket 
				ServerSocket serverSocket = new ServerSocket(8000); 
				ta.appendText("MultiThreadServer started at " 
						+ new Date() + '\n'); 


				while (true) { 
					// Listen for a new connection request 
					Socket socket = serverSocket.accept(); 

					// Increment clientNo 
					clientNo++; 

					Platform.runLater( () -> { 
						// Display the client number 
						ta.appendText("Starting thread for client " + clientNo +
								" at " + new Date() + '\n'); 

						// Find the client's host name, and IP address 
						InetAddress inetAddress = socket.getInetAddress();
						ta.appendText("Client " + clientNo + "'s host name is "
								+ inetAddress.getHostName() + "\n");
						ta.appendText("Client " + clientNo + "'s IP Address is " 
								+ inetAddress.getHostAddress() + "\n");	}); 


					// Create and start a new thread for the connection
					new Thread(new HandleAClient(socket)).start();
				} 
			} 
			catch(IOException ex) { 
				System.err.println(ex);
			}
		}).start();
	}


	// Define the thread class for handling
	class HandleAClient implements Runnable, Observer {
		
		private User user;
		private Socket socket; // A connected socket
		DataInputStream inputFromClient;
		DataOutputStream outputToClient;
		
		/** Construct a thread */ 
		public HandleAClient(Socket socket) { 
			
			this.socket = socket;
		}
		/** Run a thread */
		public void run() { 
			try {
				// Create data input and output streams
				inputFromClient = new DataInputStream( socket.getInputStream());
				outputToClient = new DataOutputStream( socket.getOutputStream());
				
				String username = inputFromClient.readUTF();
				user = new User(username);
				ChatRoom globalChat = chatRooms.get(0);
				globalChat.addObserver(this);
				user.joinRoom(globalChat);
				
				// Continuously serve the client
				while (true) { 
					String messageReceived = inputFromClient.readUTF();
					globalChat.postMessage(messageReceived);
				}
			} catch(IOException e) {
				chatRooms.get(0).postMessage(user.getName()+" has left the room.");
				e.printStackTrace();
			}
		}
		
		
		@Override
		public void update(Observable arg0, Object arg1) {
			int chatId = ((ChatRoom) arg0).getId();
			String newMessage = (String) arg1;
			try {
				outputToClient.writeUTF(newMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	
	public static void main(String[] args) {
		chatRooms.add(new ChatRoom()); //Global chat room (id 0)
		launch(args);
	}
}