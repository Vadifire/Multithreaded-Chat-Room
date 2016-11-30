package assignment7;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
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
			System.out.println("Wrote " + username);
			ChatWindow globalChat = new ChatWindow("Global");

			chats.put(fromServer.readInt(), globalChat);

			toServer.writeUTF(username + " has joined the room.");

			new ReadThread(fromServer, chats).start();
			new WriteThread(toServer, chats).start();
			new GuiHandler(globalChat).start();
		} catch (IOException e) {
		}

	}

	public static void main(String[] args) {
		launch(args);
	}

	class WriteThread extends Thread {
		DataOutputStream toServer;
		HashMap<Integer, ChatWindow> chats;

		public WriteThread(DataOutputStream toServer, HashMap<Integer, ChatWindow> chats) {
			this.toServer = toServer;
			this.chats = chats;
		}

		public void run() {
			boolean done = false;
			while (!done) {
				for (Entry<Integer, ChatWindow> m : chats.entrySet()) {
					while (!m.getValue().outbox.isEmpty()) {
						try {
							String toWrite = String.format("%04d", m.getKey()) + m.getValue().outbox.poll();
							toServer.writeUTF(toWrite);
						} catch (Exception e) {
							done = true;
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	class ReadThread extends Thread {

		DataInputStream fromServer;
		HashMap<Integer, ChatWindow> chats;

		public ReadThread(DataInputStream fromServer, HashMap<Integer, ChatWindow> chats) {
			this.fromServer = fromServer;
			this.chats = chats;
		}

		public void run() {
			String message;
			boolean done = false;
			while (!done) {
				try {
					message = fromServer.readUTF();
					int chatID = Integer.parseInt(message.substring(0, 3));
					chats.get(chatID).inbox.add(message.substring(4));
				} catch (Exception e) {
					done = true;
					e.printStackTrace();
				}
			}
		}
	}

	class GuiHandler extends Thread {
		ChatWindow chat;

		public GuiHandler(ChatWindow chat) {
			this.chat = chat;
		}

		public void run() {
			while (true) {
				while (!chat.inbox.isEmpty()) {
					chat.addText(chat.inbox.poll());
				}
			}
		}
	}
}

class ChatWindow {
	private Stage window;
	private Scene scene;
	private HBox content;
	private TextArea ta;
	Queue<String> inbox;
	Queue<String> outbox;
	Label l;

	public void addText(String message) {
		ta.appendText(message + "\n");
	}

	public ChatWindow(String name) {
		l = new Label("LABEL");
		inbox = new LinkedList<String>();
		outbox = new LinkedList<String>();
		ta = new TextArea();
		content = new HBox();
		VBox messageArea = new VBox();

		ta.setEditable(false);
		ScrollPane scrollPane = new ScrollPane(ta);
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		messageArea.getChildren().add(scrollPane);
		TextField messageInput = new TextField();
		messageArea.getChildren().add(messageInput);
		content.getChildren().add(messageArea);
		content.getChildren().add(l);

		scene = new Scene(content, 800, 400);
		window = new Stage();
		window.setScene(scene);
		window.setTitle(name);
		window.setX(10);
		window.setY(10);
		window.show();

		window.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent arg0) {
				window.hide();
			}

		});

		messageInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					if (!messageInput.getText().equals("")) {
						outbox.add(messageInput.getText());
					}
					messageInput.setText("");
				}
			}
		});
	}
}