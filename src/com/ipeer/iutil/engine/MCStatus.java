package com.ipeer.iutil.engine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import com.ipeer.iutil.json.EmptyJSONFileException;
import com.ipeer.iutil.json.JSONReader;

public class MCStatus {

	public MCStatus(String channel, Engine engine) throws MalformedURLException, EmptyJSONFileException, IOException {
		boolean login, auth, session, account, minecraft, skins;
		login = auth = session = account = minecraft = skins = true;

		JSONReader a = new JSONReader("https://status.mojang.com/check");
		
		login = a.get("login.minecraft.net").equals("green");
		minecraft = a.get("minecraft.net").equals("green");
		auth = a.get("auth.mojang.com").equals("green");
		session = a.get("session.minecraft.net").equals("green");
		skins = a.get("skins.minecraft.net").equals("green");
		account = a.get("account.mojang.com").equals("green");
		
		String outString = c1("Auth", auth)+c1(", ")+c1("Account", account)+c1(", ")+c1("Login", login)+c1(", ")+c1("Session", session)+c1(", ")+c1("Website", minecraft)+c1(", ")+c1("Skins", skins);
		try {
			if (engine != null)
				engine.send("PRIVMSG "+channel+" :"+outString);
			else
				System.out.println(outString);
		} catch (IOException e) {
			System.err.println("Couldn't send MC Status!");
			e.printStackTrace();
		}
		
	}


	private String c1(String s, boolean t) {
		return Engine.colour+(t ? "03" : "04")+s;
	}
	private String c1(String s) {
		return Engine.colour+"14"+s;
	}
	
	public static void main(String[] args) throws MalformedURLException, EmptyJSONFileException, IOException {
		new MCStatus(null, null);
	}

}
