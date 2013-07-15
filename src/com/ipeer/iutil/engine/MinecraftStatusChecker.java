package com.ipeer.iutil.engine;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class MinecraftStatusChecker implements Runnable {

	private String password;
	private String username;
	private Thread thread;
	private boolean IS_RUNNING = false;
	private HashMap<String, Long> times;
	private HashMap<String, String> statuses;
	private boolean firstCheck = true;
	private String sessionID, realIGN;

	public static void main(String[] args) {
		MinecraftStatusChecker a = new MinecraftStatusChecker();
		a.start();
	}

	public void start() {
		IS_RUNNING = true;
		(thread = new Thread(this, "Minecraft Service Status Checker")).start();
	}

	public void stop() {
		IS_RUNNING = false;
		thread.interrupt();
	}

	public void run() {
		String[] logind = readLogin();
		this.username = logind[0];
		this.password = logind[1];
		times = new HashMap<String, Long>();
		statuses = new HashMap<String, String>();
		while (IS_RUNNING && !Thread.interrupted()) {
			try {
				HashMap<String, String> skins = getResponseCode("http://skins.minecraft.net/MinecraftSkins/iPeer.png", "GET");
				skins.put("online", (skins.get("status").equals("HTTP 200") ? "up" : "down"));
				HashMap<String, String> minecraft = getResponseCode("https://minecraft.net");
				minecraft.put("online", (minecraft.get("status").equals("HTTP 200") ? "up" : "down"));
				HashMap<String, String> account = getResponseCode("https://account.mojang.com", "GET");
				account.put("online", (account.get("status").equals("HTTP 200") ? "up" : "down"));
				//				HashMap<String, String> auth = getMinecraftLoginResponseCode("https://auth.mojang.com");
				//				auth.put("online", (auth.get("status").equals("HTTP 404") || auth.get("status").equals("HTTP 400") || auth.get("status").equals("HTTP 200") ? "up" : "down"));
				HashMap<String, String> session = getResponseCode("https://session.minecraft.net/game/checkserver.jsp");
				session.put("online", (session.get("status").equals("HTTP 200") ? "up" : "down"));
				HashMap<String, String> login = getMinecraftLoginResponseCode("https://login.minecraft.net");
				login.put("online", (login.get("status").equals("HTTP 200") ? "up" : "down"));
				HashMap<String, String> yggAuth = getResponseCode("https://authserver.mojang.com");
				yggAuth.put("online", (login.get("status").equals("HTTP 200") ? "up" : "down"));
				//HashMap<String, String> realms = getMinecraftRealmsResponseCode("https://mcoapi.minecraft.net/mco/available");
				//realms.put("online", (realms.get("status").equals("HTTP 200") ? "up" : "down"));
				
				HashMap<String, HashMap<String, String>> a = new HashMap<String, HashMap<String, String>>();				
				a.put("Skins", skins);
				a.put("Website", minecraft);
				a.put("Accounts", account);
				//a.add(auth);
				a.put("Session", session);
				a.put("Login", login);
				a.put("Yggdrasil Auth", yggAuth);
				//a.put("Realms", realms);

				Iterator<Entry<String, HashMap<String, String>>> it = a.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, HashMap<String, String>> da = it.next();
					String k = da.getKey();
					//					if ((!statuses.containsKey(k) 
					//							|| !times.containsKey(k)) 
					//							|| (((!statuses.get(k).equals(da.getValue().get("status")) && System.currentTimeMillis() - times.get(k) >= 120000) 
					//							|| (statuses.get(k).equals(da.getValue().get("status")) && System.currentTimeMillis() - times.get(k) >= 900000)))
					//
					//System.err.println(k+", "+statuses.get(k));
					if (!statuses.containsKey(k) || !times.containsKey(k) || (!statuses.get(k).equals(da.getValue().get("status")) && System.currentTimeMillis() - times.get(k) >= 60000))
						announceStatus(k, da.getValue());
				}

				if (firstCheck) {
					firstCheck = false;	
				}

				writeDataToFile(a);

				Thread.sleep(60000);

			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void announceStatus(String k, HashMap<String, String> value) {
		Engine e = Engine.engine;
		if (e != null && !e.announceMCStatus)
			return;
		boolean reAnnounce = (times.get(k) != null && System.currentTimeMillis() - times.get(k) >= 900000 && (statuses.get(k) != null && !statuses.get(k).equals("HTTP 200")));
		times.put(k, System.currentTimeMillis());
		if (statuses.get(k) == null)
			statuses.put(k, "HTTP 200");
		//System.err.println(statuses.get(k)+", "+value.get("status"));
		if (reAnnounce || !value.get("status").equals(statuses.get(k))) {
			statuses.put(k,	value.get("status"));
			if (e == null)
				System.err.println(k+" "+(value.get("status").equals("HTTP 200") ? "is back online!" : (reAnnounce ? "is still down. (" : "is reporting downtime! ("+value.get("status")+")")));
			else
				e.amsg(prefix()+c2(k)+c1(" "+(value.get("status").equals("HTTP 200") ? "is back online!" : (reAnnounce ? "is still down. (" : "is reporting downtime! (")+c2(value.get("status"))+c1(")"))));
		}
	}

	private String c1(String s) {
		return Engine.colour+"14"+s;
	}

	private String c2(String s) {
		return Engine.colour+"13"+s;
	}

	private String prefix() {
		return c1("[")+c2("Minecraft Status")+c1("]: ");
	}

	private void writeDataToFile(HashMap<String, HashMap<String, String>> a) throws IOException {
		File c = new File("MinecraftStatus.txt");
		FileWriter d = new FileWriter(c);
		Iterator<Entry<String, HashMap<String, String>>> it = a.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, HashMap<String, String>> da = it.next();
			HashMap<String, String> map = da.getValue();
			d.write(map.get("server")+"\01"+map.get("ping")+"\01"+map.get("online")+"\01"+map.get("status")+"\n");
		}
		d.close();
	}

	@SuppressWarnings("unused")
	private void writeDataToFile(List<HashMap<String, String>> a) throws IOException {
		File c = new File("MinecraftStatus.txt");
		FileWriter d = new FileWriter(c);
		for (HashMap<String, String> b : a)
			d.write(b.get("server")+"\01"+b.get("ping")+"\01"+b.get("online")+"\01"+b.get("status")+"\n");
		d.close();
	}

	public static HashMap<String, String> getResponseCode(String url) {
		return getResponseCode(url, "HEAD");
	}

	public HashMap<String, String> getMinecraftRealmsResponseCode(String url) {
		if (!url.startsWith("http://") && !url.startsWith("https://"))
			url = "https://"+url;
		HttpsURLConnection a;
		HashMap<String, String> b = new HashMap<String, String>();
		b.put("server", url.split("/")[2]);
		b.put("ping", Long.toString(0L));
		long ping1 = System.currentTimeMillis();
		try {
			a = (HttpsURLConnection)new URL(url).openConnection();
			a.setRequestMethod("GET");
			//System.err.println(this.sessionID);
			System.err.println(this.sessionID+" / "+this.realIGN);
			a.setRequestProperty("Cookie", "sid="+this.sessionID+";user="+this.realIGN);
			a.setRequestProperty("Cache-Control", "no-cache");
			a.setRequestProperty("Pragma", "no-cache");
			a.setRequestProperty("User-Agent", "Java/"+System.getProperty("java.version"));
			a.setRequestProperty("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
			a.setRequestProperty("Connection", "keep-alive");
			a.setUseCaches(false);
			a.setDoInput(true);
			a.setDoOutput(true);
			a.setReadTimeout(5000);
			a.setConnectTimeout(5000);
			a.connect();
/*			DataOutputStream wr = new DataOutputStream(a.getOutputStream());
			wr.writeBytes(params);
			wr.flush();
			wr.close();*/
			b.put("status", getErrorName(a.getResponseCode(), null));
		}
		catch (UnknownHostException e) {
			b.put("status", getErrorName(-2, e));
		}
		catch (SocketTimeoutException e) {
			b.put("status", getErrorName(-1, e));
		}
		catch (ConnectException e) {
			b.put("status", getErrorName(-3, e));
		}
		catch (Exception e) {
			b.put("status", getErrorName(0, e));
		}
		b.put("ping", Long.toString((System.currentTimeMillis() - ping1)));
		return b;
	}
	
	public HashMap<String, String> getMinecraftLoginResponseCode(String url) {
		if (!url.startsWith("http://") && !url.startsWith("https://"))
			url = "https://"+url;
		HttpsURLConnection a = null;
		HashMap<String, String> b = new HashMap<String, String>();
		b.put("server", url.split("/")[2]);
		b.put("ping", Long.toString(0L));
		long ping1 = System.currentTimeMillis();
		try {
			a = (HttpsURLConnection)new URL(url).openConnection();
			String params = "user="+this.username+"&password="+this.password+"&version=13";
			if (url.contains("auth.mojang.com"))
				params = params+"&authenticityToken=0";
			a.setRequestMethod("POST");
			a.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			a.setRequestProperty("Content-Length", Integer.toString(params.getBytes().length));
			a.setRequestProperty("Content-Language", "en-US");
			a.setUseCaches(false);
			a.setDoInput(true);
			a.setDoOutput(true);
			a.setReadTimeout(5000);
			a.setConnectTimeout(5000);
			a.connect();
			DataOutputStream wr = new DataOutputStream(a.getOutputStream());
			wr.writeBytes(params);
			wr.flush();
			BufferedReader in = new BufferedReader(new InputStreamReader(a.getInputStream()));
			String line = in.readLine();
			//System.err.println(line);
			this.sessionID = line.split(":")[3];
			this.realIGN = line.split(":")[2];
			//System.err.println(this.sessionID+", "+line);
			in.close();
			wr.close();
			b.put("status", getErrorName(a.getResponseCode(), null));
		}
//		catch (IOException e) {
//			b.put("status", getErrorName(a.getResponseCode(), null));
//		}
		catch (UnknownHostException e) {
			b.put("status", getErrorName(-2, e));
		}
		catch (SocketTimeoutException e) {
			b.put("status", getErrorName(-1, e));
		}
		catch (ConnectException e) {
			b.put("status", getErrorName(-3, e));
		}
		catch (ArrayIndexOutOfBoundsException e) {
			b.put("status", getErrorName(-4, e));
		}
		catch (SocketException e) {
			b.put("status", getErrorName(-5, e));
		}
		catch (Exception e) {
			try {
			b.put("status", getErrorName(a.getResponseCode(), e));
			} catch (Exception e1) { }
		}
		b.put("ping", Long.toString((System.currentTimeMillis() - ping1)));
		return b;
	}

	public static int getHttpsResponseCode(String url, String method) {
		if (!url.startsWith("http://") && !url.startsWith("https://"))
			url = "https://"+url;
		HttpsURLConnection a;
		try {
			a = (HttpsURLConnection)new URL(url).openConnection();
			a.setRequestMethod(method);
			a.setConnectTimeout(3000);
			a.setReadTimeout(3000);
			return a.getResponseCode();
		}
		catch (UnknownHostException e) {
			return -2;
		}
		catch (SocketTimeoutException e) {
			return -1;
		}
		catch (ConnectException e) {
			return -3;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static HashMap<String, String> getResponseCode(String url, String method) {
		if (!url.startsWith("http://") && !url.startsWith("https://"))
			url = "http://"+url;
		HttpURLConnection a = null;
		HashMap<String, String> b = new HashMap<String, String>();
		b.put("server", url.split("/")[2]);
		b.put("ping", Long.toString(0L));
		long ping1 = System.currentTimeMillis();
		try {
		    SSLContext sc = SSLContext.getInstance("SSL"); 
		    sc.init(null, SSLUtils.trustAll, new java.security.SecureRandom()); 
		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} 
		catch (GeneralSecurityException e) { 
			b.put("status", getErrorName(0, e)); 
			return b;
		} 
		try {
			if (url.startsWith("https"))
				a = (HttpsURLConnection)new URL(url).openConnection();
			else
				a = (HttpURLConnection)new URL(url).openConnection();
			a.setRequestMethod(method);
			a.setConnectTimeout(5000);
			a.setReadTimeout(5000);
			//System.err.println(a.getResponseCode());
			b.put("status", getErrorName(a.getResponseCode(), null));
		}
		catch (UnknownHostException e) {
			b.put("status", getErrorName(-2, e));
		}
		catch (SocketTimeoutException e) {
			b.put("status", getErrorName(-1, e));
		}
		catch (ConnectException e) {
			b.put("status", getErrorName(-3, e));
		}
		catch (SocketException e) {
			b.put("status", getErrorName(-5, e));
		}
		catch (Exception e) {
			try {
			b.put("status", getErrorName(a.getResponseCode(), e));
			} catch (Exception e1) { }
		}
		b.put("ping", Long.toString((System.currentTimeMillis() - ping1)));
		return b;
	}

	public static String getErrorName(int code, Exception e) {
		switch (code) {
		case -5:
			return e.getMessage();
		case -4:
			return "Bad response";
		case -3:
			return "Connection refused";
		case -2:
			return "Unknown host";
		case -1:
			return "Timed out";
		case 0:
			e.printStackTrace();
			return "Unknown error / "+e.toString();
		default:
			return "HTTP "+code;
		}
	}

	@SuppressWarnings("unused")
	private void writeLogin(String u, String p) throws IOException {
		DataOutputStream out = new DataOutputStream(new FileOutputStream(new File("MCSSCreds.iaf")));
		KeyGenerator kg;
		Key key;
		Cipher ci;
		try {
			kg = KeyGenerator.getInstance("AES");
			kg.init(128);
			key = kg.generateKey();
			out.writeInt(key.getEncoded().length);
			ci = Cipher.getInstance("AES/ECB/PKCS5Padding");
			ci.init(Cipher.ENCRYPT_MODE, key);
			out.write(key.getEncoded());
			byte[] pass = ci.doFinal(p.getBytes());
			out.writeInt(pass.length);
			out.write(pass);
			byte[] user = ci.doFinal(u.getBytes());
			out.writeInt(user.length);
			out.write(user);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			out.close();
		} 
		out.close();
	}

	private String[] readLogin() {
		Key key;
		String u = "", p = "";
		try {
			Cipher ci = Cipher.getInstance("AES/ECB/PKCS5Padding");
			DataInputStream in = new DataInputStream(new FileInputStream(new File("MCSSCreds.iaf")));
			byte[] ke = new byte[in.readInt()];
			in.readFully(ke);
			key = new SecretKeySpec(ke, "AES");
			ci.init(Cipher.DECRYPT_MODE, key);
			byte[] pass = new byte[in.readInt()];
			in.readFully(pass);
			p = new String(ci.doFinal(pass));
			byte[] user = new byte[in.readInt()];
			in.readFully(user);
			u = new String(ci.doFinal(user));
			in.close();
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IOException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}
		String[] s = {u, p};
		return s;
	}

}
