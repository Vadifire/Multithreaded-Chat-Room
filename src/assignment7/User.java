/*
 * EE422C Project 7 submission by
 * <Ahsan Khan>
 * <ajk2723>
 * <16445>
 * <Cedric Debelle>
 * <cfd363>
 * <16445>
 * Slip days used: <1>
 * Fall 2016
 */

package assignment7;

import java.util.ArrayList;

import assignment7.MultiThreadServer.HandleAClient;

public class User {
	
	private HandleAClient hac;
	private String name;
	private ArrayList<ChatRoom> chatRooms = new ArrayList<ChatRoom>();
	private ArrayList<User> friends = new ArrayList<User>();

	public boolean isInRoom(ChatRoom c){
		return chatRooms.contains(c);
	}
	
	public boolean isChattingWith(User u){
		for (ChatRoom c : chatRooms){
			if((c.getId() != 0) && c.hasUser(u)){
				return true;
			}
		}
		return false;
	}
	
	User(String name, HandleAClient hac){
		this.setName(name);
		this.hac = hac;
	}
	
	public HandleAClient getHandler(){
		return hac;
	}
	
	public void joinRoom(ChatRoom cr){
		if (!chatRooms.contains(cr))
			chatRooms.add(cr);
	}
	
	public void leaveRoom(ChatRoom cr){
		if (chatRooms.contains(cr))
			chatRooms.remove(cr);
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
