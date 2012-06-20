package com.ipeer.iutil.engine;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class GetLatestVideo {


	public static void main(String[] args) throws IOException {
		lookupLatestVideo(Engine.engine, 0, "#peer.dev", "EthosLab");
	}

	public static void lookupLatestVideo(Engine engine, int outType, String channel, String user) throws IOException {
		try {
			DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
			DocumentBuilder a = f.newDocumentBuilder();
			Document doc = a.newDocument();
			doc = a.parse("http://gdata.youtube.com/feeds/api/users/"+user+"/uploads");
			Element e = doc.getDocumentElement();
			NodeList n = e.getElementsByTagName("media:content");
			NamedNodeMap n2 = n.item(0).getAttributes();
			String data = n2.item(5).getNodeValue();
			String postoutput = data.split("/")[4];
			VideoInfo.getVideoInfo(engine, outType, channel, postoutput.substring(0, postoutput.indexOf("?")));
		}
		catch (Exception e) {
			engine.send("PRIVMSG "+channel+" :SEVERE: "+e.toString()+" @ "+e.getStackTrace()[0]);
		}
	}

}
