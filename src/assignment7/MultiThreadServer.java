package assignment7;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MultiThreadServer extends Application { // Text area for displaying
														// contents
	private TextArea ta = new TextArea();

	// private static ArrayList<ChatRoom> chatRooms = new ArrayList<ChatRoom>();
	// private static ArrayList<User> users = new ArrayList<User>();

	private static HashMap<Integer, ChatRoom> chatRooms = new HashMap<Integer, ChatRoom>();
	private static HashMap<String, User> users = new HashMap<String, User>();
	private static ChatRoom globalChat = new ChatRoom();
	// Number a client
	private int clientNo = 0;

	@Override // Override the start method in the Application class
	public void start(Stage primaryStage) {
		// Create a scene and place it in the stage
		ta.setEditable(false);
		Scene scene = new Scene(new ScrollPane(ta), 450, 200);
		primaryStage.setTitle("MultiThreadServer"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage

		new Thread(() -> {
			try { // Create a server socket
				ServerSocket serverSocket = new ServerSocket(8000);
				ta.appendText("MultiThreadServer started at " + new Date() + '\n');
				

				while (true) {
					// Listen for a new connection request
					Socket socket = serverSocket.accept();
					// Increment clientNo
					clientNo++;
					Platform.runLater(() -> {
						// Display the client number
						ta.appendText("Starting thread for client " + clientNo + " at " + new Date() + '\n');
						// Find the client's host name, and IP address
						InetAddress inetAddress = socket.getInetAddress();
						ta.appendText("Client " + clientNo + "'s host name is " + inetAddress.getHostName() + "\n");
						ta.appendText("Client " + clientNo + "'s IP Address is " + inetAddress.getHostAddress() + "\n");
					});

					// Create and start a new thread for the connection
					new Thread(new HandleAClient(socket)).start();
				}
			} catch (IOException ex) {
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
				inputFromClient = new DataInputStream(socket.getInputStream());
				outputToClient = new DataOutputStream(socket.getOutputStream());

				//Get username and initialize user object
				String username = inputFromClient.readUTF();
				user = new User(username, this);
				users.put(username, user);
				globalChat.addObserver(this);
				user.joinRoom(globalChat);
				outputToClient.writeInt(globalChat.getId());

				globalChat.postMessage(inputFromClient.readUTF());
				// Continuously serve the client
				while (true) {
					String messageReceived = inputFromClient.readUTF();
					String code = messageReceived.substring(0, 4);
					String value = messageReceived.substring(4);
					
					if (code.equals("FRND")) {						
						if(users.containsKey(value)){
							User u = users.get(value);
							if(!(u.isChattingWith(this.user))){
								ChatRoom chat = new ChatRoom();
								chat.addUser(u, u.getHandler());
								chat.addUser(this.user, this);
								chatRooms.put(chat.getId(), chat);
								chat.postMessage("Say hello, " + user.getName() + " and " + u.getName() + "!");
							}
						}
					} else if (code.equals("EXIT")) {						
						code = messageReceived.substring(4,8); //extracts room code
						value = messageReceived.substring(8); //value gets shifted as well
						ChatRoom c = chatRooms.get(Integer.parseInt(code));
						User u = users.get(value);
						c.removeUser(this.user, this);
						c.postMessage(value);
					} else {
						ChatRoom c = chatRooms.get(Integer.parseInt(code));
						c.postMessage(String.format("(%tT) ", Calendar.getInstance()) + user.getName() + ": "
								+ value);
						System.out.println("MESSAGE RECEIVED FROM CLIENT: " + messageReceived);
						
					}
				}
			} catch (IOException e) {
				chatRooms.get(0).postMessage(user.getName() + " has left the room.");
				e.printStackTrace();
			}
		}

		@Override
		public void update(Observable arg0, Object arg1) {
			int chatId = ((ChatRoom) arg0).getId();
			String newMessage = (String) arg1;
			try {
				outputToClient.writeUTF(String.format("%04d", chatId) + newMessage);
				System.out.println("MESSAGES SENT FROM SERVER: "+ newMessage + " AND ALSO " + String.format("%04d", chatId) + newMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		chatRooms.put(globalChat.getId(), globalChat); // Global chat
																// room (id 0)
		launch(args);
	}
}