package com.ipeer.iutil.engine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

public class MCStatus {

	public MCStatus(String channel, Engine engine) {
		boolean login, auth, session, account, minecraft, skins;
		login = auth = session = account = minecraft = skins = true;	
		int logini, authi, sessioni, accounti, minecrafti, skinsi;
		
		logini = Utils.getResponseCode("https://login.minecraft.net");
		authi = Utils.getResponseCode("https://auth.mojang.com");
		sessioni = Utils.getResponseCode("https://session.minecraft.net");
		accounti = Utils.getResponseCode("https://account.mojang.com");
		minecrafti = Utils.getResponseCode("http://minecraft.net");
		skinsi = Utils.getResponseCode("http://skins.minecraft.net");
		
		
		login = logini == HttpURLConnection.HTTP_OK;
		auth = authi == HttpURLConnection.HTTP_OK;
		session = sessioni == HttpURLConnection.HTTP_OK;
		account = accounti == HttpURLConnection.HTTP_OK;
		minecraft = minecrafti == HttpURLConnection.HTTP_OK;
		skins = skinsi == HttpURLConnection.HTTP_OK;
		
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
		return Engine.colour+(t ? "03" : "04")+s+(c == HttpURLConnection.HTTP_OK ? "" : " ("+Utils.getErrorName(c)+")");
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
