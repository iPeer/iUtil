package com.ipeer.iutil.remote.protocol;

import java.io.IOException;

import com.ipeer.iutil.engine.Engine;
import com.ipeer.iutil.engine.ThreadUtils;
import com.ipeer.iutil.remote.server.CannotSavePasswordException;
import com.ipeer.iutil.remote.server.NoSuchAccountException;
import com.ipeer.iutil.remote.server.ThreadedClient;
import com.ipeer.iutil.remote.server.UserAlreadyExistsException;
import com.ipeer.iutil.remote.server.iUtilAccount;
import com.ipeer.iutil.youtube.YouTube;


public class iUtilProtocol {

	private int state = -1;
	private ThreadedClient client;

	public iUtilProtocol (ThreadedClient threadedClient) {
		this.client = threadedClient;
	}

	public void process(String a) throws IOException {
		if (state > -1) {
			Engine engine = client.getServer().getEngine();
			if (a.equals("DISCONNECT")) {
				client.terminate();
			}
			else if (a.equals("LOGMEOUT")) {
				client.setAccount(null);
				client.send("\247aYou have been logged out.");
			}
			else if (a.matches("(bot)?info(rmation)?")) {
				String args = "";
				try {
					args = a.split(" ")[1];
				}
				catch (ArrayIndexOutOfBoundsException e) { }
				String[] s = engine.getInfoStrings(args);
				for (String s1 : s)
					client.send(s1);
			}
			else if (a.startsWith("CLIENTVERSION")) {
				int v = 0;
				try {
					v = Integer.valueOf(a.split(" ")[1]);
				}
				catch (ArrayIndexOutOfBoundsException e) { 
					client.send("\247cYour client responded with an invalid version response.");
					client.terminate();
				}
				if (v < client.server.clientVersion)
					client.send("\247c\247tYour client is outdated. Please download the latest version from https://dl.dropbox.com/u/21719562/Java/iUtil/iUtil-Client.jar");
			}
			else if (a.startsWith("setdelay")) {
				if (!client.isAdmin()) {
					client.send("\247cYou don't have the rights to use this command.");
					return;
				}
				String[] data = a.split(" ");
				if (data.length < 3) {
					client.send("\247cInvalid parameters.");
					client.send("\247csetdelay THREADNAME DELAYINGINMS");
					return;
				}
				String thread = data[1];
				long time = Long.valueOf(data[2]);
				if (thread.equalsIgnoreCase("youtube")) {
					Engine.YouTube.updateDelay = time;
					client.send("\247aYouTube thread to update every "+time+"ms.");
				}
				else if (thread.equalsIgnoreCase("twitch")) {
					Engine.twitchTV.updateDelay = time;
					client.send("\247aTwitch.TV thread to update every "+time+"ms.");
				}
				else if (thread.equalsIgnoreCase("awesomestatus")) {
					engine.serverStatus.updateDelay = time;
					client.send("\247aAWeSomeStatus thread to update every "+time+"ms.");
				}
				else {
					client.send("\247cUnknown thread!");
					return;
				}
				client.send("Changes will take effect the next time this thread runs.");
			}
			else if (a.equals("ping"))
				client.send("PONG!");
			else if (a.startsWith("login")) {
				String data[] = a.split(" ");
				if (data.length < 3) {
					client.send("\247cInvalid paramters!");
					client.send("/login \247vusername\247z \247vpassword\247z.");
					return;
				}
				if (client.isLoggedIn()) {
					client.send("\247cYou are already logged in! Type \247t/logout\247z\247c to log out of your current account.");
					return;
				}
				try {
					iUtilAccount acc = new iUtilAccount(data[1]);
					if (!data[2].equals(acc.getPassword())) {
						client.send("\247cThe password you supplied is incorrect.");
					}
					else {
						client.setAccount(acc);
						client.send("\247aYou are now logged in as "+acc.getUsername());
						client.send("\247aTo logout, type /logout");
					}

				}
				catch (NoSuchAccountException e) {
					client.send("\247cNo account with that name exists!");
				} catch (Exception e) {
					client.send("\247cUnable to check account data. Please contact an administrator.");
					e.printStackTrace();
				}

			}
			else if (a.startsWith("create")) {
				String[] data = a.split(" ");
				if (data.length < 4) {
					client.send("\247cInvalid paramters!");
					client.send("/create \247vusername\247z \247vpassword\247z \247vrepeatpassword\247z.");
					return;
				}
				if (!data[2].equals(data[3])) {
					client.send("\247cPasswords do not match.");
					return;
				}
				iUtilAccount acc;
				try {
					acc = new iUtilAccount(data[2], data[1], false);
					client.setAccount(acc);
					client.send("\247aYou are now logged in as "+acc.getUsername()+"!");
					client.send("\247aTo logout, type /logout");
					client.getServer().sendToAllAdminClients("\2478Account '"+acc.getUsername()+"' created by "+client.getSocket().getInetAddress().getHostAddress()+"/"+client.getSocket().getPort());
				} catch (UserAlreadyExistsException e) {
					client.send("\247cAn account with that name already exists.");
				} catch (CannotSavePasswordException e) {
					client.send("\247cUnable to create your account. Please contact an administrator.");
				}
			}
			else if (a.equals("testamsg")) {
				client.getServer().sendToAllClients("This is a test to see if all clients recieve messages.");
			}
			else if (a.equals("bandwidth")) {
				client.send("Bandwidth | IN: "+client.getServer().rec+" bytes, OUT: "+client.getServer().sent+" bytes.");
			}
			else if (a.startsWith("remadmin")) {
				if (!client.isAdmin()) {
					client.send("\247c\247tYou do have the correct rights to use this command.");
					return;
				}
				String[] data = a.split(" ");
				if (data.length < 2) {
					client.send("\247cInvalid paramters. Must specify a user to take admin from.");
					return;
				}
				try {
					iUtilAccount acc = new iUtilAccount(data[1]);
					acc.setAdmin(false);
					acc.saveData(true);
					client.send("\247a"+data[1]+" is no longer an administrator.");
					client.getServer().sendToUser(data[1], "\247cYou are no longer an administrator.");
				}
				catch (NoSuchAccountException e) {
					client.send("\247cThere is no account with that name.");
				} 
				catch (Exception e) {
					client.send("\247c\247tUnable to update user's rights.");
					client.send("\247c"+e.toString()+(e.getMessage() == null || e.getMessage().equals("null") ? "" : ": "+e.getMessage()));
					for (int x = 0; x < e.getStackTrace().length; x++)
						client.send("\247c    "+e.getStackTrace()[x].toString());
				}

			}
			else if (a.startsWith("setadmin")) {
				if (!client.isAdmin()) {
					client.send("\247c\247tYou do have the correct rights to use this command.");
					return;
				}
				String[] data = a.split(" ");
				if (data.length < 2) {
					client.send("\247cInvalid paramters. Must specify a user to give admin to.");
					return;
				}
				try {
					iUtilAccount acc = new iUtilAccount(data[1]);
					acc.setAdmin(true);
					acc.saveData(true);
					client.send("\247a"+data[1]+" is now an administrator.");
					client.getServer().sendToUser(data[1], "\247cYou are now an administrator.");
				}
				catch (NoSuchAccountException e) {
					client.send("\247cThere is no account with that name.");
				} 
				catch (Exception e) {
					client.send("\247c\247tUnable to update user's rights.");
					client.send("\247c"+e.toString()+(e.getMessage() == null || e.getMessage().equals("null") ? "" : ": "+e.getMessage()));
					for (int x = 0; x < e.getStackTrace().length; x++)
						client.send("\247c    "+e.getStackTrace()[x].toString());
				}

			}
			else if (a.equalsIgnoreCase("amiadmin"))
				client.send(Boolean.toString(client.isAdmin()));
			else if (a.startsWith("msg")) {
				if (!client.isAdmin()) {
					client.send("\247cYou do not have the rights to use this command.");
					return;
				}
				String[] data = a.split(" ");
				if (data.length < 3) {
					if (data.length < 2)
						client.send("\247cYou must supply a channel for the message!");
					else
						client.send("\247cNo text to send!");
					client.send("msg \247vchannel\247z \247vmessage\247z.");
					return;
				}
				String channel = data[1];
				String message = "";
				for (int x = 2; x < data.length; x++)
					message = message+(message.length() > 0 ? " " : "")+data[x];
				if (Engine.channels.containsKey(channel))
					engine.send("PRIVMSG "+channel+" :["+client.getAccount().getUsername()+"] "+message);
				else
					client.send("\247ciUtil is not in that channel.");
				client.getServer().sendToAllAdminClientsAndConsole(client.getAccount().getUsername()+" issued msg command: {msg, "+channel+", "+message+"}");
			}
			else if (a.startsWith("stop") && client.getAccount().isAdmin()) {				
				String thread = a.split("stop ")[1];
				/*if (thread.equals("mcchat")) {
					Engine.mindcrack.stop();
				}
				else */if (thread.equals("youtube")) {
					Engine.YouTube.stopAll();
				}
				else if (thread.equals("twitch")) {
					Engine.twitchTV.stop();
				}
				else if (thread.equals("awesomestatus")) {
					engine.serverStatus.stop();
				}
				//			else if (thread.equals("awesomechat")) {
				//				AWeSome.stop();
				//				AWeSomeCreative.stop();
				//			}
				//			else if (thread.equals("olympics")) {
				//				olympics.stop();
				//			}
				else {
					client.send("\247cUnknown thread.");
					return;
				}
				String s = "\247aStopping thread for "+thread;
				client.send(s);
			}

			else if (a.startsWith("start") && client.getAccount().isAdmin()) {
				String thread = a.split("start ")[1];
				/*if (thread.equals("mcchat")) {
					Engine.mindcrack.silent = true;
					Engine.mindcrack.start();
				}
				else */if (thread.equals("youtube")) {
					Engine.YouTube.startAll();
				}
				else if (thread.equals("twitch")) {
					Engine.twitchTV.start();
				}
				else if (thread.equals("awesomestatus")) {
					engine.serverStatus.start();
				}
				//			else if (thread.equals("awesomechat")) {
				//				AWeSome.start();
				//				AWeSomeCreative.start();
				//			}
				//			else if (thread.equals("olympics")) {
				//				olympics.start();
				//			}
				else {
					client.send("\247cUnknown thread.");
					return;
				}
				String s = "\247aStopping thread for "+thread;
				client.send(s);
			}
		}
		else {
			switch (state) {
			case -1:
				String[] b = {"Your connection was successful. Your connection ID is \247t"+client.getId()+"\247z.",
						"Please log in to your account or create a new one.",
						"To log in to an existing account: /login \247vusername\247z \247vpassword\247z.",
						"To create a new account: /create \247vusername\247z \247vpassword\247z \247vrepeatpassword\247z.",
				"SENDCLIENTVERSION"};
				//client.send("CONNECTSUCCESS "+b.length);
				//client.send(b);
				for (String a1 : b) {
					client.send(a1);
				}
				state++;
				break;
			}
		}
	}

}
