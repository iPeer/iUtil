package com.ipeer.iutil.engine;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class ExitHandler extends Thread implements Runnable {

	private Engine engine;

	public ExitHandler(Engine engine) {
		this.engine = engine;
	}

	@Override
	public void run() {
		try {
			Date d = new Date();
			d.setTime(System.currentTimeMillis());
			engine.writeToLog("\r\n****\r\nAPPLICATION TERMINATED "+DateFormat.getDateInstance().format(d)+"\r\n****");
//			File a = new File("MinecraftStatus.txt");
//			if (a.exists())	
//				a.delete();
			File a = new File("AWeSomeStatus.txt");
			if (a.exists())	
				a.delete();
			if (engine.serverEnabled()) {
				engine.getServer().sendToAllClients("Server stopping!");
				engine.getServer().terminateAll();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
