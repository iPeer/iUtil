package com.ipeer.iutil.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ipeer.iutil.cache.Cache;

public class VideoChecker implements Runnable {

	private static List<String> userNames = new ArrayList<String>();
	private static Properties videoIDCache = new Properties();
	public boolean isRunning = false;
	public static Thread updateThread;
	protected Engine engine;
	public long updateAt = 0;
	public Cache cache;

	public VideoChecker(Engine engine) throws FileNotFoundException, IOException {
		this.engine = engine;
		this.cache = new Cache(engine);
		if (videoIDCache == null) 
			videoIDCache = new Properties();
		File IDCacheFile = (Engine.YouTubeVIDCache != null ? Engine.YouTubeVIDCache : new File("F:\\Dropbox\\Java\\iUtil\\caches\\ircswiftircnet\\YouTube.iuc"));
		if (IDCacheFile.exists())
			videoIDCache.load(new FileInputStream(IDCacheFile));
		System.out.println("CACHED IDs: "+videoIDCache.toString());
		loadUsernames();
	}

	public void start() {
		isRunning = true;
		(new Thread(this, "YouTube Upload Detector")).start();
	}

	public void stop() {
		isRunning = false;
	}

	public void addUser(String user, String channel) throws FileNotFoundException, IOException {
		try {
			if (userNames.contains(user))
				engine.send("PRIVMSG "+channel+" :"+user+" is already on the update list...");
			else {
				userNames.add(user);
				saveUsernames();
				engine.send("PRIVMSG "+channel+" :Adding "+user+" to update list, please stand by...");
				updateUserNames();
			}
		}
		catch (Exception e) {
			engine.send("PRIVMSG "+channel+" :Uh oh... "+e.getMessage());
			e.printStackTrace();
		}
	}

	public void removeUser(String user, String channel) throws FileNotFoundException, IOException {
		userNames.remove(user);
		videoIDCache.remove(user);
		engine.send("PRIVMSG "+channel+" :Removed "+user+" from update list");
		saveUsernames();
		saveIDCache();
	}

	public final void loadUsernames() throws FileNotFoundException, IOException {
		File usersFile = (Engine.YouTubeUsernameConfig != null ? Engine.YouTubeUsernameConfig : new File("F:\\Dropbox\\Java\\iUtil\\config\\YouTubeUsernameConfig.cfg"));
		Properties a = new Properties();
		if (usersFile.exists()) {
			a.load(new FileInputStream(usersFile));
			String b = a.getProperty("users");
			String[] c = b.split(",");
			for (int d = 0; d < c.length; d++) {
				userNames.add(c[d]);
			}
		}
		else {	
			userNames.add("EthosLab");
			userNames.add("BdoubleO100");
			userNames.add("GuudeBoulderfist");
			userNames.add("KurtjMac");
			userNames.add("W92Baj");
			userNames.add("PauseUnpause");
			userNames.add("VintageBeef");
			this.saveUsernames();
		}
		System.out.println("Users: "+userNames.toString());
	}

	public List<String> getUsernames() {
		return userNames;
	}

	public void saveUsernames() throws FileNotFoundException, IOException {
		File usersFile = (Engine.YouTubeUsernameConfig != null ? Engine.YouTubeUsernameConfig : new File("F:\\Dropbox\\Java\\iUtil\\config\\YouTubeUsernameConfig.cfg"));
		Properties a = new Properties();
		String saveString = "";
		for (String user : userNames)
			if (saveString.equals(""))
				saveString = user;
			else
				saveString = saveString+","+user;
		a.put("users", saveString);
		a.store(new FileOutputStream(usersFile), "User List File");
	}

