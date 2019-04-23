package com.tomputtemans.waze_chat_monitor;

import java.util.Scanner;

public class App {
	
	public static void main(String[] args) throws Exception {
		ChatMonitor monitor = new ChatMonitor();
		monitor.startMonitoring();
		try (Scanner scanner = new Scanner(System.in)) {
			while(!"exit".equals(scanner.nextLine())) {
				if ("poke".equals(scanner.next())) {
					monitor.poke();
				}
			}
			monitor.stopMonitoring();
			System.exit(0);
		}
	}
}
