package com.ipeer.iutil.engine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;

public class MCStatus {

	public MCStatus(String channel, Engine engine) {
		boolean login, auth, session, account, minecraft, skins;
		login = auth = session = account = minecraft = skins = true;	
		int logini, authi, sessioni, accounti, minecrafti, skinsi;
		
		logini = Utils.getMinecraftLoginResponseCode("https://login.minecraft.net/");
		authi = Utils.getResponseCode("https://auth.mojang.com/game/");
		sessioni = Utils.getResponseCode("https://session.minecraft.net/");
		accounti = Utils.getResponseCode("https://account.mojang.com/");
		minecrafti = Utils.getResponseCode("http://minecraft.net/");
		skinsi = Utils.getResponseCode("http://skins.minecraft.net/");
		
		
		login = Arrays.asList(200, 404).contains(logini);
		auth = Arrays.asList(200, 404).contains(authi);
		session = Arrays.asList(200, 404).contains(sessioni);
		account = Arrays.asList(200, 404).contains(accounti);
		minecraft = Arrays.asList(200, 404).contains(minecrafti);
		skins = Arrays.asList(200, 404).contains(skinsi);
		
		String outString = c1("Auth", auth, authi)+c1(", ")+c1("Account", account, accounti)+c1(", ")+c1("Login", login, logini)+c1(", ")+c1("Session", session, sessioni)+c1(", ")+c1("Website", minecraft, minecrafti)+c1(", ")+c1("Skins", skins, skinsi);
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

	
	private String c1(String s, boolean t, int c) {
		return Engine.colour+(t ? "03" : "04")+s+(c == HttpURLConnection.HTTP_OK || c == HttpURLConnection.HTTP_NOT_FOUND ? "" : " ("+Utils.getErrorName(c)+")");
	}


	@SuppressWarnings("unused")
	private String c1(String s, boolean t) {
		return Engine.colour+(t ? "03" : "04")+s;
	}
	private String c1(String s) {
		return Engine.colour+"14"+s;
	}
	
	public static void main(String[] args) {
		new MCStatus(null, null);
	}

}
