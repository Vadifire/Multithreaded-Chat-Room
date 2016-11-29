package assignment7;

import java.util.ArrayList;
import java.util.Observable;

import assignment7.MultiThreadServer.HandleAClient;

public class ChatRoom extends Observable{

	private ArrayList<User> users = new ArrayList<User>();
	private String newMessage;
	private int id;
	private static int nextId = 0;
	
	public ChatRoom(){
		id = nextId++;
	}
	
	public void postMessage(String message){
		newMessage = message;
		this.setChanged();
		this.notifyObservers(newMessage);
	}
	
	public int getId(){
		return id;
	}
	
	public void addUser(User u, HandleAClient hac){
		if (!users.contains(u)){
			users.add(u);
			this.addObserver(hac);
		}
	}
}
