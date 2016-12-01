package assignment7;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Client extends Application {
	// IO streams
	DataOutputStream toServer = null;
	DataInputStream fromServer = null;

	HashMap<Integer, ChatWindow> chats;

	private String username;
	TextArea messageLog = new TextArea();

	@Override
	public void start(Stage primaryStage) {
		chats = new HashMap<Integer, ChatWindow>();

		try {
			// Create a socket to connect to the server
			@SuppressWarnings("resource")
			Socket socket = new Socket("localhost", 8000);

			// Create an input stream to receive data from the server
			fromServer = new DataInputStream(socket.getInputStream());

			// Create an output stream to send data to the server
			toServer = new DataOutputStream(socket.getOutputStream());
		} catch (IOException ex) {
			// ta.appendText(ex.toString() + '\n');
		}

		TextField login = new TextField("Enter Username:");
		/*
		 * HBox hbox = new HBox(); Scene chatScene = new Scene(hbox, 800,600);
		 * 
		 * VBox vbox1 = new VBox(); ScrollPane scrollPane = new
		 * ScrollPane(messageLog);
		 * scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		 * vbox1.getChildren().add(scrollPane); TextField messageInput = new
		 * TextField(); vbox1.getChildren().add(messageInput);
		 * 
		 * hbox.getChildren().add(vbox1);
		 */
		Scene loginScene = new Scene(login, 200, 40);
		primaryStage.setScene(loginScene);
		primaryStage.setX(10);
		primaryStage.setY(10);
		primaryStage.setTitle("Login");
		primaryStage.setResizable(false);
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				System.exit(0);
			}
		});
		primaryStage.show();

		/*
		 * messageInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
		 * 
		 * @Override public void handle(KeyEvent event) { if (event.getCode() ==
		 * KeyCode.ENTER){ String message = messageInput.getText(); try {
		 * toServer.writeUTF(username+": "+message); messageInput.setText(""); }
		 * catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } } } });
		 */
		login.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (login.getText().equals("Enter Username:")) {
					login.setStyle("-fx-text-fill: black;");
					login.setText("");
				}
			}
		});

		login.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					username = login.getText();
					if (username.length() > 0) {
						login(username);
						primaryStage.hide();
					}
				}
			}
		});
	}

	private void login(String username) {
		try {
			toServer.writeUTF(username);
			// System.out.println("Wrote " + username);
			ChatWindow globalChat = new ChatWindow("Global", 0, username);

			chats.put(fromServer.readInt(), globalChat);

			toServer.writeUTF(username + " has joined the room.");

			new Reader(fromServer, chats).start();
			globalChat.outbox.addObserver(new Writer(toServer, globalChat));
			// globalChat.inbox.addObserver(new GuiHandler(globalChat));
		} catch (IOException e) {
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	class Writer implements Observer {
		DataOutputStream toServer;
		ChatWindow chat;

		public Writer(DataOutputStream toServer, ChatWindow chat) {
			this.toServer = toServer;
			this.chat = chat;
		}

		@Override
		public void update(Observable o, Object arg) {
			Box<String> out = (Box<String>) o;
			while (!out.isEmpty()) {
				try {
					String message = out.poll();
					toServer.writeUTF(message);
					toServer.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	class Reader extends Thread {

		DataInputStream fromServer;
		HashMap<Integer, ChatWindow> chats;

		public Reader(DataInputStream fromServer, HashMap<Integer, ChatWindow> chats) {
			this.fromServer = fromServer;
			this.chats = chats;
		}

		public void run() {
			boolean done = false;
			while (!done) {
				try {
					String message = fromServer.readUTF();
					System.out.println("message from server: "+message);
					if (message.substring(0,4).equals("LIST")){ //list command
						System.out.println("List message: "+message);
						int chatID = Integer.parseInt(message.substring(4, 8));
						for (ChatWindow c: chats.values()){
							System.out.println(c.chatID);
						}
						Platform.runLater( () -> {
							chats.get(chatID).updateUserList(message.substring(8));
						});
					}
					else{
						int chatID = Integer.parseInt(message.substring(0, 4));
						if (!chats.containsKey(chatID)){
							Platform.runLater( () -> {
								ChatWindow newWindow = new ChatWindow("Private Chat", chatID, username);
								chats.put(chatID, newWindow);
								newWindow.outbox.addObserver(new Writer(toServer, newWindow));
								chats.get(chatID).addText(message.substring(4));
							});
						}
						else{
							chats.get(chatID).addText(message.substring(4));
						}
					}
				} catch (Exception e) {
					done = true;
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * class GuiHandler implements Observer { ChatWindow chat;
	 * 
	 * public GuiHandler(ChatWindow chat) { this.chat = chat; }
	 * 
	 * @Override public void update(Observable o, Object arg) { Box inb = (Box)
	 * o; String message = (String) arg; while(!inb.isEmpty()){
	 * chat.addText(message); } }
	 * 
	 * }
	 */
	class Box<T> extends Observable {
		Queue<T> queue;

		public Box() {
			queue = new LinkedList<T>();
		}

		public boolean isEmpty() {
			return queue.isEmpty();
		}

		public T poll() {
			// System.out.println("MESSAGE " + queue.peek() + " READ FROM
			// INBOX");
			return queue.poll();
		}

		public void add(T value) {
			queue.add(value);
			// System.out.println("MESSAGE " + value + " PUT IN OUTBOX");
			this.setChanged();
			this.notifyObservers();
		}
	}

	class ChatWindow {
		private Stage window;
		private Scene scene;
		private HBox content;
		private TextArea ta;
		private TextArea userArea;
		private TextField friendChat;
		Box<String> inbox;
		Box<String> outbox;
		int chatID;
		Label l;
		private String username;

		public void addText(String message) {
			ta.appendText(message + "\n");
		}
		
		public void updateUserList(String users){
			userArea.clear();
			userArea.appendText(users);
		}

		public ChatWindow(String name, int ID, String username) {
			friendChat = new TextField("Enter Username...");
			inbox = new Box<String>();
			outbox = new Box<String>();
			this.chatID = ID;
			this.username = username;

			ta = new TextArea();
			userArea = new TextArea();
			content = new HBox();
			VBox messageArea = new VBox();
			VBox friendArea = new VBox();

			ta.setEditable(false);
			userArea.setEditable(false);
			ScrollPane scrollPane = new ScrollPane(ta);
			scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
			
			ScrollPane userList = new ScrollPane(userArea);
			userList.setHbarPolicy(ScrollBarPolicy.NEVER);

			messageArea.getChildren().add(scrollPane);
			TextField messageInput = new TextField();
			messageArea.getChildren().add(new Label("Enter Message:"));
			messageArea.getChildren().add(messageInput);
			friendArea.getChildren().add(new Label("Start New Chat With:"));
			friendArea.getChildren().add(friendChat);
			friendArea.getChildren().add(new Label("Users In Room:"));
			friendArea.getChildren().add(userList);
			
			if (ID != 0){//not global chat, allow invitation
				TextField groupChat = new TextField("Enter Username...");
				friendArea.getChildren().add(new Label("Invite To This Chat:"));
				friendArea.getChildren().add(groupChat);
				
				groupChat.setOnKeyPressed(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent event) {
						if (event.getCode() == KeyCode.ENTER) {
							if (!groupChat.getText().equals("")) {
								outbox.add("INVT"+String.format("%04d", chatID) +groupChat.getText());
							}
							groupChat.setText("");
						}
					}
				});
				groupChat.setOnMouseClicked(new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						groupChat.setText("");
					}
				});			
			}
			content.getChildren().add(messageArea);
			content.getChildren().add(friendArea);
			content.setPadding(new Insets(10,10,10,10));
			friendArea.setPadding(new Insets(10,10,10,10));
			messageArea.setPadding(new Insets(10,10,10,10));


			scene = new Scene(content, 650, 250);
			window = new Stage();
			window.setResizable(false);
			window.setScene(scene);
			window.setTitle(name+" - "+username);
			window.setX(10);
			window.setY(10);
			window.show();

			window.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent arg0) {
					outbox.add("EXIT"+String.format("%04d", chatID) + username+ " has left the room.");
					chats.remove(ID);
					window.hide();
				}

			});

			friendChat.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					if (event.getCode() == KeyCode.ENTER) {
						if (!friendChat.getText().equals("")) {
							outbox.add("FRND" + friendChat.getText());
						}
						friendChat.setText("");
					}
				}
			});
			friendChat.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					friendChat.setText("");
				}
			});
			
			messageInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					if (event.getCode() == KeyCode.ENTER) {
						if (!messageInput.getText().equals("")) {
							System.out.println("adding message to outbox");
							outbox.add(String.format("%04d", chatID) + messageInput.getText());
						}
						messageInput.setText("");
					}
				}
			});
		}
	}
}