package com.ipeer.iutil.youtube;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ipeer.iutil.engine.Engine;

public class Channel implements Runnable {

	private String user;
	private String channelName;
	public Map<String, Upload> uploads = new HashMap<String, Upload>();
	public Properties cache;
	private boolean IS_RUNNING;

	public Channel(String user) throws FileNotFoundException, IOException {
		this.cache = new Properties();
		File a = new File(Engine.YouTubeVIDCache, user+".iuc");
		if (a.exists())
			this.cache.load(new FileInputStream(a));
		else {
			a.createNewFile();
			this.cache.put("lastID", "Pending...");
		}
		this.user = this.channelName = user;
		this.uploads = loadCache();
	}

	public void start() {
		this.IS_RUNNING = true;
		(new Thread(this, "YouTube Announcer ("+this.user+")")).start();
	}

	public void stop() {
		this.IS_RUNNING = false;
		run();
	}

	@Override
	public void run() {
		while (IS_RUNNING) {
			try {
				update();

				Thread.sleep(600000/* + (long)new Random().nextInt(120000)*/);
			}
			catch (NullPointerException e) { 
				e.printStackTrace();
				break;
			}
			catch (Exception e) { 
				String e1 = "The following error occured while updating "+this.user+": "+e.toString()+": "+e.getStackTrace()[0];
				Engine e2 = Engine.engine;
				if (e2 == null)
					System.err.println(e1);
				else
					Engine.engine.amsg(e1); 
			}
		}
	}

	public void update() throws SAXException, ParserConfigurationException {
		if (!YouTube.sync.isEmpty()) {
			for (Channel a : YouTube.sync)
				a.start();
			YouTube.sync.clear();
		}
		try {
		System.err.println(this.user);
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		DocumentBuilder a = f.newDocumentBuilder();
		Document doc = a.newDocument();
		String duration, title, videoID;
		duration = title = videoID = "";
		doc = a.parse("http://gdata.youtube.com/feeds/api/users/"+this.channelName+"/uploads");
		Element e = doc.getDocumentElement();
		e.normalize();
		NodeList n4 = e.getElementsByTagName("author");
		String author = n4.item(0).getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
		if (!author.toLowerCase().equals(this.user.toLowerCase()))
			this.user = author;
		NodeList n3 = e.getElementsByTagName("media:group");
		Node node = n3.item(0);
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

		if (!cache.getProperty("lastID").equals(videoID) && !uploads.containsKey(videoID)) {
			if (uploads.size() == 5)
				uploads.remove(uploads.keySet().iterator().next());
			uploads.put(videoID, new Upload(title, duration, videoID));
			cache.put("lastID", videoID);
			YouTube.cache.addVideo(this.user, title, videoID);
			announce(title, duration, videoID);
			saveCache();
		}
		}
		catch (IOException e) {
			String e1 = "The following error occured while updating "+this.user+": "+e.toString()+": "+e.getStackTrace()[0];
			Engine e2 = Engine.engine;
			if (e2 == null)
				System.err.println(e1);
			else
				Engine.engine.amsg(e1); 
		}



	}

	public void saveCache() throws FileNotFoundException, IOException {
		File a = new File(Engine.YouTubeVIDCache, this.channelName+".iuc");
		cache.store(new FileOutputStream(a), "YouTube Upload Cache for "+this.channelName);
		if (!uploads.isEmpty())
		{
			File b = new File(Engine.ChannelUploadsCache, this.channelName+".iuc");
			if (b.exists())
				b.delete();
			BufferedWriter c = new BufferedWriter(new FileWriter(b));
			for (String d : uploads.keySet()) {
				Upload e = uploads.get(d);
				c.write("T: "+e.title+"\n");
				c.write("L: "+e.length+"\n");
				c.write("I: "+e.VID+"\n");
			}
			c.close();

		}
	}

	public Map<String, Upload> loadCache() throws IOException {
		File a = new File(Engine.ChannelUploadsCache, this.channelName+".iuc");
		//System.err.println(a.getAbsolutePath());
		if (!a.exists())
			return new HashMap<String, Upload>();
		BufferedReader b = new BufferedReader(new FileReader(a));
		String line = null;
		String title = null;
		String length = null;
		String VID = null;
		Map<String, Upload> c = new HashMap<String, Upload>();
		while ((line = b.readLine()) != null) {
			if (line.startsWith("T: "))
				title = line.split("T: ")[1];
			if (line.startsWith("I: ")) {
				length = line.split("I: ")[1];
				c.put(VID, new Upload(title, length, VID));
			}
			if (line.startsWith("L: "))
				VID = line.split("L: ")[1];

		}
		b.close();
		a.delete();
		return c;
	}

	private void announce(String title, String duration, String videoID) throws IOException {
		Engine e = Engine.engine;
		char dash = 8212;
		int time = Integer.parseInt(duration);
		int minutes = time / 60;
		int seconds = time % 60;
		int hours = 0;
		while (minutes >= 60) {
			hours++;
			minutes -= 60;
		}
		duration = (hours > 0 ? (hours < 10 ? "0"+hours : hours)+":" : "")+(minutes < 10 ? "0"+minutes : minutes)+":"+(seconds < 10 ? "0"+seconds : seconds);
		String o = c2(this.user)+c1(" uploaded a video: ")+c2(title)+c1(" [")+c2(duration)+c1("] "+dash)+c2(" http://youtu.be/"+videoID);
		if (e == null)
			System.out.println(o.replaceAll(Engine.colour+"[0-9]{1,2}", ""));
		else
			e.amsg(o);
	}

	private String c1(String s) {
		return Engine.colour+"14"+s;
	}

	private String c2(String s) {
		return Engine.colour+"13"+s;
	}

	public String getName() {
		return this.user;
	}

	public String getChannelName() {
		return this.channelName;
	}

}
