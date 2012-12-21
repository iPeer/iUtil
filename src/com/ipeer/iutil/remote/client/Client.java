package com.ipeer.iutil.remote.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;

public class Client implements Runnable {

	private boolean IS_RUNNING = false;
	private int port = 4444;
	private String host = "127.0.0.1";
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private Thread thread;
	private File log;

	public static void main(String[] args) {
		//iUtilServer a = new iUtilServer();
		Client b = new Client();
		//a.start();
		b.start();
	}

	public void start() {
		IS_RUNNING = true;
		(thread = new Thread(this, "iUtil Remote Client")).start();
	}

	public void run() {
		try {
			System.out.println("Attempting to create connection...");
			this.socket = new Socket(host, port);
			this.out = new PrintWriter(this.socket.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			System.err.println("Connection created.");
		}
		catch (Exception e) {
			System.err.println("Unable to connect to "+host+":"+port);
			e.printStackTrace();
			IS_RUNNING = false;
			return;
		}
		try {
			String input, output;
			while ((input = in.readLine()) != null) {
				if (!input.equals("null")) {
					System.err.println("[SRV] "+input);
				}
			}
			System.err.println("Disconnected.");
			in.close();
			out.close();
			socket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void log(String text) throws IOException {
		RandomAccessFile a = new RandomAccessFile(this.log, "rw");
		a.writeUTF(text);
	}

}
