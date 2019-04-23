package com.tomputtemans.waze_chat_monitor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

public class ChatMonitor {
	private Map<String, Consumer<WebsocketEvent>> handlers = new HashMap<>();
	private WebsocketEndpoint endpoint;

	public ChatMonitor() {
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		Userlist whitelist = new Userlist();
		Userlist onlineUsers = new Userlist();
		
		handlers.put("connect", event -> {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			event.getSocket().emit("user:pan", "{\"center\":{\"lon\":4.354956610058953,\"lat\":50.92738751565759},\"viewArea\":{\"left\":4.28745077357214,\"bottom\":50.902928117088344,\"right\":4.422462446545767,\"top\":50.95183405998914,\"centerLonLat\":null}}");
			event.getSocket().emit("user:change:roomName", "{\"roomName\":\"Benelux\"}");
			event.getSocket().emit("user:change:roomName", "{\"roomName\":\"Benelux\"}");
			event.getSocket().emit("user:change:visible", "{\"visible\":true}");
			System.out.println("Requested room change and requested visible = false");
		});
		handlers.put("disconnect", event -> {
			SlackPoster.postMessage("_Chat monitor disconnected_");
		});
		handlers.put("me:change:room", event -> {
			try {
				JSONArray currentUsers = event.getObject().getJSONObject("room").getJSONArray("users");
				for (int i = 0; i < currentUsers.length(); i++) {
					onlineUsers.add(((JSONObject)currentUsers.get(i)).getString("name"));
				}
				SlackPoster.postMessage("_Chat monitor started_");
				System.out.println("Monitor started");
				System.out.println("Logged in users: " + onlineUsers);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		});
		handlers.put("room:userJoin", event -> {
			try {
				onlineUsers.add(event.getObject().getJSONObject("user").getString("name"));
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		});
		handlers.put("room:userLeave", event -> {
			try {
				onlineUsers.remove(event.getObject().getJSONObject("user").getString("name"));
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		});
		handlers.put("room:newMessage", event -> {
			try {
				String username = event.getObject().getJSONObject("from").getString("name");
				if (!onlineUsers.isListed(username)) {
					System.out.println("Ignoring " + username + " as he/she is not listed as online");
					return;
				}
				int rank = event.getObject().getJSONObject("from").getInt("rank");
				SlackPoster.postMessage("*" + username + " (" + (rank+1) + "):* " + event.getObject().getString("body"));
				if (rank < 2 && !whitelist.isListed(username)) {
				    JSONObject body = new JSONObject();
				    body.put("body", "Hey there! This is an automated message to notify you that within the Benelux this chat is not always actively monitored.");
                    event.getSocket().emit("user:newMessage", body.toString());
					body.put("body", "We've sent your message to our communication platform Slack, so it might be that another editor will join the chat soon.");
					event.getSocket().emit("user:newMessage", body.toString());
					body.put("body", "If not, we'd like to encourage you to join Slack via https://www.wazebelgium.be/join-slack/ We are much more active there.");
					event.getSocket().emit("user:newMessage", body.toString());
					whitelist.add(username);
					SlackPoster.postMessage("_Sent welcoming message with invitation for Slack to " + username + "_");
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		});
		/*handlers.put("room:userPan", event -> {});
		handlers.put("room:userEdit", event -> {});
		handlers.put("viewport:reset", event -> {});
		handlers.put("viewport:userPan", event -> {});*/
	}

	public void startMonitoring() {
		requestSession();
		String webSession = login();
		String url = String.format("https://marx.waze.com:443/chat?sessionId=%s", webSession);
		endpoint = new WebsocketEndpoint(url, "sessionId=" + webSession);
		endpoint.setEventHandler(this::handleEvent);
	}
	
	public void stopMonitoring() {
		endpoint.disconnect();
		SlackPoster.postMessage("_Chat monitor stopped_");
	}
	
	public void poke() {
		handlers.get("connect").accept(new WebsocketEvent(endpoint.getSocket(), "connect", null));
	}
	
	private void handleEvent(WebsocketEvent event) {
		if (handlers.containsKey(event.getName())) {
			System.out.println(event.getName() + " event received");
			handlers.get(event.getName()).accept(event);
		} else {
			System.out.println("Other event: " + event.toString());
		}
	}

	private static void requestSession() {
		try {
			HttpsURLConnection session = (HttpsURLConnection) new URL(
					"https://www.waze.com/row-Descartes/app/Session?language=en").openConnection();
			session.getContent();
		} catch (IOException e) {
			// We are expecting a 403 response at this point
			return;
		}
	}

	private static String login() {
		try {
			HttpsURLConnection login = (HttpsURLConnection) new URL("https://www.waze.com/login/create")
					.openConnection();
			login.setDoOutput(true);
			login.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

			login.setRequestProperty("X-CSRF-Token", getCookieValue("_csrf_token"));
			String loginPayload = String.format("user_id=%s&password=%s",
					URLEncoder.encode(MonitorProperties.getProperty("username"), StandardCharsets.UTF_8.name()),
					URLEncoder.encode(MonitorProperties.getProperty("password"), StandardCharsets.UTF_8.name()));
			try (OutputStream output = login.getOutputStream()) {
				output.write(loginPayload.getBytes(StandardCharsets.UTF_8.name()));
				login.getContent();
			}
			return getCookieValue("_web_session");
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("Failed to create a session", e);
		}
	}

	private static String getCookieValue(String name) throws URISyntaxException {
		HttpCookie crsfToken = ((CookieManager) CookieHandler.getDefault()).getCookieStore()
				.get(new URI("https://www.waze.com")).stream().filter(cookie -> name.equals(cookie.getName()))
				.findFirst().orElseThrow(() -> new RuntimeException("Could not retrieve CSRF token"));
		return crsfToken.getValue();
	}
}