	@Override
	public void run() {
		while (isRunning && engine.isConnected) {
			updateUserNames();
			if (Engine.channels.size() > 0)
				try {
					updateAt = System.currentTimeMillis();
					Thread.sleep(600000); // Sleep for 10 minutes
				} catch (InterruptedException e) {
					try {
						engine.send("PRIVMSG "+Engine.channels.get(0).getName()+" :Unable to sleep!");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
		}
	}

	public void updateUserNames() {
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		List<String> outList = new ArrayList<String>();
		if (videoIDCache == null)
			videoIDCache = new Properties();
		try {
			DocumentBuilder a = f.newDocumentBuilder();
			Document doc = a.newDocument();
			String duration, title, videoID;
			duration = title = videoID = "";
			for (int x = 0; x < userNames.size(); x++) {
				String user = userNames.get(x);
				doc = a.parse("http://gdata.youtube.com/feeds/api/users/"+user+"/uploads");
				Element e = doc.getDocumentElement();
				e.normalize();
				NodeList n3 = e.getElementsByTagName("media:group");
				Node node = n3.item(0);
				try {
					n3 = node.getChildNodes();

					for (int q1 = 0; q1 < n3.getLength(); q1++) {
						String nodeName = n3.item(q1).getNodeName();
						if (nodeName.equals("media:title"))
							title = n3.item(q1).getChildNodes().item(0).getNodeValue();
					}
					NodeList n = e.getElementsByTagName("media:content");
					NamedNodeMap n2 = n.item(0).getAttributes();
					for (int q = 0; q < n2.getLength(); q++) {
						String nodeName = n2.item(q).getNodeName();
						if (nodeName.equals("duration"))
							duration = n2.item(q).getNodeValue();
					}
					NodeList n1 = e.getElementsByTagName("media:player");
					NamedNodeMap nn1 = n1.item(0).getAttributes();
					for (int x1 = 0; x1 < nn1.getLength(); x1++) {
						String nodeName = nn1.item(x1).getNodeName();
						if 	(nodeName.equals("url"))
							videoID = nn1.item(x1).getNodeValue().split("\\?v=")[1].split("&feature")[0];
					}
					int time = Integer.parseInt(duration);
					int minutes = time / 60;
					int seconds = time % 60;
					int hours = 0;
					while (minutes >= 60) {
						hours++;
						minutes -= 60;
					}
					String newDuration = (hours > 0 ? (hours < 10 ? "0"+hours : hours)+":" : "")+(minutes < 10 ? "0"+minutes : minutes)+":"+(seconds < 10 ? "0"+seconds : seconds);
					char dash = 8212;
					String o = Engine.colour+"13"+user+Engine.colour+"14 uploaded a video: "+Engine.colour+"13"+title+Engine.colour+"14 ["+Engine.colour+"13"+newDuration+Engine.colour+"14] "+dash+" "+Engine.colour+"13http://youtu.be/"+videoID;
					if (!videoIDCache.containsKey(user))
						videoIDCache.put(user, "Pending");
					//System.err.println(user+": "+videoIDCache.get(user)+" "+videoID+" "+!videoIDCache.get(user).equals(videoID));
					if (!videoIDCache.get(user).equals(videoID)) {
						if ((!user.equals("TheGameStation")) || (user.equals("TheGameStation") && title.startsWith("The Game Station Podcast")))
							outList.add(o);
						videoIDCache.put(user, videoID);
						cache.addVideo(user, title, videoID);
						//System.out.println(o);
					}				
				}
				catch (NullPointerException np) { }
				catch (StringIndexOutOfBoundsException siobe) { siobe.printStackTrace(); }
			}
			if (Engine.channels.size() > 0 && outList.size() > 0)
				engine.msgList("#questhelp", outList);
			//				for (Channel channel : Engine.channels.values())
			//					if (!channel.getName().equals("#peer.dev"))
		} catch (Exception e) {
			Map<String, Channel> channels = Engine.channels;
			for (Channel channel : channels.values()) {
				try {
					engine.send("PRIVMSG "+channel.getName()+" :An error occured: "+e.toString()+" @ "+e.getStackTrace()[0]);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
			if (Engine.channels.size() > 0 && outList.size() > 0)
				for (Channel channel : Engine.channels.values())
					if (!channel.getName().toLowerCase().equals("#peer.dev"))
						try {
							engine.msgList(channel.getName(), outList);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
		}
		try {
			saveIDCache();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveIDCache() throws IOException {
		try {
			File IDCacheFile = (Engine.YouTubeVIDCache != null ? Engine.YouTubeVIDCache : new File("F:\\Dropbox\\Java\\iUtil\\caches\\ircswiftircnet\\YouTube.iuc"));
			videoIDCache.store(new FileOutputStream(IDCacheFile), "Video ID Cache File");
		} catch (FileNotFoundException e) {
			engine.send("PRIVMSG "+Engine.channels.get(0).getName()+" :Unable to save ID Cache file: File not found.");
			e.printStackTrace();
		} catch (IOException e) {
			engine.send("PRIVMSG "+Engine.channels.get(0).getName()+" :Unable to save ID Cache file: an IO exception occured.");
			e.printStackTrace();
		} 
	}

	public static void main(String[] args) {
		VideoChecker v;
		try {
			v = new VideoChecker(Engine.engine);
			v.cache.addVideo("BdoubleO100", "Building with BdoubleO - Episode 70 - A shop for precious", "uS0mhrmgWZk");
			v.cache.addVideo("BdoubleO100", "OOGE - Vinyl Fantasy 2 - Episode 19", "pSKpgR97nvA");
			v.cache.addVideo("GenerikB", "Redstone Academy Ep 4 - \"Logic Gates & Practical Uses For Them (AND, XOR, etc)\"", "dg72sN7Qb4Y");
			v.updateUserNames();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
