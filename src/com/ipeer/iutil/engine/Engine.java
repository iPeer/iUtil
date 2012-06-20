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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.ipeer.minecraft.servers.MCServer;

public class Engine implements Runnable {

	public int port = 6667;
	public String server = "";
	public String MY_NICK = "";
	public boolean SSL = false;
	public boolean isRunning = false;
	public String ACTUAL_SERVER = "";
	public final String commandChars = "#@.!";

	public static Map<String, Channel> channels = new HashMap<String, Channel>();
	public static Map<String, String> networks = new HashMap<String, String>();
	public static File MCChatCache = null;
	public static File YouTubeVIDCache = null;
	public static File YouTubeUsernameConfig = null;
	public static VideoChecker YouTube;
	public static MCChat mindcrack, AWeSome, AWeSomeCreative;

	public static final char colour = 03;
	public static final char bold = 02;
	public static final char underline = (char)1f;
	public static final char italics = (char)1d;
	public static final char reverse = 16;
	public static final char endall = (char)0f;

	protected Socket connection;
	protected Utils utils;
	protected IdentdServer identd;
	protected String password = "";

	private BufferedWriter out;
	private BufferedReader in;

	private Map<String, String> networkSettings = new HashMap<String, String>();

	private static Writer logWriter;
	public static Engine engine;


	public Engine(String nick, String server, int port, boolean SSL, String password) throws IOException {
		this.MY_NICK = nick;
		this.server = server;
		this.port = port;
		this.SSL = SSL;
		this.identd = new IdentdServer(this);
		this.password = password;
		File lockFile = new File("F:\\Dropbox\\Java\\iUtil\\config");
		if (!lockFile.exists())
			lockFile.mkdirs();
		YouTubeUsernameConfig = new File(lockFile, "YouTubeUsernameConfig.cfg");
		lockFile = new File(lockFile, "\\lock.lck");
		File logFile = new File("F:\\Dropbox\\Java\\iUtil\\logs\\"+this.server.replaceAll("\\.", ""));
		if (!logFile.exists())
			logFile.mkdirs();
		logFile = new File(logFile, "\\sent.log");		
		logFile.createNewFile();
		File MCChatCache1 = new File("F:\\Dropbox\\Java\\iUtil\\caches\\"+this.server.replaceAll("\\.", ""));
		if (!MCChatCache1.exists())
			MCChatCache1.mkdirs();
		YouTubeVIDCache = new File(MCChatCache1, "YouTube.iuc");
		if (!YouTubeVIDCache.exists())
			YouTubeVIDCache.createNewFile();
		MCChatCache1 = new File(MCChatCache1, "MCChat.iuc");
		MCChatCache = MCChatCache1;
		if (!MCChatCache.exists())
			MCChatCache.createNewFile();
		logWriter = new OutputStreamWriter(new FileOutputStream(logFile));
		lockFile.createNewFile();
		lockFile.deleteOnExit();
		utils = new Utils(this);
		writeToLog("-> STARTING UP");
		YouTube = new VideoChecker(this);
		mindcrack = new MCChat("http://guudelp.com/serverlog.cgi", this);
		AWeSome = new MCChat("http://auron.co.uk/mc/chat.php", this);
		AWeSomeCreative = new MCChat("http://auron.co.uk/mc/chat.php?world=creative", this);
	}

