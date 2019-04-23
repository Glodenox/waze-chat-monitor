package com.tomputtemans.waze_chat_monitor;

import java.util.ArrayList;
import java.util.List;

public class Userlist {
	private List<String> usernames = new ArrayList<>();
	
	public boolean isListed(String username) {
		return usernames.contains(username);
	}
	
	public void add(String username) {
		usernames.add(username);
	}
	
	public void remove(String username) {
		usernames.remove(username);
	}
	
	@Override
	public String toString() {
		return String.join(", ", usernames);
	}
}
