package com.ipeer.iutil.remote.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import com.ipeer.iutil.engine.Engine;

public class iUtilServer implements Runnable {

	private int port = 4444;
	private Thread thread;
	private boolean IS_RUNNING = false;
	private ServerSocket server;
	private List<ThreadedClient> connections;
	public volatile long sent = 0;
	public volatile long rec = 0;
	private Engine engine;
	public int clientVersion = 1;

	public iUtilServer() {
		this.engine = Engine.engine;
	}
	
	public iUtilServer(Engine engine) {
		this.engine = engine;
	}

	public static void main(String[] args) {
		iUtilServer a = new iUtilServer();
		a.start();
	}

	public void start() {
		IS_RUNNING = true;
		(thread = new Thread(this, "iUtil Remote Access Server")).start();
	}

	public void run() { 
		try {
			System.err.println("Server starting up...");
			connections = new ArrayList<ThreadedClient>();
			this.server = new ServerSocket(port);
		}
		catch (IOException e) {
			System.err.println("Unable to listen on port "+port);
			e.printStackTrace();
			IS_RUNNING = false;
			return;
		}
		System.err.println("Server started on port "+port);
		while (IS_RUNNING) { 
			try {
				ThreadedClient a = new ThreadedClient(this, server.accept());
				connections.add(a);
				a.start();
				sendToAllAdminClientsAndConsole("\2478Connection accepted from "+a.getSocket().getInetAddress().getHostAddress()+"/"+a.getSocket().getPort());
			} catch (IOException e) {
				sendToAllAdminClientsAndConsole("\2478Could not accept connection on port "+port);
				e.printStackTrace();
			}

		}
	}

	public void removeConnection(ThreadedClient threadedClient) {
		connections.remove(threadedClient);
	}

	public void sendToAllClients(String string) {
		for (ThreadedClient a : connections)
			a.send(string);
	}
	
	public void sendToAllLoggedInClients(String string) {
		for (ThreadedClient a : connections)
			if (a.isLoggedIn())
				a.send(string);
	}
	
	public void sendToAllAdminClients(String string) {
		for (ThreadedClient a : connections)
			if (a.isAdmin())
				a.send(string);
	}
	
	public void sendToAllAdminClientsAndConsole(String string) {
		System.err.println("[SRV] "+string);
		for (ThreadedClient a : connections)
			if (a.isAdmin())
				a.send(string);
	}

	public void sendToUser(String string, String string2) {
		for (ThreadedClient a : connections)
			if (a.getAccount() != null && a.getAccount().getUsername().equals(string))
				a.send(string2);
	}

	public Engine getEngine() {
		return this.engine;
	}

	public void terminateAll() {
		for (ThreadedClient a : connections)
			a.terminate(false);
		connections.clear();
			
	}


}