	public void start() {
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
					send("MODE "+MY_NICK+" +Bp");
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
			YouTube.start();
			mindcrack.start();
			AWeSome.start();
			AWeSomeCreative.start();
			line = "";
			while ((line = in.readLine()) != null) {
				try {
					parseLine(line);
				}
				catch (Exception e) { e.printStackTrace(); }
			}


		}
		catch (Exception e) { e.printStackTrace(); }

	}

	public void parseLine(String l) throws IOException {
		writeToLog("<- "+l);
		if (l.startsWith("PING ")) {
			System.err.println(l);
			System.err.println("PONG "+l.substring(5));
			send("PONG "+l.substring(5));
		}
		else if (l.startsWith("ERROR :")) {
			System.err.println(l.substring(7));
			writeToLog("-> DISCONNECTED: "+l.substring(7));
			System.exit(0);
		}

		else if (l.split(" ")[1].equals("MODE")) {
			String[] raw = l.split(" ");
			String channel = raw[2];
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
			System.out.println((type.equals("NOTICE") ? "<- " : "")+"["+target+"] "+ nick+": "+message);

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

			else if (message.matches("https?://(www.)?youtube.com/watch\\?.*") && l.indexOf("video_response_view_all") < 0 && l.indexOf("/playlist") < 0 && !nick.equals("iUtil")) {
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
			else if (message.matches("https?://(www.)?youtu.be/.*") && !nick.equals("iUtil")) {
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
					String[] data = message.replaceAll("Â ", " ").split("/");
					String statusID = "0";
					if (!data[4].equals("status") && !data[4].equals("statuses")) 
						return;
					if (data[5].equals("status"))
						statusID = data[6].split(" ")[0];
					else
						statusID = data[5].split(" ")[0];
					Twitter.getTweetInfo(this, 1, target, statusID);
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
			if (type.equals("JOIN"))
				send("WHO +cn "+target+" "+nick);
			else {
				for (Channel c : channels.values()) { 
					//System.err.println(c.toString());
					Map<String, User> a = c.getUserList();
					if (a.containsKey(nick))
						a.remove(nick);
				}
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

	private String EngineVersion() {
		return "Stable 1";
	}

	private String iUtilVersion() {
		return "1.0";
	}

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

		else if (command.equals("quit") && userIsAdmin) {
			if (commandParams.equals(""))
				quit("Disconnect requested by "+nick);
			else
				quit ("Disconnect requested by "+nick+" ("+commandParams+")");
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
				YouTube.stop();
			}
			else if (thread.equals("awesomechat")) {
				AWeSome.stop();
				AWeSomeCreative.stop();
			}
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
				mindcrack.start();
			}
			else if (thread.equals("youtube")) {
				YouTube.start();
			}
			else if (thread.equals("awesomechat")) {
				AWeSome.start();
				AWeSomeCreative.start();
			}
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
			YouTube.addUser(user, target);
		}

		else if (command.startsWith("deluser") && userIsAdmin) {
			String user = commandParams;
			YouTube.removeUser(user, target);
		}

		else if (command.equals("listusernames") && userIsAdmin) {
			send(sendPrefix+YouTube.getUsernames().toString());
		}

		else if (command.equals("serverdata") && userIsAdmin) {
			send(sendPrefix+this.server+", "+this.ACTUAL_SERVER);
		}

		else if (command.equals("listservers") && userIsAdmin) {
			send(sendPrefix+networks.toString());
		}

		if (command.matches("force(y(ou)?t(ube)?)?update") && userIsAdmin) {
			send("PRIVMSG "+target+" :Forcing update, please stand by...");
			YouTube.updateUserNames();
			send("PRIVMSG "+target+" :Finished forcing update on "+YouTube.getUsernames().size()+" users!");
		}

		else if (command.equals("addressesEqual") && userIsAdmin) {
			String[] users = commandParams.split(" ");
			send(sendPrefix+utils.addressesEqual(channels.get(target.toLowerCase()), users[0], users[1]));
		}

		if (command.matches("(m(ine)?c(raft)?)?e?xp(ri[ea]nce)?")) {
			boolean snapshot = false;
			NumberFormat n = NumberFormat.getInstance();
			String errorString = "You must supply at least one level! "+commandPrefix+command+" LEVEL1 [LEVEL2]";
			int a = -1;
			int b = a;
			try {
				String[] a1 = commandParams.split(" ");
				if (a1[0].startsWith("-e")) {
					int exp1 = Integer.parseInt(a1[0]);
					int l1 = 0;
					while (MCUtils.getExp(l1, (a1[0].equals("-es") ? "12w23b" : "")) <= exp1) {
						l1++;		
					}
					int exp = MCUtils.getExp(l1, (a1[0].equals("-es") ? "12w23b" : ""));
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
				int a2 = MCUtils.getExp(a, snapshot ? "12w23b" : "");
				int b1 = MCUtils.getExp(b, snapshot ? "12w23b" : "");
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
					String line3 = c1("If you are after the experience values for snapshot 12w23b onwards, use ")+c2(commandPrefix+command+commandParams.replaceFirst(" ", " -s "));
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
				int port = 25565;
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
				MCServer.pollServer(pub ? target : nick, pub ? 1 : 0, "auron.co.uk", 25565, this);
				MCServer.pollServer(pub ? target : nick, pub ? 1 : 0, "auron.co.uk", 25566, this);
				return;
			}
		}


		else if (command.matches("(bot)?info(rmation)?")) {
			long totalMemory = Runtime.getRuntime().totalMemory();
			long freeMemory = Runtime.getRuntime().freeMemory();
			long usedMemory = totalMemory - freeMemory;
			String memory = (usedMemory / 1024L / 1024L)+"MB/"+(totalMemory / 1024L / 1024L)+"MB";
			long YTUpdate = (((YouTube.updateAt + 600000) - System.currentTimeMillis()) / 1000);
			long MCUpdate = (((mindcrack.updateAt + 120000) - System.currentTimeMillis()) / 1000);
			int minutes = (int)((YTUpdate % 3600) / 60);
			int seconds = (int)(YTUpdate % 60);
			String ytupdateo = "YouTube: "+(minutes < 10 ? "0"+minutes : minutes)+":"+(seconds < 10 ? "0"+seconds : seconds);
			minutes = (int)(MCUpdate % 3600) / 60;
			seconds = (int)MCUpdate % 60;
			String mcupdateo = " | MCChat: "+(minutes < 10 ? "0"+minutes : minutes)+":"+(seconds < 10 ? "0"+seconds : seconds);
			MCUpdate = (((AWeSome.updateAt + 120000) - System.currentTimeMillis()) / 1000);
			minutes = (int)(MCUpdate % 3600) / 60;
			seconds = (int)MCUpdate % 60;
			String awesomeupdateo = " | AWeSomeChat: "+(minutes < 10 ? "0"+minutes : minutes)+":"+(seconds < 10 ? "0"+seconds : seconds);
			String out =  "Memory Usage: "+memory+" | "+channels.size()+" channels | "+YouTube.getUsernames().size()+" watched users | "+ytupdateo+mcupdateo+awesomeupdateo;
			String out2 = "Connection: "+connection.toString()+", "+connection.getReceiveBufferSize()+", "+connection.getSendBufferSize()+", "+this.ACTUAL_SERVER;
			send(sendPrefix+out);
			send(sendPrefix+out2);
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
				formula = formula.replaceAll("k", "*1000").replaceAll("K", "*1000").replaceAll("M", "*1000000").replaceAll("m", "*1000000").replaceAll("B", "*1000000000").replaceAll("b", "*1000000000");
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


	public static void main(String[] args) {
		Engine engine;
		String password = "";
		String server = "irc.swiftirc.net";
		int port = 6667;
		boolean SSL = false;
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
		}
		try {
			engine = new Engine(nick, server, port, SSL, password);
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

}
