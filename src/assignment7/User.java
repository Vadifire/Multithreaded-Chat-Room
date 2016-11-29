package assignment7;

import java.util.ArrayList;

public class User {
	
	private String name;
	private ArrayList<ChatRoom> chatRooms = new ArrayList<ChatRoom>();
	private ArrayList<User> friends = new ArrayList<User>();

	
	User(String name){
		this.setName(name);
	}
	
	public void addFriend(User u){
		if (!friends.contains(u))
			friends.add(u);			
	}
	
	public void joinRoom(ChatRoom cr){
		if (!chatRooms.contains(cr))
			chatRooms.add(cr);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
