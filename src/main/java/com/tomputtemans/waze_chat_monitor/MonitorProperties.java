package com.tomputtemans.waze_chat_monitor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MonitorProperties {
	private static final Properties PROPERTIES = new Properties();

	static {
		try {
			InputStream inStream = new FileInputStream("./monitor.properties");
			PROPERTIES.load(inStream);
			inStream.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Retrieve a certain value from the properties file
	 * 
	 * @param key
	 *            The key of the value to retrieve
	 * @return The value configured for the given key
	 */
	public static String getProperty(String key) {
		return PROPERTIES.getProperty(key);
	}
}
