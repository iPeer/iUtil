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

	private int replyLimit = 2;
	private int currentReply = 0;
	List<String> outList;

	public Twitter() {
		outList = new ArrayList<String>();
	}

	public void getTweetInfo(Engine engine, int i, String channel, String statusID) throws IOException {
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		String tweetText = "";
		String inReplyToID = "";
		String fromUser = "";
		String inReplyToUser = "";
		DocumentBuilder a;
		try {


			a = f.newDocumentBuilder();
			Document doc = a.newDocument();
			doc = a.parse("https://api.twitter.com/1/statuses/show.xml?id="+statusID);
			NodeList n = doc.getElementsByTagName("text").item(0).getChildNodes();
			tweetText = n.item(0).getNodeValue().replaceAll("&amp;", "&").replaceAll("\n", " ");
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
			if (!inReplyToID.equals("") || currentReply > 0) {
				currentReply++;
				String b = "@"+fromUser+": "+tweetText;
				outList.add(b);
				if (currentReply == replyLimit) {
					for (int x = outList.size() - 1; x > -1; x--) {
						if (channel != null)
							engine.send("PRIVMSG "+channel+" :"+(x == (outList.size() - 1) ? Engine.italics : "")+outList.get(x));
						else
							System.err.println((x == (outList.size() - 1) ? Engine.italics : "")+outList.get(x));
					}
				}
				else
					this.getTweetInfo(engine, 1, channel, inReplyToID);

			}
			else {
				if (channel != null) {
					String b = "@"+fromUser+": "+tweetText;
					engine.send("PRIVMSG "+channel+" :"+b);
				}
				else
					System.err.println("@"+fromUser+": "+tweetText);
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
			Twitter t = new Twitter();
			t.getTweetInfo(null, 1, null, "252060042088550400");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
