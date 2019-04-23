package com.tomputtemans.waze_chat_monitor;

import java.util.function.Consumer;

import javax.net.ssl.SSLContext;

import org.json.JSONObject;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

public class WebsocketEndpoint {
    private SocketIO socket;
    private Consumer<WebsocketEvent> eventHandler;

    public WebsocketEndpoint(String endpointURI, String query) {
    	System.out.println("Creating endpoint for " + endpointURI);
        try {
        	SocketIO.setDefaultSSLSocketFactory(SSLContext.getDefault());
        	SocketIO.setDefaultHeartbeatTimeout(0);
        	socket = new SocketIO();
        	socket.addHeader("Origin", "https://www.waze.com");
        	socket.addHeader("Referer", "https://www.waze.com/editor");
        	socket.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0");
        	socket.setQueryString(query);
        	socket.connect(endpointURI, new IOCallback() {
				@Override
				public void on(String event, IOAcknowledge arg1, Object... objs) {
					if (objs != null && objs.length == 1 && objs[0] instanceof JSONObject) {
						eventHandler.accept(new WebsocketEvent(socket, event, (JSONObject) objs[0]));
					} else {
						eventHandler.accept(new WebsocketEvent(socket, event, null));
					}
				}
				
				@Override
				public void onMessage(JSONObject obj, IOAcknowledge ack) {
					System.out.println("Message received: " + obj);
				}
				
				@Override
				public void onMessage(String message, IOAcknowledge ack) {
					System.out.println("Message received: " + message);
				}
				
				@Override
				public void onConnect() {
					eventHandler.accept(new WebsocketEvent(socket, "connect", null));
				}
				
				@Override
				public void onDisconnect() {
					eventHandler.accept(new WebsocketEvent(socket, "disconnect", null));
				}
				
				@Override
				public void onError(SocketIOException e) {
					System.out.println("Error received: " + e.getMessage());
					e.printStackTrace();
				}
			});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void disconnect() {
    	if (socket.isConnected()) {
    		socket.disconnect();
    	}
    }

    /**
     * register message handler
     * 
     * @param message
     */
    public void setEventHandler(Consumer<WebsocketEvent> eventHandler) {
        this.eventHandler = eventHandler;
    }
    
    public SocketIO getSocket() {
    	return socket;
    }
}
