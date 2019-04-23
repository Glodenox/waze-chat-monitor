package com.tomputtemans.waze_chat_monitor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

public class SlackPoster {
	private static String previousMessage = "";
	
	public static synchronized void postMessage(String message) {
		System.out.println("Would output message to Slack: " + message);
		return;
		// Prevent duplicate message forwarding
		/*if (message.equals(previousMessage)) {
			return;
		}
		previousMessage = message;
		try {
			JSONObject slackMessage = new JSONObject();
			slackMessage.put("text", message);
			
			String webhook = MonitorProperties.getProperty("slack_incoming_webhook");
			HttpsURLConnection connection = (HttpsURLConnection) new URL(webhook).openConnection();
			connection.setDoOutput(true);
			try (OutputStream output = connection.getOutputStream()) {
				output.write(slackMessage.toString().getBytes(StandardCharsets.UTF_8.name()));
				connection.getContent();
			}
		} catch (IOException | JSONException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}*/
	}
}
