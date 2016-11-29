package assignment7;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ClientMain extends Application {
	
	private String username = null;
	private static Client client;
	
	public static void main (String [] args) {
		client = new Client();
				
		new Thread(new Runnable () {
			@Override
			public void run() {
				client.runme();
			}
		}).start();
		launch(args);
		
	}
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
				
		TextField login = new TextField("Enter Username:");
		HBox hbox = new HBox();
		Scene chatScene = new Scene(hbox, 800,600);
		
		
		Scene s = new Scene(login , 160, 40);
		primaryStage.setScene(s);
		primaryStage.setX(10);
		primaryStage.setY(10);
		primaryStage.setTitle("Login:");
		primaryStage.show();
		
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
				if (event.getCode() == KeyCode.ENTER){
					username = login.getText();
					primaryStage.setScene(chatScene);
					Platform.runLater(new Runnable(){
						@Override
						public void run() {
							client.login(username);
						}
					});
				}
			}
		});
	}
}
