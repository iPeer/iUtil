package com.ipeer.iutil.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import com.ipeer.iutil.json.EmptyJSONFileException;
import com.ipeer.iutil.json.JSONReader;

public class MCStatus {

	public MCStatus(String channel, Engine engine) throws MalformedURLException, EmptyJSONFileException, IOException {
//		boolean login, auth, session, account, minecraft, skins;
//		login = auth = session = account = minecraft = skins = true;

//		JSONReader a = new JSONReader("https://status.mojang.com/check");
		File file = new File("MinecraftStatus.txt");
		if (!file.exists()) {
			try {
				if (engine != null)
					engine.send("PRIVMSG "+channel+" :No Status Data!");
				else
					System.out.println("No Status Data!");
				return;
			} catch (IOException e) {
				System.err.println("Couldn't send MC Status!");
				e.printStackTrace();
			}
		}
		
		String out = "";
		
		BufferedReader r = new BufferedReader(new FileReader(file));
		String line = "";
		Map<String, String> servers = new HashMap<String, String>();
		servers.put("minecraft.net", "Website");
		servers.put("login.minecraft.net", "Login");
		servers.put("session.minecraft.net", "Session");
		servers.put("skins.minecraft.net", "Skins");
		servers.put("account.mojang.com", "Account");
		while ((line = r.readLine()) != null) {
			String afinal = "";
			String[] data = line.split("\01");
			String serverName = data[0];
			int serverPing = Integer.valueOf(data[1]);
			String serverStatus = data[2];
			String HTTP = data[3];
			if (serverStatus.equals("up"))
				afinal = Engine.colour+(serverPing > 1500 ? "07" : "03")+servers.get(serverName)+" ("+serverPing+"ms)";
			else
				afinal = Engine.colour+"04"+servers.get(serverName)+" ("+HTTP+")";
			out = out+(out.length() > 0 ? Engine.colour+"14, " : "")+afinal;
				
		}
//		
//		
//		login = a.get("login.minecraft.net").equals("green");
//		minecraft = a.get("minecraft.net").equals("green");
//		auth = a.get("auth.mojang.com").equals("green");
//		session = a.get("session.minecraft.net").equals("green");
//		skins = a.get("skins.minecraft.net").equals("green");
//		account = a.get("account.mojang.com").equals("green");
//		
//		String outString = c1("Auth", auth)+c1(", ")+c1("Account", account)+c1(", ")+c1("Login", login)+c1(", ")+c1("Session", session)+c1(", ")+c1("Website", minecraft)+c1(", ")+c1("Skins", skins);
		try {
			if (engine != null)
				engine.send("PRIVMSG "+channel+" :"+out);
			else
				System.out.println(out);
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
