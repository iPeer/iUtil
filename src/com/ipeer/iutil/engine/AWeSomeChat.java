package com.ipeer.iutil.engine;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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
	private String server;
	//private Properties deathCounter;
	private File deathsFile = new File("AWeSomeDeaths.dat");
	private Thread thread;

	public AWeSomeChat(Engine engine, String file) {
		this.engine = engine;
		this.file = new File(file);
		//this.deathCounter = new Properties();
		loadDeathCounterData();
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

	public void loadDeathCounterData() {
		File a = new File("AWeSomeDeaths.dat");
		if (a.exists())
			try {
				ThreadUtils.deathCounter.load(new FileInputStream(a));
			} catch (Exception e) {
				System.err.println("Unable to load death counter data");
				e.printStackTrace();
			}
	}

	public void start() {
		if (this.IS_RUNNING)
			return;
		this.IS_RUNNING = true;
		String s = ((file.getAbsolutePath()).toLowerCase().contains("creative")) ? "Creative" : (file.getAbsolutePath().toLowerCase().contains("ftb") ? "FTB" : "Survival");
		this.server = s;
		System.out.println("Starting server chat crawler for "+s+" server");
		(thread = new Thread(this, "AWeSome Chat ("+s+")")).start();
	}

	public void stop() {
		this.IS_RUNNING = false;
		thread.interrupt();
	}

	public static void main(String[] args) throws IOException {
		AWeSomeChat a = new AWeSomeChat(null, /*"/home/minecraft/servers/survival/server.log"*/"F:/MC Server/server.log");
		//a.parseLine("2012-09-06 20:11:30 [INFO] <iPeer> I'm still testing my tailing", "Survival");
		//a.start();
		a.setOnline("iPeer");
		a.setOnline("Auron956");
		//a.start();
/*		a.parseLine("2013-01-09 14:58:53 [INFO] Auron956 lost connection: disconnect.endOfStream", "survival");
		a.parseLine("2013-01-09 16:13:45 [INFO] iPeer lost connection: disconnect.quitting", "survival");*/
		a.parseLine("2013-02-05 01:04:08 [WARNING] Failed to handle packet for Auron956/78.105.52.16: java.lang.NullPointerException", "ftb");
		a.parseLine("2013-04-09 15:56:13 [INFO] Disconnecting iPeer [/2.26.159.173:51602]: Failed to verify username! [internal error java.io.IOException: Server returned HTTP response code: 503 for URL: http://session.minecraft.net/game/checkserver.jsp?user=iPeer&serverId=-3e6a7d55ab81fda07ab9f325732017e1241ec74d]", "FTB");
		a.parseLine("2013-04-17 17:18:04 [INFO] Done (5.485s)! For help, type \"help\" or \"?\"", "FtB");
		a.parseLine("2013-06-15 19:55:05 [INFO] * iPeer test", "Test");
		a.parseLine("2013-06-15 19:55:52 [INFO] iPeer lost connection: disconnect.quitting", "Test");
	}

	@Override
	public void run() {
		try {
			String c = ((file.getAbsolutePath()).toLowerCase().contains("creative")) ? "Creative" : (file.getAbsolutePath().toLowerCase().contains("ftb") ? "FTB" : "Survival");
			this.server = c;
			//Reader a = new FileReader(file);
			//BufferedReader b = new BufferedReader(a);
			readOnlineFromFile();
			RandomAccessFile b = new RandomAccessFile(file, "r");
			String line = null;
			System.out.println("Tailing file of "+this.server+", "+file.getAbsolutePath());
			while (IS_RUNNING && !Thread.interrupted()) {
				long marker = Long.valueOf(cache.getProperty(c, Long.toString(0)));
				//				cache.put(c, Long.toString(marker));
				//				cache.store(new FileOutputStream(Engine.AWeSomeChatCache), "");
				b.seek(marker);
				//System.out.println(b.length()+", "+b.toString());
				if ((line = b.readLine()) != null) {
					//System.out.println(line);
					long newMarker = b.getFilePointer();
					if (line.length() > 0)
						try {
							parseLine(line, c);
						}
					catch (ConcurrentModificationException e) { System.err.println("Concurrent Modification error ocurred ("+c+")"); }
					if (engine != null && engine.serverEnabled())
						engine.getServer().sendToAllAdminClients("AWeSome Chat -> ("+c+") O: "+marker+", N: "+newMarker);
					cache.put(c, Long.toString(newMarker));
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

		if (line.contains("[INFO] <")) {
			String[] data = line.split(" ");
			String u = data[3].replaceAll("(<|>)", "");
			String message = data[4];
			setOnline(u);
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

		else if (line.contains("Failed to verify") || (line.contains("lost connection:") || (line.contains("Failed to handle packet") || line.contains("left the game")))) {
			String u = line.split(" ")[3];
			String reason = line.split(" ")[6];
			String out = prefix+" "+u+" left the game."+(!reason.equals("disconnect.quitting") ? " ("+reason+")" : "");
					//2013-01-09 14:58:53 [INFO] Auron956 lost connection: disconnect.endOfStream
			if (line.contains("Failed to handle packet")) {
				u = line.split(" ")[8].split("/")[0];
				reason = "Failed to handle packet: "+line.split(":")[3].trim();
				out = prefix+" "+u+" was disconnected by the server ("+reason+")";
			}
			if (line.contains("Failed to verify")) {
				u = line.split(" ")[4];
				reason = line.split("\\]:")[1].split("\\[")[0].trim();
				out = prefix+" "+u+" was disconnected by the server ("+reason+")";
			}
			if (online.contains(u)) {
				setOffline(u);
				writeToLog(line.split(" ")[0]+" "+line.split(" ")[1]+" "+u+" left the game."+(!reason.equals("disconnect.quitting") ? " ("+reason+")" : ""), c);
				if (engine != null) {
					engine.send("PRIVMSG #QuestHelp :"+out);
				}
				else
					System.err.println(u+" disconnected."+(!reason.equals("disconnect.quitting") ? " ("+reason+")" : ""));
			}
		}

		else if (line.contains("logged in with entity id") || line.contains("joined the game")) {
			String u = line.split(" ")[3].replaceAll("\\[.*\\]", "");
			if (!online.contains(u)) {
				setOnline(u);
				writeToLog(line.split(" ")[0]+" "+line.split(" ")[1]+" "+u+" joined the game.", c);
				if (engine != null) {
					engine.send("PRIVMSG #QuestHelp :"+prefix+" "+u+" joined the game.");
				}
				else
					System.err.println(u+" connected.");
			}
		}
		else if (line.contains("[INFO] * ")) { // Actions
			String player = line.split(" ")[4];
			if (!online.contains(player))
				setOnline(player);
			writeToLog(line.split(" ")[0]+" "+line.split(" ")[1]+" * "+player+" "+line.split(player+" ")[1], c);
			if (engine != null)
				engine.send("PRIVMSG #QuestHelp :"+prefix+" * "+player+" "+line.split(player+" ")[1]);
			else
				System.err.println(" * "+player+" "+line.split(player+" ")[1]);
		}
		else if (line.contains("[INFO]") && online.contains(line.split(" ")[3]) && !line.contains("joined the game")) {	// deaths
			String out = "";
			String[] data = line.split(" ");
			String user = data[3];
			int deaths = (Integer.valueOf(ThreadUtils.deathCounter.getProperty(user+"-"+c, "0")) + 1);
			String deathsOut = user+" has died "+(deaths == 1 ? "for the first time!" : deaths+" times!");
			for (int x = 3; x < data.length; x++)
				out = out+(out.length() > 0 ? " " : "")+data[x];
			writeToLog(line.split(" ")[0]+" "+line.split(" ")[1]+" "+out, c);
			if (engine != null) {
				engine.send("PRIVMSG #QuestHelp :"+prefix+" "+out);
				engine.send("PRIVMSG #QuestHelp :"+prefix+" "+deathsOut);
			}
			else {
				System.err.println(out);
				System.err.println(deathsOut);
			}
			ThreadUtils.deathCounter.setProperty(user+"-"+c, Integer.toString(deaths));
			ThreadUtils.deathCounter.store(new FileOutputStream(deathsFile), "AWeSome Death Counter Data");
		}
		else if (line.contains("For help, type \"help\" or \"?\"")) {
			if (engine != null)
				engine.send("PRIVMSG #QuestHelp :"+c+" server has started.");
			else
				System.out.println(c+" has started.");
		}
		else if (line.contains("Stopping the server")) {
			if (!online.isEmpty()) {
				for (String a : online) {
					online.remove(a);
					writeToLog(line.split(" ")[0]+" "+line.split(" ")[1]+" "+a+" disconnected. (Server stopping!)", c);
					writeOnlineToFile();
					if (engine != null)
						engine.send("PRIVMSG #QuestHelp :"+prefix+" "+a+" disconnected. (Server stopping!)");
					else
						System.err.println(a+" disconnected.");
				}
			}
			if (engine != null)
				engine.send("PRIVMSG #QuestHelp :"+c+" server has been stopped.");
		}

	}

	@SuppressWarnings("unused")
	@Deprecated
	private void toggleOnlineStatus(List<String> a) {
		for (String b : a)
			online.remove(b);
		writeOnlineToFile();
	}

	@SuppressWarnings("unused")
	@Deprecated
	private void toggleOnlineStatus(String u) {
		if (online.contains(u))
			online.remove(u);
		else
			online.add(u);
		writeOnlineToFile();
	}

	public void setOffline(String user) {
		if (!online.contains(user) || online.isEmpty())
			return;
		online.remove(user);
		if (engine != null && engine.serverEnabled())
			engine.getServer().sendToAllAdminClients("AweSome Chat ("+this.server+") -> set offline: "+user);
		writeOnlineToFile();
	}

	public void setOnline(String user) {
		if (online.contains(user))
			return;
		online.add(user);
		if (engine != null) {
			if (engine.serverEnabled())
				engine.getServer().sendToAllAdminClients("AweSome Chat ("+this.server+") -> set online: "+user);
		}
		else {
			System.out.println("AweSome Chat ("+this.server+") -> set online: "+user);
		}
		writeOnlineToFile();
	}

	private void readOnlineFromFile() {
		try {
			File a = new File("AWeSomeOnline-"+server+".txt");
			if (!a.exists())
				return;
			if (online == null)
				online = new ArrayList<String>();
			BufferedReader b = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(a))));
			String l = "";
			while ((l = b.readLine()) != null)
				online.add(l);
			b.close();
		} catch (Exception e) {
			System.err.println("["+server+"] Unable to load online users!");
			e.printStackTrace();
		}

	}

	private void writeOnlineToFile() {
		try {
			File a = new File("AWeSomeOnline-"+server+".txt");
			if (online.isEmpty()) {
				a.delete();
				return;
			}
			FileWriter b = new FileWriter(a);
			for (String c : online)
				b.write(c+"\r\n");
			b.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
