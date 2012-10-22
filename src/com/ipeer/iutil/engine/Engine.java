/* 
 * iUtil main Engine file.
 * 
 * These file paths are relative to my computer. 
 * If you want to run this bot, you'll need to change them.
 * 
 * Connection options are supplied via the command line:
 * 	-p=<password>
 * 	-n=<nick> - Defaults to "iUtil"
 * 	-s=<server> - Defaults to "irc.swiftirc.net"
 * 	-ssl=<should I use SSL? (true/false)> - Defaults to false.
 * 	-port=<port to connect to IRC on> - Defaults to 6667
 * 
 */

package com.ipeer.iutil.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Date;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.ipeer.iutil.gui.GuiEngine;
import com.ipeer.iutil.gui.GuiUtils;
import com.ipeer.iutil.shell.Shell;
import com.ipeer.iutil.youtube.YouTube;
import com.ipeer.minecraft.servers.MCServer;

public class Engine implements Runnable {

	public int port = 6667;
	public String server = "";
	public String MY_NICK = "";
	public boolean SSL = false;
	public boolean isRunning = false;
	public String ACTUAL_SERVER = "";
	public final String commandChars = "#@.!";
	public boolean ignoreLock = false;
	public long botStart = 0L;
	public long connectionStart = 0L;
	public boolean isConnected = false;
	public boolean runIdentd = false;
	public boolean runGUI = false;
	public boolean requestedQuit = false;
	public File lock;
	public boolean relayChat = true;

	public static Map<String, Channel> channels = new HashMap<String, Channel>();
	public static Map<String, String> networks = new HashMap<String, String>();
	public static File MCChatCache = new File("F:\\Dropbox\\Java\\iUtil\\caches\\MCChat.iuc");
	public static File YouTubeVIDCache = new File("F:\\Dropbox\\Java\\iUtil\\caches\\ircswiftircnet\\YouTube\\");
	public static File YouTubeUsernameConfig = new File("F:\\Dropbox\\Java\\iUtil\\config\\YouTubeUsernameConfig.cfg");
	public static File UploadsCache = new File("F:\\Dropbox\\Java\\iUtil\\caches\\UploadsCache.iuc");
	public static File ChannelUploadsCache = new File("F:\\Dropbox\\Java\\iUtil\\caches\\ircswiftircnet\\YouTube\\history");
	public static File AWeSomeChatCache = new File("AWeSomeChatCache.iuc");
	public static YouTube YouTube;
	public static MCChat mindcrack/*, AWeSome, AWeSomeCreative*/;
	public static AWeSomeChat ASurvival, ACreative;
	public static GuiEngine gui;
	public static Shell Shell;
	//public static Olympics olympics;

	public static final char colour = 0x03;
	public static final char bold = 0x02;
	public static final char underline = 0x1F;
	public static final char italics = 0x1D;
	public static final char reverse = 0x16;
	public static final char endall = 0xF0;

	public Socket connection;
	protected Utils utils;
	protected IdentdServer identd;
	protected String password = "";
	protected Twitter twitter;
	protected Console console;

	private BufferedWriter out;
	private BufferedReader in;

	private Map<String, String> networkSettings = new HashMap<String, String>();

	private static Writer logWriter;
	public static Engine engine;


