package com.ipeer.iutil.engine;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.ipeer.iutil.youtube.Upload;

public class GetLatestVideo {


	public static void main(String[] args) throws IOException {
		lookupLatestVideo(Engine.engine, 0, "#peer.dev", "EthosLab");
	}

	public static void lookupLatestVideo(Engine engine, int outType, String channel, String user) throws IOException {
		boolean pub = outType == 1;
		try {
			DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
			DocumentBuilder a = f.newDocumentBuilder();
			Document doc = a.newDocument();
			doc = a.parse("http://gdata.youtube.com/feeds/api/users/"+user+"/uploads");
			Element e = doc.getDocumentElement();
			NodeList n1 = e.getElementsByTagName("media:player");
			NamedNodeMap nn1 = n1.item(0).getAttributes();
			String postoutput = "";
			for (int x1 = 0; x1 < nn1.getLength(); x1++) {
				String nodeName = nn1.item(x1).getNodeName();
				if 	(nodeName.equals("url"))
					postoutput = nn1.item(x1).getNodeValue().split("\\?v=")[1].split("&feature")[0];
			}
			VideoInfo.getVideoInfo(engine, outType, channel, postoutput);
//			if (Engine.YouTube.channels.containsKey(user)) {
//				char dash = 8212;
//				Map<String, Upload> b = Engine.YouTube.channels.get(user).uploads;
//				if (b.size() < 2)
//					return;
//				b.remove(postoutput);
//				engine.send((pub ? "PRIVMSG " : "NOTICE ")+channel+" :"+c1("Previous videos from ")+c2(user)+c1(":"));
//				for (String c : b.keySet()) {
//					Upload d = b.get(c);
//					engine.send((pub ? "PRIVMSG " : "NOTICE ")+channel+" :    "+c2(d.title)+c1(" [")+c2(Engine.YouTube.formatDuration(d.length))+c1("] "+dash+" ")+c2("http://youtu.be/"+d.VID));
//				}
//			}
		}
		catch (NullPointerException np) {
			
			engine.send((pub ? "PRIVMSG " : "NOTICE ")+channel+" :Oops! It doesn't look like "+user+" has a latest video...");
			np.printStackTrace();
		} catch (Exception e) {
			engine.send((pub ? "PRIVMSG " : "NOTICE ")+channel+" :Oh dear! It appears something went wrong. Here's a little more information: "+e.toString()+" @ "+e.getStackTrace()[0]);
			e.printStackTrace();
		} 
	}

	private static String c1(String s) {
		return Engine.colour+"14"+s;
	}

	private static String c2(String s) {
		return Engine.colour+"13"+s;
	}
	
}
