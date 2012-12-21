package com.ipeer.iutil.remote.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.ipeer.iutil.remote.protocol.iUtilProtocol;

public class ThreadedClient extends Thread {

	private int ID;
	private PrintWriter out;
	private BufferedReader in;
	private Socket socket;
	private iUtilProtocol protocol;
	public iUtilServer server;
	private Thread thread;
	private int state = -1;
	boolean open = false;
	private iUtilAccount account;

	public ThreadedClient(iUtilServer server, PrintWriter a, BufferedReader b, int id, Socket client) {
		this.ID = id;
		this.socket = client;
		this.in = b;
		this.out = a;
		//this.protocol = protocol;
		this.server = server;
	}

	public ThreadedClient(iUtilServer server, Socket socket) {
		super("ThreadClient: "+socket.getInetAddress().getHostAddress()+"/"+socket.getPort());
		this.socket = socket;
		this.server = server;
		this.open = true;
	}

	@Override
	public void run() { 
		try {
			this.out = new PrintWriter(socket.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String input, output;
			iUtilProtocol protocol = new iUtilProtocol(this);
			protocol.process(null);
			//	out.println(output);
			while (open && (input = in.readLine()) != null) {
				//System.err.println("[CLI] <- "+input);
				server.rec += input.getBytes().length;
				protocol.process(input);
				//out.println(output);
			}
			if (open)
				terminate();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void send(String string) {
		server.sent += string.getBytes().length;
		out.println(string);
	}

	public void send(String[] b) {
		for (String a : b)
			send(a);
	}

	public PrintWriter getOut() {
		return this.out;
	}

	public Socket getSocket() {
		return this.socket;
	}
	
	public void terminate() {
		terminate(true);
	}

	public void terminate(boolean b) {
		try {
			out.println("\247tYou have been disconnected.");
			open = false;
			out.close();
			in.close();
			socket.close();
			if (b)
				server.removeConnection(this);
			server.sendToAllAdminClientsAndConsole("\2478"+socket.getInetAddress().getHostAddress()+"/"+socket.getPort()+" "+(isLoggedIn() ? "("+account.getUsername()+") " : "")+"disconnected.");
		}
		catch (IOException e) {
			System.err.println("[SRV] [WARNING] Unable to cleanly terminate connection.");
			e.printStackTrace();
		}
	}

	public void setAccount(iUtilAccount account) {
		this.account = account;
	}

	public iUtilAccount getAccount() {
		return this.account;
	}

	public boolean isLoggedIn() {
		return !(this.account == null);
	}

	public boolean isAdmin() {
		return isLoggedIn() && account.isAdmin();
	}

	public iUtilServer getServer() {
		return this.server;
	}

}
