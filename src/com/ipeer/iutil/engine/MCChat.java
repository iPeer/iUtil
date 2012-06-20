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
	boolean IS_RUNNING = true;
	public long updateAt = 0;
	private String AWeSomeCacheLine = "";
	private boolean firstRun = true;
	private boolean silent = true;

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
		IS_RUNNING = true;
		(new Thread(this)).start();
	}

	public void stop() {
		IS_RUNNING = false;
	}

	@Override
	public void run() {
		while (IS_RUNNING) {
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
								if (!c.getName().equals("#peer.dev"))
									engine.send("PRIVMSG "+c.getName()+" :"+colour+"14["+colour+"13Mindcrack Chat"+colour+"14]:"+colour+" "+messages.size()+" messages skipped due to startup.");
							}
						}
					}
					else {
						if (silent)
							silent = false;
						int x = messages.size() - 1;
						for (; x >= 0; x--) {
							if (!Engine.channels.isEmpty()) {
								for (Channel c : Engine.channels.values()) {
									if (!c.getName().equals("#peer.dev"))
										engine.send("PRIVMSG "+c.getName()+" :"+colour+"14["+colour+"13Mindcrack Chat"+colour+"14]: "+colour+messages.get(x));
								}
							}
							System.out.println(messages.get(x));
						}
					}
				}
				else if (stream.contains("auron")) {
					URL u = new URL(stream);
					in = u.openStream();
					Scanner s = new Scanner(in, "UTF-8");
					while (s.hasNextLine()) {
						String l = s.nextLine().replaceFirst("&lt;", "€").replaceFirst("&gt;", "&");
						if (l.equals("<br />")) {
							firstLine = AWeSomeCacheLine;
							break;
						}
						l = l.split("€")[1].replaceFirst("&", ":").replaceAll("&lt;", "<").replaceAll("&gt;", ">");
						if (AWeSomeCacheLine.equals(""))
							AWeSomeCacheLine = l;
						if (l.equals(lastLine)) {
							if (firstLine.equals(""))
								firstLine = lastLine;
							break;
						}
						if (firstLine.equals(""))
							firstLine = l;
						messages.add(l/*.replaceAll("iPeer", "iPéer")*/.replaceAll("clbyt", "clbÿt"));
					}
					cache.put(stream, firstLine);
					saveMessageCache();
					char colour = Engine.colour;
					int x = messages.size() - 1;
					for (; x >= 0; x--) {
						if (!Engine.channels.isEmpty()) {
							for (Channel c : Engine.channels.values()) {
								if (!c.getName().equals("#peer.dev"))
									engine.send("PRIVMSG "+c.getName()+" :"+colour+"14["+colour+"13AWeSome"+(stream.contains("creative") ? " Creative " : " ")+"Chat"+colour+"14]: "+colour+messages.get(x));
							}
						}
						System.out.println(messages.get(x));
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
					for (Channel c : Engine.channels.values())
						try {
							engine.send("PRIVMSG "+c.getName()+" :[ERROR] "+e.toString()+" "+(stream.contains("guude") ? "Mindcrack" : "AWeSome")+" @ "+e.getStackTrace()[0]);
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
				}
				System.err.println(e.toString()+" @ "+e.getStackTrace()[0]);
				try {
					Thread.sleep(120000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	} 

	private void saveMessageCache() throws IOException {
		System.err.println("[MCChat] Saving cache!");
		cache.store(new FileOutputStream(Engine.MCChatCache), "MCChat Cache");
	}


}

