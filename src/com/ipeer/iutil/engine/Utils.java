package com.ipeer.iutil.engine;

import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;


public class Utils {

	protected Engine engine;

	public Utils(Engine engine2) {
		this.engine = engine2;
	}

	public boolean addressesEqual(Channel c, String n) {
		String mynick = engine.MY_NICK;
		String a1 = c.getUserList().get(n).getAddress();
		String a2 = c.getUserList().get(mynick).getAddress();
		return a1.equals(a2);
	}

	public boolean isAdmin(Channel c, String n) {
		try {
			return c.getUserList().get(n).isOp() || addressesEqual(c, n);
		}
		catch (NullPointerException np) {
			return false;
		}
	}

	public boolean addressesEqual(Channel c, String n, String n2) {
		try {
			String a1 = c.getUserList().get(n).getAddress();
			String a2 = c.getUserList().get(n2).getAddress();
			return a1.equals(a2);
		}
		catch (NullPointerException n1) {
			return false;
		}
	}

	public static String getURL(String url) {
		Pattern url1 = Pattern.compile("((https?://)?.*([^[/$]])?)", Pattern.CASE_INSENSITIVE);
		Matcher m = url1.matcher(url);
		while (m.find()) {
			return m.group();
		}
		return "No match";
	}


	public static int getResponseCode(String url) {
		return getResponseCode(url, "HEAD");
	}

	public static int getMinecraftLoginResponseCode(String url) {
		if (!url.startsWith("http://") && !url.startsWith("https://"))
			url = "https://"+url;
		HttpsURLConnection a;
		try {
			a = (HttpsURLConnection)new URL(url).openConnection();
			String params = "user=notausername";
			a.setRequestMethod("POST");
			a.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			a.setRequestProperty("Content-Length", Integer.toString(params.getBytes().length));
			a.setRequestProperty("Content-Language", "en-US");
			a.setUseCaches(false);
			a.setDoInput(true);
			a.setDoOutput(true);
			a.setReadTimeout(3000);
			a.setConnectTimeout(3000);
			a.connect();
			DataOutputStream wr = new DataOutputStream(a.getOutputStream());
			wr.writeBytes(params);
			wr.flush();
			wr.close();
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

	public static int getResponseCode(String url, String method) {
		if (!url.startsWith("http://") && !url.startsWith("https://"))
			url = "http://"+url;
		HttpURLConnection a;
		HashMap<String, String> b = new HashMap<String, String>();
		b.put("ping", Long.toString(-1L));
		try {
			long ping1 = System.nanoTime();
			a = (HttpURLConnection)new URL(url).openConnection();
			a.setRequestMethod(method);
			a.setConnectTimeout(3000);
			a.setReadTimeout(3000);
			b.put("ping", Long.toString((System.nanoTime() - ping1 / 1000)));
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

	public static boolean checkIfUp(String url) {
		HttpURLConnection a;
		try {
			a = (HttpURLConnection)new URL(url).openConnection();
			a.setRequestMethod("HEAD");
			a.setConnectTimeout(3000);
			a.setReadTimeout(3000);
			return (a.getResponseCode() == HttpURLConnection.HTTP_OK || a.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND);
		}
		catch (Exception e) {
			return false;
		}
	}

	public static boolean bothEqual(String string, String string2, String string3) {
		return string.equals(string3) && string2.equals(string3);
	}

	public static String intArrayToString(int[] array) {
		String out = "{";
		for (int x = 0; x < array.length; x++) 
			out += (out.length() > 1 ? ", " : "")+array[x];
		return out+"}";
	}

}
