package com.ipeer.iutil.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.ipeer.iutil.json.EmptyJSONFileException;
import com.ipeer.iutil.json.JSONReader;

public class TwitchAnnounce implements Runnable {

	private List<String> users;
	private boolean IS_RUNNING = false;
	private Thread thread;
	private Engine engine;
	public long updateAt = 0L;
	private int errors = 0;
	private boolean errored = false;
	private int successes = 0;
	public long updateDelay = 600000;

	public TwitchAnnounce(Engine engine) {
		users = new ArrayList<String>();
		//		users.add("BdoubleO");
		//		users.add("Generikb");
		//		users.add("Kurtjmac");
		//		users.add("Harumei");
		//		users.add("Nebris");
		loadUsers();
		this.engine = engine;
	}

	public static void main(String[] args) throws MalformedURLException, IOException {	
		TwitchAnnounce a = new TwitchAnnounce(null);
		for (String b : a.users)
			System.err.println(b);
		a.start();
	}

	public void start() {
		if (IS_RUNNING)
			return;
		IS_RUNNING = true;
		(thread = new Thread(this, "Twitch.TV Announcer")).start();
	}
	public void stop() { 
		IS_RUNNING = false;
		thread.interrupt();
	}
	public void run() { 
		while (IS_RUNNING && !thread.isInterrupted()) {
			for (String u : users)
				check(u);
			updateAt = System.currentTimeMillis();
			try {
				Thread.sleep(updateDelay);
			} catch (InterruptedException e) {
				thread.interrupt();
				IS_RUNNING = false;
				e.printStackTrace();
			}
		}
	}


	public void check(String user) {
		File userCache = new File(Engine.twitchCache, user+".iuc");
		Properties prop = new Properties();
		try {
			JSONReader a = new JSONReader(new URL("https://api.justin.tv/api/stream/list.json?channel="+user));
			String game = a.get("meta_game");
			String status = a.get("status");
			if (userCache.exists()) {
				prop.load(new FileInputStream(userCache));
				String lastGame = prop.getProperty("lastGame", "");
				String lastStatus = prop.getProperty("lastStatus", "");
				if ((game+" "+status).equals((lastGame+" "+lastStatus)))
					return;
				else {
					prop.put("lastGame", game);
					prop.put("lastStatus", status);
					announce(user, status, game);
				}

			}
			else {
				prop.put("lastGame", game);
				prop.put("lastStatus", status);
				announce(user, status, game);
			}
			successes++;
			if (successes >= users.size()) {
				errored = false;
				errors = 0;
				updateDelay = 600000;
			}				
			prop.store(new FileOutputStream(userCache), "Twitch.TV Cache for "+user);
		} catch (EmptyJSONFileException e) {
			if (userCache.exists()) {
				announce(user, "", "", 2);
				userCache.delete();
			}
		} catch (Exception e) {
			String e1 = "[Twitch, Strike: "+errors+"] The following error occured while updating "+user+": "+e.toString();
			if (engine == null)
				System.err.println(e1);
			else
				if (!e1.contains("HTTP response code: 408")) {
					errors++;
					successes = 0;
					e1 = "[Twitch, Strike: "+errors+"] The following error occured while updating "+user+": "+e.toString();
					if (errors >= 3) {
						errored = true;
						updateDelay = 1800000;
						if (errors == 3)
							engine.amsg("[Twitch] Due to prolonged errors the update time for this thread has been increased. Once a full update successfully completes the delay will be reset to the normal interval.");
						return;
					}
					engine.amsg(e1); 
				}
			engine.getServer().sendToAllAdminClients(e1);
			e.printStackTrace();
		}
	}

	public void announce(String u, String s, String g) {
		announce(u, s, g, 1);
	}

	public void announce(String u, String s, String g, int t) {
		String out = "";
		switch (t) {
		case 1:
			String url = "http://twitch.tv/"+u;
			char dash = 8212;
			out = c2(u)+c1(" is LIVE ")+(s.equals("") ? "" : "with ")+c2(s)+(!g.equals("") ? c1(" playing ")+c2(g) : "")+c1(" "+dash+" ")+c2(url);
			if (engine.serverEnabled())
				engine.getServer().sendToAllClients("\247d"+u+" \2478is LIVE "+(s.equals("") ? "" : "with \247d")+c2(s)+(!g.equals("") ? " \2478playing \247d"+g : "")+" \2478"+dash+" \247d"+url);
			if (engine == null)
				System.err.println(out);
			else
				engine.amsg(out);
			break;
		case 2:
			out = c2(u)+c1(" is no longer live.");
			if (engine == null)
				System.err.println(out);
			else
				engine.amsg(out);
			break;
		}
	}

	public String c1(String s) { 
		return Engine.colour+"14"+s;
	}

	public String c2(String s) { 
		return Engine.colour+"13"+s;
	}

	public void saveUsers() {
		try {
			File saveFile = Engine.twitchConfig;
			System.err.println(saveFile.getAbsolutePath());
			FileWriter out = new FileWriter(saveFile);
			for (String a : users)
				out.write(a+"\n");
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void loadUsers() {
		File a = Engine.twitchConfig;
		try {
			if (!users.isEmpty())
				users.clear();
			FileInputStream in = new FileInputStream(a);
			Scanner s = new Scanner(in, "UTF-8");
			while (s.hasNextLine())
				users.add(s.nextLine());
			s.close();
		} catch (FileNotFoundException e) {
			users.add("BdoubleO");
			users.add("Generikb");
			users.add("Kurtjmac");
			users.add("Harumei");
			users.add("Nebris");
			users.add("TotalBiscuit");
		}
	}

	public void addUser(String u) {
		users.add(u);
		saveUsers();
	}

	public void delUser(String user) {
		if (users.contains(user))
			users.remove(user);
		saveUsers();
	}

}
