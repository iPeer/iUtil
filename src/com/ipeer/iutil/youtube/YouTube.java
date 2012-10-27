package com.ipeer.iutil.youtube;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.ipeer.iutil.cache.Cache;
import com.ipeer.iutil.engine.Engine;

public class YouTube {

	public Map<String, Channel> channels = new HashMap<String, Channel>();
	public static Cache cache;
	public static List<Channel> sync;

	public YouTube(Engine engine) throws FileNotFoundException, IOException { 
		cache = new Cache(engine);
		sync = new ArrayList<Channel>();
		try {
			cache.loadFromFile();
		}
		catch (IOException e) { }
	}

	public static void main(String[] args) {
		try {
			YouTube a = new YouTube(null);
			a.loadChannels();
			a.saveUsernames();
		}
		catch (Exception e) {
			System.err.println("Unable to load youtube usernames:");
			e.printStackTrace();
		}
	}

	public void updateChannel() {
		updateChannel("");
	}

	public void updateChannel(String channel) {
		try {
			if (channel.equals(""))
				for (Channel a : channels.values())
					a.update();
			else
				if (channels.containsKey(channel.toLowerCase()))
					(channels.get(channel.toLowerCase())).update();
				else
					System.err.println("[ERR, YouTube] Not a watched channel!");
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void saveUsernames() throws FileNotFoundException, IOException {
		File usersFile = (Engine.YouTubeUsernameConfig != null ? Engine.YouTubeUsernameConfig : new File("F:\\Dropbox\\Java\\iUtil\\config\\YouTubeUsernameConfig.cfg"));
		Properties a = new Properties();
		String saveString = "";
		String user = "";
		//Iterator it = channels.keySet().iterator();
		for (Channel c : channels.values()) {
			user = c.getChannelName();
			if (saveString.equals(""))
				saveString = user;
			else
				saveString = saveString+","+user;
		}
		a.put("users", saveString);
		a.store(new FileOutputStream(usersFile), "User List File");
	}

	public void addChannel(String user) throws FileNotFoundException, IOException {
		Channel a = new Channel(user);
		channels.put(user.toLowerCase(), a);
		sync.add(a);
		saveUsernames();
		//a.start();
	}

	public void removeChannel(String user) {
		channels.get(user.toLowerCase()).stop();
		channels.remove(user.toLowerCase());
	}

	public void loadChannels() throws IOException, SAXException, ParserConfigurationException {
		File a = Engine.YouTubeUsernameConfig;
		String[] d = "EthosLab,BdoubleO100,GuudeBoulderfist,Kurtjmac,W92Baj,PauseUnpause,VintageBeef,Docm77".split(",");
		if (a.exists()) {
			Properties b = new Properties();
			b.load(new FileInputStream(a));
			String c = b.getProperty("users");
			d = c.split(",");
		}
		for (String e : d) {
			Channel f = new Channel(e);
			f.start();
			channels.put(e.toLowerCase(), f);
		}
	}

	public void saveAllCaches() {
		try {
			cache.saveToFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Channel a : channels.values())
			try {
				a.saveCache();
			} catch (Exception e) {
				e.printStackTrace();
			}
		try {
			cache.saveToFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stopAll() {
		for (Channel a : channels.values())
			a.stop();
	}

	public void startAll() {
		for (Channel a : channels.values())
			a.start();
	}

	public String formatDuration(String dur) {
		int time = Integer.parseInt(dur);
		int minutes = time / 60;
		int seconds = time % 60;
		int hours = 0;
		while (minutes >= 60) {
			hours++;
			minutes -= 60;
		}
		return (hours > 0 ? (hours < 10 ? "0"+hours : hours)+":" : "")+(minutes < 10 ? "0"+minutes : minutes)+":"+(seconds < 10 ? "0"+seconds : seconds);
	}

}
