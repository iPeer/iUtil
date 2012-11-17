package com.ipeer.iutil.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Properties;

public class AWeSomeChat implements Runnable {

	private File file;
	private static File logout;
	public boolean IS_RUNNING = false;
	public List<String> online = new ArrayList<String>();
	protected Engine engine;
	public static Properties cache;

	public AWeSomeChat(Engine engine, String file) {
		this.engine = engine;
		this.file = new File(file);
		if (engine == null)
			logout = new File("F:\\Dropbox\\Java\\iUtil\\");
		else
			logout = new File("/home/minecraft/logs/");
		cache = new Properties();
		if (Engine.AWeSomeChatCache.exists())
			try {
				cache.load(new FileInputStream(Engine.AWeSomeChatCache));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public void start() {
		if (this.IS_RUNNING)
			return;
		this.IS_RUNNING = true;
		String s = ((file.getAbsolutePath()).toLowerCase().contains("creative")) ? "Creative" : "Survival";
		(new Thread(this, "AWeSome Chat ("+s+")")).start();
	}
	
	public void stop() {
		this.IS_RUNNING = false;
	}

	public static void main(String[] args) throws IOException {
		AWeSomeChat a = new AWeSomeChat(null, "/home/minecraft/servers/survival/server.log"/*"F:\\Dropbox\\Public\\Testtextfile.txt"*/);
		a.parseLine("2012-09-06 20:11:30 [INFO] <iPeer> I'm still testing my tailing", "Survival");
		//a.start();
	}

	@Override
	public void run() {
		try {
			String c = ((file.getAbsolutePath()).toLowerCase().contains("creative")) ? "Creative" : "Survival";
			//Reader a = new FileReader(file);
			//BufferedReader b = new BufferedReader(a);
			RandomAccessFile b = new RandomAccessFile(file, "r");
			String line = null;
			while (IS_RUNNING) {
				long marker = Long.valueOf(cache.getProperty(c, Long.toString(b.length())));
				//				cache.put(c, Long.toString(marker));
				//				cache.store(new FileOutputStream(Engine.AWeSomeChatCache), "");
				b.seek(marker);
				if ((line = b.readLine()) != null) {
					marker = b.getFilePointer();
					if (line.length() > 0)
						try {
							parseLine(line, c);
						}
					catch (ConcurrentModificationException e) { }
					cache.put(c, Long.toString(marker));
					cache.store(new FileOutputStream(Engine.AWeSomeChatCache), "");
				}
				else
					Thread.sleep(500);
			}
			b.close();
		}
		catch (IOException e) { 
			System.err.println("Unable to tail file:");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.err.println("Thread was interrupted: \n");
			e.printStackTrace();
		}

	}

	private void parseLine(String line, String c) throws IOException {
		String prefix = Engine.colour+"14["+Engine.colour+"13AWeSome "+c+Engine.colour+"14]:"+Engine.colour;
		//System.err.println(line);
		
		if (line.contains("[INFO] <")) {
			String[] data = line.split(" ");
			String u = data[3].replaceAll("(<|>)", "");
			String message = data[4];
			if (!online.contains(u))
				online.add(u);
			for (int x = 5; x < data.length; x++)
				message = message+" "+data[x];
			if (!message.startsWith("P ")) {
				writeToLog(data[0]+" "+data[1]+" "+u+": "+message, c);
				if (engine != null) {
					engine.send("PRIVMSG #QuestHelp :"+prefix+" "+u+": "+message);
				}
				else {
					System.err.println(u+": "+message);
				}
			}
		}
		
		else if (line.contains("lost connection:")) {
			String u = line.split(" ")[3];
			if (online.contains(u)) {
				online.remove(u);
				writeToLog(line.split(" ")[0]+" "+line.split(" ")[1]+" "+u+" disconnected.", c);
				if (engine != null) {
					engine.send("PRIVMSG #QuestHelp :"+prefix+" "+u+" disconnected.");
				}
				else
					System.err.println(u+" disconnected.");
			}
		}

		else if (line.contains("logged in with entity id")) {
			String u = line.split(" ")[3].replaceAll("\\[.*\\]", "");
			if (!online.contains(u))
				online.add(u);
			writeToLog(line.split(" ")[0]+" "+line.split(" ")[1]+" "+u+" connected.", c);
			if (engine != null) {
				engine.send("PRIVMSG #QuestHelp :"+prefix+" "+u+" connected.");
			}
			else
				System.err.println(u+" connected.");
		}
		
		else if (line.contains("Stopping server")) {
			if (!online.isEmpty()) {
				for (String a : online) {
					online.remove(a);
					writeToLog(line.split(" ")[0]+" "+line.split(" ")[1]+" "+a+" disconnected. (Server stopping!)", c);
					if (engine != null)
						engine.send("PRIVMSG #QuestHelp :"+prefix+" "+a+" disconnected. (Server stopping!)");
					else
						System.err.println(a+" disconnected.");
				}
			}
		}
		
	}

	public void writeToLog(String s, String c) throws IOException {
		//RandomAccessFile a = new RandomAccessFile(new File(logout,"chat_"+c.toLowerCase()+".log"), "rw");
		FileWriter a = new FileWriter(new File(logout,"chat_"+c.toLowerCase()+".log"), true);
		a.write(s+"\r\n");
		a.close();
	}

	public void sendPlayers(String sendPrefix, String c) throws IOException {
		String prefix = Engine.colour+"14["+Engine.colour+"13AWeSome "+c+Engine.colour+"14]:"+Engine.colour;
		String o = "";
		if (online.isEmpty())
			o = Engine.colour+"14 Nobody is currently online :(";
		else {
			int a = online.size();
			o = " "+c2(Integer.toString(a))+" "+c1((a > 1 ? "people" : "person"))+" online: "+c2(online.get(0));
			for (int x = 1; x < online.size(); x++)
				o = o+c1(", ")+c2(online.get(x));				
		}
		engine.send(sendPrefix+prefix+o);
	}

	private String c1(String s) {
		return Engine.colour+"14"+s;
	}

	private String c2(String s) {
		return Engine.colour+"13"+s;
	}

}
