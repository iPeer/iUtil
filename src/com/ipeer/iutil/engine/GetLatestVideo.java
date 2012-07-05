package com.ipeer.iutil.engine;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
		}
		catch (NullPointerException np) {
			
			engine.send((pub ? "PRIVMSG " : "NOTICE ")+channel+" :Oops! It doesn't look like "+user+" has a latest video...");
			np.printStackTrace();
		} catch (Exception e) {
			engine.send((pub ? "PRIVMSG " : "NOTICE ")+channel+" :Oh dear! It appears something went wrong. Here's a little more information: "+e.toString()+" @ "+e.getStackTrace()[0]);
			e.printStackTrace();
		} 
	}

}