	public Engine(String nick, String server, int port, boolean SSL, String password, boolean ignoreLock, boolean runIdentd, boolean reconnecting, boolean runGUI) throws IOException {
		this.MY_NICK = nick;
		this.server = server;
		this.port = port;
		this.SSL = SSL;
		this.password = password;
		this.ignoreLock = ignoreLock;
		this.runGUI = runGUI;
		if (!runGUI)
			System.err.println("Running in NoGUI mode");
		if (runIdentd)
			this.identd = new IdentdServer(this);
		this.runIdentd = runIdentd;
		if (gui == null)
			gui = new GuiEngine(this);
		File lockFile = new File("config");
		if (!lockFile.exists())
			lockFile.mkdirs();
		YouTubeUsernameConfig = new File(lockFile, "YouTubeUsernameConfig.cfg");
		lockFile = new File(lockFile, "lock.lck");
		if (lockFile.exists() && !ignoreLock && !reconnecting) {
			System.err.println("Lock file exists, aborting start up.");
			System.exit(0);
		}
		File logFile = new File("logs/"+this.server.replaceAll("\\.", ""));
		if (!logFile.exists())
			logFile.mkdirs();
		File passwordFile = new File("config/"+this.server.replaceAll("\\.", ""), "password");
		if (passwordFile.exists() && password.equals("")) {
			System.err.println("Reading password...");
			File a = new File("config/"+this.server.replaceAll("\\.", ""));
			File b = new File(a, "key");
			File c = new File(a, "password");
			DataInputStream f = new DataInputStream(new FileInputStream(b));
			int x = f.readInt();
			System.err.println(x);
			byte[] key = new byte[x];
			f.readFully(key);
			Key k = new SecretKeySpec(key, "AES");
			f.close();
			f = new DataInputStream(new FileInputStream(c));
			x = f.readInt();
			System.err.println(x);
			byte[] pass = new byte[x];
			f.readFully(pass);
			f.close();
			try {
				Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
				aes.init(Cipher.DECRYPT_MODE, k);
				this.password = new String(aes.doFinal(pass));
				System.err.println("Password successfully read from file.");
			} catch (InvalidKeyException e) {
				System.err.println("SEVERE: The key found is invalid. Continuing without a password.");
				this.password = "";
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				System.err.println("SEVERE: NoSuchAlgorithmException - Exiting");
				e.printStackTrace();
				System.exit(0);
			} catch (NoSuchPaddingException e) {
				System.err.println("SEVERE: NoSuchPaddingException - Exiting");
				e.printStackTrace();
				System.exit(0);
			} catch (IllegalBlockSizeException e) {
				System.err.println("SEVERE: IllegalBlockSizeException - Exiting");
				e.printStackTrace();
				System.exit(0);
			} catch (BadPaddingException e) {
				System.err.println("SEVERE: BadPaddingException - Exiting");
				e.printStackTrace();
				System.exit(0);
			} 
		}
		logFile = new File(logFile, "/sent.log");		
		logFile.createNewFile();
		File MCChatCache1 = new File("caches/"+this.server.replaceAll("\\.", ""));
		if (!MCChatCache1.exists())
			MCChatCache1.mkdirs();
		UploadsCache = new File(MCChatCache1, "UploadsCache.iuc");
		AWeSomeChatCache = new File(MCChatCache1, "AWeSomeChat.iuc");
		YouTubeVIDCache = new File(MCChatCache1, "YouTube");
		ChannelUploadsCache = new File(YouTubeVIDCache, "history");
		if (!YouTubeVIDCache.exists())
			YouTubeVIDCache.mkdirs();
		if (!ChannelUploadsCache.exists())
			ChannelUploadsCache.mkdirs();
		MCChatCache1 = new File(MCChatCache1, "MCChat.iuc");
		MCChatCache = MCChatCache1;
		if (!MCChatCache.exists())
			MCChatCache.createNewFile();
		logWriter = new OutputStreamWriter(new FileOutputStream(logFile));
		lockFile.createNewFile();
		lockFile.deleteOnExit();
		lock = lockFile;
		utils = new Utils(this);
		writeToLog("-> STARTING UP");
		//twitter = new Twitter(this);
		YouTube = new YouTube(this);
		//olympics = new Olympics(this);
		mindcrack = new MCChat("http://guudelp.com/serverlog.cgi", this);
		//AWeSome = new MCChat("http://auron.co.uk/mc/chat.php", this);
		//AWeSomeCreative = new MCChat("http://auron.co.uk/mc/chat.php?world=creative", this);
		ACreative = new AWeSomeChat(this, "/home/minecraft/servers/creative/server.log");
		ASurvival = new AWeSomeChat(this, "/home/minecraft/servers/survival/server.log");
		console = new Console(this);
		console.start();
		Shell = new Shell();
		engine = this;
	}

	public void start() {
		botStart = System.currentTimeMillis();
		if (this.runIdentd)
			identd.start();
		this.isRunning = true;
		(new Thread(this)).start();
	}

	public void stop() {
		this.isRunning = false;
	}

