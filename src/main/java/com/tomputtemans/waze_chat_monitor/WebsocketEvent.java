package com.tomputtemans.waze_chat_monitor;

import org.json.JSONObject;

import io.socket.SocketIO;

public class WebsocketEvent {
	private SocketIO socket;
	private String name;
	private JSONObject object;

	public WebsocketEvent(SocketIO socket, String name, JSONObject object) {
		this.socket = socket;
		this.name = name;
		this.object = object;
	}
	
	public SocketIO getSocket() {
		return socket;
	}
	
	public String getName() {
		return name;
	}
	
	public JSONObject getObject() {
		return object;
	}
	
	public String toString() {
		return name + ": " + object;
	}
}
