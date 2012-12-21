package com.ipeer.iutil.engine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

public class MinecraftStatusChecker implements Runnable {

	private String password;
	private String username;
	private Thread thread;
	private boolean IS_RUNNING = false;
	
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
		while (IS_RUNNING && !Thread.interrupted()) {
			try {
				HashMap<String, String> skins = getResponseCode("http://skins.minecraft.net/MinecraftSkins/iPeer.png", "GET");
				skins.put("online", (skins.get("status").equals("HTTP 200") ? "up" : "down"));
				HashMap<String, String> minecraft = getResponseCode("http://minecraft.net");
				minecraft.put("online", (minecraft.get("status").equals("HTTP 200") ? "up" : "down"));
				HashMap<String, String> account = getResponseCode("https://account.mojang.com");
				account.put("online", (account.get("status").equals("HTTP 200") ? "up" : "down"));
//				HashMap<String, String> auth = getMinecraftLoginResponseCode("https://auth.mojang.com");
//				auth.put("online", (auth.get("status").equals("HTTP 404") || auth.get("status").equals("HTTP 400") || auth.get("status").equals("HTTP 200") ? "up" : "down"));
				HashMap<String, String> session = getResponseCode("https://session.minecraft.net/game/checkserver.jsp");
				session.put("online", (session.get("status").equals("HTTP 200") ? "up" : "down"));
				HashMap<String, String> login = getMinecraftLoginResponseCode("https://login.minecraft.net");
				login.put("online", (login.get("status").equals("HTTP 200") ? "up" : "down"));
				
				List<HashMap<String, String>> a = new ArrayList<HashMap<String, String>>();				
				a.add(skins);
				a.add(minecraft);
				a.add(account);
				//a.add(auth);
				a.add(session);
				a.add(login);
				
				writeDataToFile(a);
				
				Thread.sleep(60000);
				
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
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

	public HashMap<String, String> getMinecraftLoginResponseCode(String url) {
		if (!url.startsWith("http://") && !url.startsWith("https://"))
			url = "https://"+url;
		HttpsURLConnection a;
		HashMap<String, String> b = new HashMap<String, String>();
		b.put("server", url.split("/")[2]);
		b.put("ping", Long.toString(0L));
		long ping1 = System.currentTimeMillis();
		try {
			a = (HttpsURLConnection)new URL(url).openConnection();
			String params = "user="+this.username+"&password="+this.password;
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
			wr.close();
			b.put("status", getErrorName(a.getResponseCode()));
		}
		catch (UnknownHostException e) {
			b.put("status", getErrorName(-2));
		}
		catch (SocketTimeoutException e) {
			b.put("status", getErrorName(-1));
		}
		catch (ConnectException e) {
			b.put("status", getErrorName(-3));
		}
		catch (Exception e) {
			e.printStackTrace();
			b.put("status", getErrorName(0));
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
		HttpURLConnection a;
		HashMap<String, String> b = new HashMap<String, String>();
		b.put("server", url.split("/")[2]);
		b.put("ping", Long.toString(0L));
		long ping1 = System.currentTimeMillis();
		try {
			a = (HttpURLConnection)new URL(url).openConnection();
			a.setRequestMethod(method);
			a.setConnectTimeout(5000);
			a.setReadTimeout(5000);
			b.put("status", getErrorName(a.getResponseCode()));
		}
		catch (UnknownHostException e) {
			b.put("status", getErrorName(-2));
		}
		catch (SocketTimeoutException e) {
			b.put("status", getErrorName(-1));
		}
		catch (ConnectException e) {
			b.put("status", getErrorName(-3));
		}
		catch (Exception e) {
			e.printStackTrace();
			b.put("status", getErrorName(0));
		}
		b.put("ping", Long.toString((System.currentTimeMillis() - ping1)));
		return b;
	}

	public static String getErrorName(int code) {
		switch (code) {
		case -3:
			return "Connection refused";
		case -2:
			return "Unknown host";
		case -1:
			return "Timed out";
		case 0:
			return "Unknown error";
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
			Cipher ci = Cipher.getInstance("AES/ECB/PKCS5Padding");;
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