	@Override
	public void run() {
		try {
			writeToLog("SSL: "+SSL);
			if (SSL) {
				SSLContext ssl = SSLContext.getInstance("SSL");
				ssl.init(null, SSLUtils.trustAll, new SecureRandom());
				SSLSocketFactory sslsf = ssl.getSocketFactory();
				SSLSocket ssls = (SSLSocket)sslsf.createSocket(server, port);
				in = new BufferedReader(new InputStreamReader(ssls.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(ssls.getOutputStream()));
				connection = ssls;
				writeToLog("-> Initializing SSL connection: "+server+":"+port+"/"+ssls.getLocalPort());
			}
			else {
				connection = new Socket(server, port);
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			}
			isConnected = true;
			connectionStart = System.currentTimeMillis();

			send("NICK "+MY_NICK+"\r\n");
			send("USER "+MY_NICK+" ipeer.ipeerftw.co.cc "+MY_NICK+": iPeer's Java Utility Bot\r\n");

			writeToLog("-> Connected. Waiting for server...");

			String line = "";
			while ((line = in.readLine()) != null) {
				System.err.println(line);

				if (line.indexOf("001") >= 0) // Server address
					this.ACTUAL_SERVER = line.split(" ")[0].substring(1);

				else if (line.indexOf("005") >= 0) {	// Network settings				
					String[] a = line.split(" ");
					for (int x = 3; x < a.length; x++) {
						if (a[x].contains("=")) {
							String[] b = a[x].split("=");
							networkSettings.put(b[0], b[1]);
						}
					}
				}

				else if (line.indexOf("004") >= 0) {
					if (!this.password.equals("")) {
						writeToLog("-> Identifying with server...");
						send("PRIVMSG NickServ :identify "+this.password);
					}
					send("MODE "+MY_NICK+" +Bp-x");
				}

				else if (line.indexOf("433") >= 0) {
					writeToLog("-> Nick is in use, trying "+MY_NICK+"2");
					String newNick = MY_NICK+"2";
					MY_NICK = newNick;
					send("NICK :"+newNick);
					send("USER "+newNick+" ipeer.ipeerftw.co.cc "+newNick+": iPeer's Java Utility Bot\r\n");
				}

				else if (line.indexOf("251") >= 0) {
					String network = null;
					if ((network = networkSettings.get("NETWORK")) != null && !networks.containsKey(network))
						networks.put(network, ACTUAL_SERVER);
					else {
						int u = 1;
						while (networks.containsKey("Unknown"+u))
							u++;
						networks.put("Unknown"+u, server);
					}
					break;
				}	
			}

			String autoJoin[] = ("#QuestHelp,#Peer.Dev,#AWeSome").split(",");
			for (String c : autoJoin)
				join(c);
			YouTube.loadChannels();
			mindcrack.start();
			//AWeSome.start();
			//AWeSomeCreative.start();
			ASurvival.start();
			ACreative.start();
			//olympics.start();
			line = "";
			while ((line = in.readLine()) != null) {
				try {
					parseLine(line);
				}
				catch (Exception e) { 
					Calendar d = Calendar.getInstance();
					d.setTime(new Date(System.currentTimeMillis()));
					System.err.println(d.getTime()+":");		
					e.printStackTrace(); 
					YouTube.saveAllCaches();
				}
			}


		}
		catch (Exception e) { 
			Calendar d = Calendar.getInstance();
			d.setTime(new Date(System.currentTimeMillis()));
			System.err.println(d.getTime()+":");
			e.printStackTrace();
			try {
				YouTube.saveAllCaches();
				engine = new Engine(MY_NICK, server, port, SSL, password, ignoreLock, runIdentd, true, runGUI);
				quit("Bot is Reconnecting.");
			} catch (IOException e1) {
				d = Calendar.getInstance();
				d.setTime(new Date(System.currentTimeMillis()));
				System.err.println(d.getTime()+":");	
				e1.printStackTrace();
				System.exit(1);
			} 
		}

	}

	public void parseLine(String l) throws IOException {
		writeToLog("<- "+l);
		if (l.startsWith("PING ")) {
			//			System.err.println(l);
			//			System.err.println("PONG "+l.substring(5));
			send("PONG "+l.substring(5));
		}
		else if (l.startsWith("ERROR :")) {
			System.err.println(l.substring(7));
			writeToLog("-> DISCONNECTED: "+l.substring(7));
			if (!requestedQuit) {
				YouTube.saveAllCaches();
				engine = new Engine(MY_NICK, server, port, SSL, password, ignoreLock, runIdentd, true, runGUI);
				engine.start();
			}
			else {
				if (runGUI)
					gui.gui.addTextHistory(l.substring(7));
				isConnected = false;
				if (!gui.isVisible || !runGUI)
					System.exit(0);
			}
		}

		else if (l.split(" ")[1].equals("MODE")) {
			String[] raw = l.split(" ");
			String channel = raw[2];
			String modes = l.split(channel+" ")[1];
			String nick = l.split("!")[0].substring(1);
			if (runGUI)
				gui.gui.addTextHistory("["+channel+"] "+nick+" set modes: "+modes);
			System.out.println(l);
			for (int x = 4; x < raw.length; x++) {
				if (!raw[x-1].equals(raw[x])) {
					send("WHO +cn "+channel+" "+raw[x]);
				}
			}
		}

		else if (Arrays.asList("PRIVMSG", "NOTICE").contains(l.split(" ")[1])) {
			String type = l.split(" ")[1];
			String nick = l.split("!")[0].substring(1);
			String address = l.split("!")[1].split(" ")[0];
			String target = l.split(" ")[2];
			String[] messageData = l.split(":");
			String message = "";
			for (int x = 2; x < messageData.length; x++) {
				message = message+":"+messageData[x];
			}
			message = message.substring(1);
			String messageOut = (type.equals("NOTICE") ? "<- " : "")+(!target.equals(MY_NICK) ? "["+target+"] " : "")+nick+": "+message;
			System.out.println(messageOut);
			if (runGUI)
				gui.gui.addTextHistory(messageOut);

			if (message.startsWith("")) {			
				String CTCPType = message.replaceAll("", "");
				System.err.println(CTCPType+" from "+nick);
				if (CTCPType.equals("VERSION"))
					send("NOTICE "+nick+" :VERSION B: "+iUtilVersion()+" E: "+EngineVersion()+" | Java: "+System.getProperty("sun.arch.data.model")+"-bit "+System.getProperty("java.version"));
				else if (CTCPType.startsWith("PING"))
					send("NOTICE "+nick+" :PING "+message.substring(6));
				else if (CTCPType.equals("TIME"))
					send("NOTICE "+nick+" :TIME "+new SimpleDateFormat().format(System.currentTimeMillis()));
			}

			else if (message.equals("+voice"))
				send("MODE +v "+nick);

			else if (message.matches(".*https?://(www.)?youtube.com/watch\\?.*") && l.indexOf("video_response_view_all") < 0 && l.indexOf("/playlist") < 0 && !nick.equals("iUtil")) {
				try {
					message = message.replaceAll("https://", "http://");
					String videoID = null;
					int indexOf = message.indexOf("http://www.youtube.com/watch?");
					String newMessage = message.substring(indexOf);
					try {
						videoID = newMessage.split(" ")[0].split("v=")[1].split("&")[0];
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					if (videoID != null)
						try {
							VideoInfo.getVideoInfo(this, 1, target, videoID);
						} catch (Exception e) {
							send("PRIVMSG "+target+" :Error getting video info: "+e.toString());
							e.printStackTrace();
						} 
				}
				catch (Exception e) { }
			}
			else if (message.matches(".*https?://(www.)?youtu.be/.*") && !nick.equals("iUtil")) {
				try {
					message = message.replaceAll("https://", "http://");
					String a = message.split("/")[3].split("&")[0].split(" ")[0];
					//System.out.println(a);
					try {
						VideoInfo.getVideoInfo(this, 1, target, a);
					} catch (Exception e) {
						send("PRIVMSG "+target+" :Error getting video info: "+e.toString());
						e.printStackTrace();
					} 
				}
				catch (Exception e) { }
			}
			else if ((message.contains("https://twitter.com/") || message.contains("http://twitter.com/"))) {
				try {
					twitter = new Twitter();
					String[] data = message.replaceAll("Â ", " ").split("/");
					String statusID = "0";
					if (!data[4].equals("status") && !data[4].equals("statuses")) 
						return;
					if (data[5].equals("status"))
						statusID = data[6].split(" ")[0];
					else
						statusID = data[5].split(" ")[0];
					twitter.getTweetInfo(this, 1, target, statusID);
				}
				catch (Exception e) { }
			}

			if (commandChars.contains(message.substring(0, 1))) {
				parseCommand(l);
			}

		}

		else if (l.split(" ")[1].equals("NICK")) {
			String nick = l.split("!")[0].substring(1);
			String address = l.split("!")[1].split(" ")[0];
			String newNick = l.split(":")[2];
			System.out.println(nick+" is now known as "+newNick);
			for (Channel c : channels.values()) {
				if (c.getUserList().containsKey(nick)) {
					User a = c.getUserList().get(nick);
					User b = new User(a.identd, a.address, a.server, newNick, a.modes, a.realname);
					c.getUserList().remove(nick);
					c.getUserList().put(newNick, b);
				}
			}
		}

		else if (Arrays.asList("JOIN", "PART", "QUIT").contains(l.split(" ")[1])) {
			String type = l.split(" ")[1];
			String nick = l.split("!")[0].substring(1);
			String address = l.split("!")[1].split(" ")[0];
			String target = l.split(" ")[2];
			if (target.startsWith(":"))
				target = target.substring(1);
			System.out.println((!type.equals("QUIT") ? "["+target+"] " : "")+type+": "+nick);
			if (type.equals("JOIN")) {
				send("WHO +cn "+target+" "+nick);
				if (relayChat())
					Shell.relayIRCChat("["+target+"] "+nick+" joined.");
				if (runGUI)
					gui.gui.addTextHistory("["+target+"] "+nick+" joined.");
			}
			else if (type.equals("QUIT")) {
				if (runGUI)
					gui.gui.addTextHistory(nick+" disconnected.");
				if (relayChat())
					Shell.relayIRCChat("["+target+"] "+nick+" disconnected.");
				if (utils.addressesEqual(channels.get("#peer.dev"), MY_NICK, nick) && !gui.isVisible && runGUI)
					gui.toggleVisible();
				for (Channel c : channels.values()) { 
					Map<String, User> a = c.getUserList();
					if (a.containsKey(nick))
						a.remove(nick);
				}
			}
			else if (type.equals("PART")) {
				Channel c = channels.get(target.toLowerCase());
				c.getUserList().remove(nick);
				if (relayChat())
					Shell.relayIRCChat("["+target+"] "+nick+" parted.");
				if (runGUI)
					gui.gui.addTextHistory("["+target+"] "+nick+" parted.");
			}
		}

		else if (l.split(" ")[1].equals("352")) {
			System.err.println(l);
			String[] a = l.split(" ");
			String channel = a[3].toLowerCase();
			String realName = l.split(":")[2];
			User b = new User(a[4], a[5], a[6], a[7], a[8], realName);
			channels.get(channel).getUserList().put(a[7], b);
		}

	}

	public String EngineVersion() {
		return "Stable 5";
	}

	public String iUtilVersion() {
		return "1.1_"+System.getProperty("os.name");
	}

	@SuppressWarnings("deprecation")
	public void parseCommand(String l) throws IOException { 
		String nick = l.split("!")[0].substring(1);
		String address = l.split("!")[1].split(" ")[0];
		String target = l.split(" ")[2];
		String[] messageData = l.split(":");
		String message = "";
		for (int x = 2; x < messageData.length; x++) {
			message = message+":"+messageData[x];
		}
		message = message.substring(1);		
		boolean userIsAdmin = utils.isAdmin(channels.get("#peer.dev"), nick);
		String commandPrefix = message.substring(0, 1);
		boolean pub = !((".!").contains(commandPrefix));
		String sendPrefix = (pub ? "PRIVMSG "+target : "NOTICE "+nick)+" :";
		String[] commandData = message.split(" ");
		String command = commandData[0].substring(1);
		String commandParams = "";
		if (relayChat())
			Shell.relayIRCChat((userIsAdmin ? "[A] " : "")+nick+": "+message);
		try {
			commandParams = commandData[1];
			for (int x = 2; x < commandData.length; x++) {
				commandParams = commandParams+" "+commandData[x];
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {	}

		if (command.equals("IALdata") && userIsAdmin) {
			Channel c = channels.get(target.toLowerCase());
			Map<String, User> users = c.getUserList();
			String line = "AIL for "+target+" contains "+users.size()+" entries";
			send((pub ? "PRIVMSG "+target : "NOTICE "+nick)+" :"+line);
		}

		else if (command.equals("send") && userIsAdmin) {
			try {
				send(commandParams);
			}
			catch (Exception e) {
				send("PRIVMSG "+target+" :Unable to send: "+e.toString()+" @ "+e.getStackTrace()[0]);
			}
		}

		else if (command.equals("quit") && userIsAdmin) {
			requestedQuit = true;
			if (commandParams.equals(""))
				quit("Disconnect requested by "+nick);
			else
				quit ("Disconnect requested by "+nick+" ("+commandParams+")");
			YouTube.saveAllCaches();
		}

		else if (command.equals("relaychat") && userIsAdmin) {
			relayChat = Boolean.valueOf(commandParams.replaceAll("0", "false").replaceAll("1", "true"));
			String s = "IRC Chat will "+(!relayChat ? "NOT" : "")+" be relayed to servers.";
			send("PRIVMSG "+target+" :"+s);
		}

		else if (command.equals("listusers") && userIsAdmin) {
			send(sendPrefix+channels.get(target.toLowerCase()).getUserList().toString());
		}

		else if (command.equals("getchannelobject") && userIsAdmin) {
			send(sendPrefix+channels.get(target.toLowerCase()).toString());
		}

		else if (command.equals("IALcontains") && userIsAdmin) {
			send(sendPrefix+channels.get(target.toLowerCase()).getUserList().containsKey(commandParams));
		}

		else if (command.equals("userdata") && userIsAdmin) {
			System.err.println(commandParams);
			if (!(channels.get(target.toLowerCase()).getUserList()).containsKey(commandParams)) {
				send(sendPrefix+"IAL doesn't contain an entry for '"+commandParams+"'");
			}
			else {
				Map<String, User> a = channels.get(target.toLowerCase()).getUserList();
				send(sendPrefix+a.get(commandParams).toString());
			}
		}

		else if (command.startsWith("stop") && userIsAdmin) {				
			String thread = message.split("stop ")[1];
			if (thread.equals("mcchat")) {
				mindcrack.stop();
			}
			else if (thread.equals("youtube")) {
				YouTube.stopAll();
			}
			//			else if (thread.equals("awesomechat")) {
			//				AWeSome.stop();
			//				AWeSomeCreative.stop();
			//			}
			//			else if (thread.equals("olympics")) {
			//				olympics.stop();
			//			}
			else {
				send("PRIVMSG "+target+" :What'chu talkin' about Willis?");
				return;
			}
			String s = "Stopping thread for "+thread;
			System.err.println(s);
			send("PRIVMSG "+target+" :"+s);
		}

		else if (command.startsWith("start") && userIsAdmin) {
			String thread = message.split("start ")[1];
			if (thread.equals("mcchat")) {
				mindcrack.silent = true;
				mindcrack.start();
			}
			else if (thread.equals("youtube")) {
				YouTube.startAll();
			}
			//			else if (thread.equals("awesomechat")) {
			//				AWeSome.start();
			//				AWeSomeCreative.start();
			//			}
			//			else if (thread.equals("olympics")) {
			//				olympics.start();
			//			}
			else {
				send("PRIVMSG "+target+" :What'chu talkin' about Willis?");
				return;
			}
			String s = "Starting thread for "+thread;
			System.err.println(s);
			send("PRIVMSG "+target+" :"+s);
		}

		else if (command.startsWith("adduser") && userIsAdmin) {
			String user = commandParams;
			YouTube.addChannel(user);
		}

		else if (command.startsWith("deluser") && userIsAdmin) {
			String user = commandParams;
			YouTube.removeChannel(user);
		}

		else if (command.equals("listusernames") && userIsAdmin) {
			send(sendPrefix+YouTube.channels.toString());
		}

		else if (command.equals("serverdata") && userIsAdmin) {
			send(sendPrefix+this.server+", "+this.ACTUAL_SERVER);
		}

		else if (command.equals("listservers") && userIsAdmin) {
			send(sendPrefix+networks.toString());
		}

		if (command.matches("force(y(ou)?t(ube)?)?update") && userIsAdmin) {
			send("PRIVMSG "+target+" :Forcing update, please stand by...");
			if (commandParams.equals("")) {
				YouTube.updateChannel();
				send("PRIVMSG "+target+" :Finished forcing update on "+YouTube.channels.size()+" users!");
			}
			else
				YouTube.updateChannel(commandParams);
		}

		//		else if (command.matches("(olympics?(medal)?)(table)?")) {
		//			olympics.manualUpdate = true;
		//			olympics.run();
		//		}

		else if (command.matches("m(ine)?c(raft)?s(tats?)(us)?")) {
			new MCStatus(target, this);
		}

		else if (command.equals("addressesEqual") && userIsAdmin) {
			String[] users = commandParams.split(" ");
			send(sendPrefix+utils.addressesEqual(channels.get(target.toLowerCase()), users[0], users[1]));
		}

		else if (command.matches("is(down|up)")) {
			String webserver = Utils.getURL(commandParams);
			if (webserver.equals("No matches")) {
				send(sendPrefix+webserver+" doesn't seem to be a valid URL");
				return;
			}
			int response = Utils.getResponseCode(webserver);
			send(sendPrefix+webserver+(response == HttpURLConnection.HTTP_OK ? " is all good here!" : " seems down from here! ("+Utils.getErrorName(response)+")"));
		}

		else if (command.equals("restart") && userIsAdmin) {
			send(sendPrefix+"This command is deprecated, please use .quit instead.");
			//			try {
			//				String jar = Engine.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			//				jar = jar.substring(1);
			//				ArrayList<String> p = new ArrayList<String>();
			//				p.add("java");
			//				p.add("-jar");
			//				p.add(jar);
			//				p.add("-port="+this.port);
			//				p.add("-ssl="+this.SSL);
			//				p.add("-s="+this.server);
			//				p.add("-n="+MY_NICK);
			//				p.add("-p="+this.password);
			//				p.add("-force");
			//				ProcessBuilder a = new ProcessBuilder(p);
			//				send("QUIT :Restart command recieved from "+nick);
			//				Process b = a.start();
			//				System.exit(0);
			//			}
			//			catch (Exception e) {
			//				send(sendPrefix+"Unable to restart: "+e.toString());
			//				e.printStackTrace();
			//			}

		}

		else if (command.matches("(awesome)?(players|online)")) {
			if (!commandParams.equals("")) {
				if (commandParams.matches("s(urv(ival)?)?"))
					ASurvival.sendPlayers(sendPrefix, "Survival");
				else if (commandParams.matches("c(reat(e|ive)?)?"))
					ACreative.sendPlayers(sendPrefix, "Creative");
			}
			else {
				ASurvival.sendPlayers(sendPrefix, "Survival");
				ACreative.sendPlayers(sendPrefix, "Creative");
			}
		}

		else if (command.matches("(m(ine)?c(raft)?)?e?xp(ri[ea]nce)?")) {
			boolean snapshot = false;
			NumberFormat n = NumberFormat.getInstance();
			String errorString = "You must supply at least one level! "+commandPrefix+command+" LEVEL1 [LEVEL2]";
			int a = -1;
			int b = a;
			try {
				String[] a1 = commandParams.split(" ");
				if (a1[0].startsWith("-e")) {
					int exp1 = Integer.parseInt(a1[1]);
					int l1 = 0;
					while (MCUtils.getExp(l1, (a1[0].equals("-es") ? "" : "12w23b")) <= exp1) {
						l1++;		
					}
					int exp = MCUtils.getExp(l1, (a1[0].equals("-es") ? "" : "12w23b"));
					String out = c2(n.format(exp1))+c1(" exp is level ")+c2(n.format(l))+((exp - exp1) > 0 ? c1(" (")+c2("+"+n.format((exp-exp1)))+c1(")") : "");
					send(sendPrefix+out);
					return;

				}
				if (a1[0].equals("-s")) {
					snapshot = true;
					a = Integer.parseInt(a1[1]);
					b = Integer.parseInt(a1[2]);
				}
				else {
					a = Integer.parseInt(a1[0]);
					b = Integer.parseInt(a1[1]);
				}
				String line1 = "";
				String line2 = line1;
				int a2 = MCUtils.getExp(a, snapshot ? "" : "12w23b");
				int b1 = MCUtils.getExp(b, snapshot ? "" : "12w23b");
				int exp = 0;
				if (b > a) 
					exp = b1 - a2;
				else
					exp = a2 - b1;
				String blaze = n.format((int)Math.ceil(exp / 10));
				String hostile = n.format((int)Math.ceil(exp / 5));
				String passivemin = n.format((int)Math.ceil(exp / 3));
				String passivemax = n.format(exp);
				String dragon = n.format((int)Math.ceil(exp / 20000.0));
				String oremax = passivemin;
				String oremin = n.format((int)Math.ceil(exp / 9));
				String smelt = passivemax;
				line1 = c1("Level ")+c2(a < b ? Integer.toString(a) : Integer.toString(b))+c1(" to ")+c2(a < b ? Integer.toString(b) : Integer.toString(a))+c1(" is ")+c2(n.format(exp))+c1(" experience.");
				line2 = c1("Mine Ore: ")+c2(oremax)+c1(" (")+c2(oremin)+c1(") Smelt Item: ")+c2(smelt)+c1(" Hostile: ")+c2(hostile)+c1(" Blaze: ")+c2(blaze)+c1(" Passive: ")+c2(passivemax)+c1(" (")+c2(passivemin)+c1(") Ender Dragon: ")+c2(dragon);
				List<String> out = new ArrayList<String>();
				out.add(line1);
				out.add(line2);
				if (!snapshot) {
					String line3 = c1("If you are after the experience values for Minecraft 1.2.5 and earlier, use ")+c2(commandPrefix+command+commandParams.replaceFirst(" ", " -s "));
					out.add(line3);
				}
				for (String out2 : out)
					send(sendPrefix+out2);
			}
			catch (NumberFormatException n1) {
				errorString = "I'm not clever enough to use numbers that high :(";
				if (a == -1)
					send(sendPrefix+errorString);
			}
			catch (ArrayIndexOutOfBoundsException e) {
				if (a == -1)
					send(sendPrefix+errorString);
				else {
					String line1 = "";

					int exp = MCUtils.getExp(a, snapshot ? "12w23b" : "");
					line1 = colour+"14Level "+colour+"13"+n.format(a)+colour+"14 is "+colour+"13"+n.format(exp)+colour+"14 experience.";
					send(sendPrefix+line1);
				}

			}

		}

		else if (command.matches("(m(ine)?c(raft)?)?server(status)?")) {
			String data = "";
			if (!commandParams.equals("")) {
				data = commandParams;
				int port = 0;
				try {
					address = data.split(":")[0];
				}
				catch (ArrayIndexOutOfBoundsException e) { }
				try {
					port = Integer.parseInt(data.split(":")[1]);
				}
				catch (ArrayIndexOutOfBoundsException e) { }
				MCServer.pollServer(pub ? target : nick, pub ? 1 : 0, address, port, this);
			}
			else {
				MCServer.pollServer(pub ? target : nick, pub ? 1 : 0, "s.auron.co.uk", 0, this);
				MCServer.pollServer(pub ? target : nick, pub ? 1 : 0, "c.auron.co.uk", 0, this);
				return;
			}
		}

		else if (command.equals("showgui") && userIsAdmin) {
			if (runGUI)
				gui.toggleVisible();
			else
				send(sendPrefix+"I am running in NoGUI mode, there is no gui to show!");
		}

		else if (command.equals("path") && userIsAdmin) {
			send(sendPrefix+YouTubeUsernameConfig.getAbsolutePath());
		}

		else if (command.matches("(bot)?info(rmation)?")) {
			long totalMemory = Runtime.getRuntime().totalMemory();
			long freeMemory = Runtime.getRuntime().freeMemory();
			long usedMemory = totalMemory - freeMemory;
			String memory = (usedMemory / 1024L / 1024L)+"MB/"+(totalMemory / 1024L / 1024L)+"MB";
			DateFormat d = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.UK);
			d.setTimeZone(TimeZone.getTimeZone("GMT"));
			String ytupdateo = /*"YouTube: "+d.format(YouTube.updateAt + 600000);*/"";
			String mcupdateo = " | MCChat: "+d.format(mindcrack.updateAt + 120000);
			//String awesomeupdateo = " | AWeSomeChat: "+d.format(AWeSome.updateAt + 120000);
			String awesomeupdateo = "";
			String olyupdateo = "";/*" | Olympics: "+d.format(olympics.updateAt + olympics.updateDelay);*/
			String out =  "Memory Usage: "+memory+" | "+channels.size()+" channels | "+YouTube.channels.size()+" watched users";
			String out6 = "Thread Updates (GMT): "+ytupdateo+mcupdateo+awesomeupdateo+olyupdateo;
			String out2 = "Connection: "+connection.toString()+", "+connection.getReceiveBufferSize()+", "+connection.getSendBufferSize()+", "+this.ACTUAL_SERVER;
			String out3 = "Java: "+System.getProperty("sun.arch.data.model")+"-bit "+System.getProperty("java.version");
			String out5 = "Uptimes: "+GuiUtils.formatTime(botStart)+" (Bot) "+GuiUtils.formatTime(connectionStart)+" (Connection)";
			send(sendPrefix+out);
			send(sendPrefix+out6);
			send(sendPrefix+out2);
			send(sendPrefix+out3);
			if (commandParams.equals("-v") && runGUI) {
				String out4 = "GUI: "+gui.toString()+", "+gui.gui.toString()+", "+gui.hashCode()+"/"+gui.gui.hashCode();
				send(sendPrefix+out4);
			}
			send(sendPrefix+out5);
		}

		else if (command.matches("(last|latest)vid(eo)?")) {
			if (commandParams.equals("")) {
				send(sendPrefix+"You must specify a user to look up!");
			}
			else {
				int outType = (pub ? 1 : 0);
				GetLatestVideo.lookupLatestVideo(this, outType, pub ? target : nick, commandParams);
			}
		}

		else if (command.matches("calc(ulat[oe]r?)?")) {
			String line = "";
			try {
				String formula = commandParams;
				List<String> regexMatches = new ArrayList<String>();
				String eq = formula;
				//formula = formula.replaceAll("k", "*1000").replaceAll("K", "*1000").replaceAll("M", "*1000000").replaceAll("m", "*1000000").replaceAll("B", "*1000000000").replaceAll("b", "*1000000000");
				if (!formula.matches("[0-9\\*+-/^\\(\\)EKMBkmb]+")) {
					line = c1("*")+c2("*")+c1("* [")+c2("CALC")+c1("]: ")+c2("Invalid equasion!");
				}
				else {
					if (formula.contains("^")) {
						Pattern p = Pattern.compile("(\\^?[0-9\\.]+\\^[0-9\\.]+\\^?)");
						Matcher m = p.matcher(formula);
						while (m.find())
							regexMatches.add(m.group(1));
						for (String s : regexMatches) {
							String[] split = s.split("\\^");
							String a = split[0];
							String b = split[1];
							formula = formula.replaceAll(Pattern.quote(s), "Math.pow("+a+", "+b+")");
						}
					}
					//System.err.println("F: "+formula);
					ScriptEngineManager mgr = new ScriptEngineManager();
					ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
					Object a = jsEngine.eval(formula);
					Double b = (Double)a;
					String c = NumberFormat.getInstance().format(b);
					if (c.endsWith(".0"))
						c = c.substring(0, c.length() - 2);
					if (c.contains("E"))
						c = c.replace('.', '€').replaceAll("€", "");
					if (formula.equals("1+1"))
						c = "3";
					else if (formula.equals("2+2"))
						c = "5";
					line = c1("*")+c2("*")+c1("* [")+c2("CALC")+c1("]: ")+c2(eq)+c1(" = ")+c2(c);
				}
				send(sendPrefix+line);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	private boolean relayChat() {
		return relayChat && (!ASurvival.online.isEmpty() && !ACreative.online.isEmpty());
	}

	public static void main(String[] args) {
		if (args.length > 0 && args[0].equals("-passgen")) {
			System.err.println("Running in passgen mode");
			String s = args[1];
			String pass = args[2];
			File a = new File("config\\"+s.replaceAll("\\.", ""));
			if (!a.exists())
				a.mkdirs();
			File b = new File(a, "key");
			File e = new File(a, "password");
			KeyGenerator d;
			try {
				d = KeyGenerator.getInstance("AES");
				d.init(128);
				Key key = d.generateKey();
				DataOutputStream f = new DataOutputStream(new FileOutputStream(b));
				f.writeInt(key.getEncoded().length);
				f.write(key.getEncoded());
				f.close();
				Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
				aes.init(Cipher.ENCRYPT_MODE, key);
				byte[] out = aes.doFinal(pass.getBytes());
				f = new DataOutputStream(new FileOutputStream(e));
				int x = out.length;
				System.err.println(x);
				f.writeInt(x);
				f.write(out);
				f.close();
				System.err.println("Generated password @ "+e.getPath());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			System.exit(0);
		}
		Engine engine;
		String password = "";
		String server = "irc.swiftirc.net";
		int port = 6667;
		boolean SSL = false;
		boolean ignoreLock = false;
		boolean runIdentd = false;
		boolean guienabled = true;
		String nick = "iUtil";
		for (String a : args) {
			if (a.startsWith("-p="))
				password = a.split("-p=")[1];
			else if (a.startsWith("-ssl="))
				SSL = Boolean.parseBoolean(a.split("-ssl=")[1]);
			else if (a.startsWith("-s="))
				server = a.split("-s=")[1];
			else if (a.startsWith("-port="))
				port = Integer.parseInt(a.split("-port=")[1]);
			else if (a.startsWith("-n="))
				nick = a.split("-n=")[1];
			else if (a.equals("-force"))
				ignoreLock = true;
			else if (a.equals("-identd"))
				runIdentd = true;
			else if (a.equals("-nogui"))
				guienabled = false;
		}
		try {
			engine = new Engine(nick, server, port, SSL, password, ignoreLock, runIdentd, false, guienabled);
			engine.start();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void send(String s) throws IOException {
		out.write(s+"\r\n");
		out.flush();
		writeToLog("-> "+s);
	}

	public void writeToLog(String s) throws IOException {
		if (!s.endsWith("\n"))
			s = s+"\n";
		if (s.startsWith("-> PRIVMSG NickServ :identify"))
			return;
		logWriter.write(s);
		logWriter.flush();
	}

	private void join (String channel) throws IOException {
		send("JOIN "+channel);
		channels.put(channel.toLowerCase(), new Channel(channel));
		System.err.println("Now in "+channels.size()+" channel(s)");
		send("WHO "+channel);
	}

	public String c1(String s) {
		return colour+"14"+s;
	}
	public String c2(String s) {
		return colour+"13"+s;
	}
	public String c2(int i) {
		return c2(Integer.toString(i));
	}

	public void quit(String s) throws IOException {
		YouTube.saveAllCaches();
		send("QUIT :"+s);

	}

	/* All functions below are temporary until command's code is update to do it itself. */

	public void msgArray(String channel, String[] outArray) throws IOException {
		for (int x = 0; x < outArray.length; x++) {
			out.write("PRIVMSG "+channel+" :"+outArray[x]+"\r\n");
			writeToLog("-> "+channel+": "+outArray[x]);
		}
		out.flush();
	}

	public void msgList(String channel, List<String> outList) throws IOException {
		Iterator<String> i = outList.iterator();
		while (i.hasNext()) {
			String s = i.next();
			out.write("PRIVMSG "+channel+" :"+s+"\r\n");
			writeToLog("-> "+channel+": "+s);
		}
		out.flush();
	}

	public void noticeList(String nick, List<String> outList) throws IOException {
		Iterator<String> i = outList.iterator();
		while (i.hasNext()) {
			String s = i.next();
			out.write("NOTICE "+nick+" :"+s+"\r\n");
			System.out.println(MY_NICK+" -> "+nick+": "+s);
			writeToLog("-> "+nick+": "+s);
		}
		out.flush();
	}

	public void noticeArray(String user, String[] outArray) throws IOException {
		for (int x = 0; x < outArray.length; x++) {
			out.write("NOTICE "+user+" :"+outArray[x]+"\r\n");
			writeToLog("-> "+user+": "+outArray[x]);
		}
		out.flush();
	}

	public void amsg(String s) {
		for (Channel a : channels.values()) {
			try {
				send("PRIVMSG "+a.getName()+" :"+s);
			}
			catch (IOException e) { }
		}
	}

	public void restartConsole() {
		console = new Console(this);
		console.start();
	}

}
