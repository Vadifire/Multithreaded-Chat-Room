package assignment7;

import java.io.*; 
import java.net.*; 
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


public class Client extends Application{ 
	// IO streams 
	DataOutputStream toServer = null; 
	DataInputStream fromServer = null;
	
	private String username;
	TextArea messageLog = new TextArea();	


	@Override // Override the start method in the Application class 
	public void start(Stage primaryStage) { 

		
		TextField login = new TextField("Enter Username:");
		HBox hbox = new HBox();
		Scene chatScene = new Scene(hbox, 800,600);
		
		VBox vbox1 = new VBox();
		ScrollPane scrollPane = new ScrollPane(messageLog);
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		vbox1.getChildren().add(scrollPane);
		TextField messageInput = new TextField();
		vbox1.getChildren().add(messageInput);
		
		hbox.getChildren().add(vbox1);
		
		Scene s = new Scene(login , 160, 40);
		primaryStage.setScene(s);
		primaryStage.setX(10);
		primaryStage.setY(10);
		primaryStage.setTitle("Login:");
		primaryStage.show();
		
		
		messageInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER){
					String message = messageInput.getText();
					try {
						toServer.writeUTF(username+": "+message);
						messageInput.setText("");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		login.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (login.getText().equals("Enter Username:")) {
					login.setStyle("-fx-text-fill: black;");
					login.setText("");
				}
				Platform.runLater(new Runnable(){
					@Override
					public void run() {
						while (true){
							try {
								messageLog.appendText(fromServer.readUTF());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				});
			}
		});
		
		login.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER){
					username = login.getText();
					try {
						toServer.writeUTF(username);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					primaryStage.setScene(chatScene);
				}
			}
		});
		
		try { 
			// Create a socket to connect to the server 
			@SuppressWarnings("resource")
			Socket socket = new Socket("localhost", 8000); 
			// Socket socket = new Socket("130.254.204.36", 8000); 
			// Socket socket = new Socket("drake.Armstrong.edu", 8000); 

			// Create an input stream to receive data from the server 
			fromServer = new DataInputStream(socket.getInputStream()); 

			// Create an output stream to send data to the server 
			toServer = new DataOutputStream(socket.getOutputStream()); 
		} 
		catch (IOException ex) { 
			//ta.appendText(ex.toString() + '\n');
		}	

	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
