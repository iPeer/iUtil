package com.ipeer.iutil.engine;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

public class MCChat implements Runnable {

	private String stream;
	public static Properties cache;
	protected Engine engine;
	boolean IS_RUNNING = false;
	public long updateAt = 0;
	private String AWeSomeCacheLine = "";
	private boolean firstRun = true;
	public boolean silent = true;
	private int errorCount = 0;

	public MCChat(String stream, Engine engine) {
		this.silent = true;
		this.engine = engine;
		this.stream = stream;
		cache = new Properties();
		try {
			cache.load(new FileInputStream(Engine.MCChatCache));
		} 
		catch (Exception e) { }
	}

	public static void main(String[] args) {
		MCChat c = new MCChat("http://auron.co.uk/mc/chat.php", null);
		c.start();
	}

	public void start() {
		System.out.println("MINDCRACK: "+this.IS_RUNNING);
		if (this.IS_RUNNING)
			return;
		IS_RUNNING = true;
		(new Thread(this, "Minecraft Chat Announcer")).start();
	}

	public void stop() {
		IS_RUNNING = false;
	}

	@Override
	public void run() {
		while (IS_RUNNING/* && engine.isConnected*/) {
			try {
				InputStream in;
				String firstLine = "";
				String lastLine = "";
				List<String> messages = new ArrayList<String>();
				try {
					cache.load(new FileInputStream(Engine.MCChatCache));
				} 
				catch (Exception e) { }
				if (cache.containsKey(stream))
					lastLine = cache.getProperty(stream);
				//System.err.println(lastLine);
				if (stream.contains("guudelp")) {
					URL u = new URL(stream);
					in = u.openStream();
					Scanner s = new Scanner(in, "UTF-8");
					while (s.hasNextLine()) {
						String l = s.nextLine();
						if (l.startsWith("<font face=\"Arial\" size=\"1\">")) {
							l = l.split("<b>")[1].split("</b>")[0]+": "+l.split("</b> - ")[1].split(" </font>")[0];
							if (l.equals(lastLine)) {
								if (firstLine.equals(""))
									firstLine = lastLine;
								break;
							}
							if (firstLine.equals(""))
								firstLine = l;
							messages.add(l);
						}
					}
					cache.put(stream, firstLine);
					saveMessageCache();
					char colour = Engine.colour;
					if (silent && messages.size() > 10) {
						silent = false;
						if (!Engine.channels.isEmpty() && messages.size() > 0) {
							for (Channel c : Engine.channels.values()) {
								if (c.getName().equals("#questhelp"))
									engine.send("PRIVMSG "+c.getName()+" :"+colour+"14["+colour+"13Mindcrack Chat"+colour+"14]:"+colour+" "+messages.size()+" messages skipped due to startup.");
							}
						}
					}
					else {
						if (silent)
							silent = false;
						if (errorCount > 0) {
							errorCount = 0;
						}
						int x = messages.size() - 1;
						for (; x >= 0; x--) {
							if (!Engine.channels.isEmpty()) {
								for (Channel c : Engine.channels.values()) {
									if (c.getName().equals("#questhelp"))
										engine.send("PRIVMSG "+c.getName()+" :"+colour+"14["+colour+"13Mindcrack Chat"+colour+"14]: "+colour+messages.get(x));
								}
							}
							//System.err.println(messages.get(x));
						}
					}
				}
				else if (stream.contains("auron")) {
					URL u = new URL(stream);
					in = u.openStream();
					Scanner s = new Scanner(in, "UTF-8");
					while (s.hasNextLine()) {
						String rawLine = s.nextLine();
						String l = rawLine.replaceFirst("&lt;", "�").replaceFirst("&gt;", "&");
						rawLine = rawLine.replaceAll("<br />", "");
						//System.err.println(rawLine+", "+lastLine+ " ("+rawLine.equals(lastLine)+") - "+AWeSomeCacheLine+", "+l);
						if (l.equals("<br />")) {
							System.err.println("EOF");
							firstLine = AWeSomeCacheLine;
							break;
						}
						l = l.split("�")[1].replaceFirst("&", ":").replaceAll("&lt;", "<").replaceAll("&gt;", ">");
						if (AWeSomeCacheLine.equals(""))
							AWeSomeCacheLine = rawLine;
						//System.err.println(rawLine.equals(lastLine)+" - "+rawLine+", "+lastLine);
						if (rawLine.equals(lastLine)) {
							//System.err.println("REACHED CACHED LINE!");
							//System.err.println(firstLine+", "+lastLine);
							if (firstLine.equals(""))
								firstLine = lastLine;
							break;
						}
						if (firstLine.equals(""))
							firstLine = AWeSomeCacheLine;
						messages.add(l/*.replaceAll("iPeer", "iP�er")*/.replaceAll("clbyt", "clb�t").replaceAll("CLBYT", "CLB�T"));
					}
					cache.put(stream, firstLine);
					saveMessageCache();
					char colour = Engine.colour;
					int x = messages.size() - 1;
					for (; x >= 0; x--) {
						if (engine != null) {
							if (!Engine.channels.isEmpty()) {
								for (Channel c : Engine.channels.values()) {
									if (c.getName().equals("#questhelp"))
										engine.send("PRIVMSG "+c.getName()+" :"+colour+"14["+colour+"13AWeSome"+(stream.contains("creative") ? " Creative " : " ")+"Chat"+colour+"14]: "+colour+messages.get(x));
								}
							}
						}
						//System.err.println(messages.get(x));
					}
				}
				this.updateAt = System.currentTimeMillis();
				if (stream.contains("guudelp") && firstRun) {
					firstRun = false;
					Thread.sleep(120000+(new Random().nextInt(60000)));
				}
				else
					Thread.sleep(120000);
			}
			catch (Exception e) {
				if (!Engine.channels.isEmpty()) {
					errorCount++;
					for (Channel c : Engine.channels.values())
						try {
							if (errorCount <= 3)
								engine.send("PRIVMSG "+c.getName()+" :[ERROR] ("+(stream.contains("guude") ? "Mindcrack" : "AWeSome")+" - Strike: "+errorCount+") "+e.toString()+" @ "+e.getStackTrace()[0]);
							if (errorCount == 3) {
								engine.send("PRIVMSG "+c.getName()+" :[ERROR] Chat has errored 3 times or more, switching to longer update interval. Once a successful update happens, original interval will be restored.");
							}
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
				}
				System.err.println(e.toString()+" @ "+e.getStackTrace()[0]);
				try {
					Thread.sleep(errorCount >= 3 ? 600000 : 120000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	} 

	private void saveMessageCache() throws IOException {
		//System.err.println("[MCChat] Saving cache!");
		cache.store(new FileOutputStream(Engine.MCChatCache), "MCChat Cache");
	}


}

