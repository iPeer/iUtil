package com.ipeer.iutil.engine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class Twitter {

	public static void getTweetInfo(Engine engine, int i, String channel, String statusID) throws IOException {
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		String tweetText = "";
		String inReplyToID = "";
		String fromUser = "";
		String inReplyToUser = "";
		List<String> outList = new ArrayList<String>();
		DocumentBuilder a;
		try {


			a = f.newDocumentBuilder();
			Document doc = a.newDocument();
			doc = a.parse("https://api.twitter.com/1/statuses/show.xml?id="+statusID);
			NodeList n = doc.getElementsByTagName("text").item(0).getChildNodes();
			tweetText = n.item(0).getNodeValue().replaceAll("&amp;", "&");
			n = doc.getElementsByTagName("screen_name").item(0).getChildNodes();
			fromUser = n.item(0).getNodeValue();
			n = doc.getElementsByTagName("in_reply_to_status_id").item(0).getChildNodes();
			try {
				inReplyToID = n.item(0).getNodeValue();
			}
			catch (NullPointerException ne) {
				inReplyToID = "";
			}

			String out = "";
			outList.add(out);
			if (!inReplyToID.equals("")) {
				char dash = 6;
				inReplyToUser = doc.getElementsByTagName("in_reply_to_screen_name").item(0).getChildNodes().item(0).getNodeValue();
				out = "@"+fromUser+": "+tweetText+" "+dash+" In reply to: https://twitter.com/"+inReplyToUser+"/status/"+inReplyToID;
			}
			else {
				out = "@"+fromUser+": "+tweetText;
			}
			if (channel != null) { 
				engine.send("PRIVMSG "+channel+" :"+out);
			}

		} 
		catch (FileNotFoundException n) {
			engine.send("PRIVMSG "+channel+" :No tweet with ID '"+statusID+"'.");
		}
		catch (Exception e) {
			e.printStackTrace();
			engine.send("PRIVMSG "+channel+" :Unable to retrieve tweet info: "+e.toString());
		}
	}

	public static void main(String[] args) {
		try {
			getTweetInfo(null, 1, null, "197420774984925184");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
