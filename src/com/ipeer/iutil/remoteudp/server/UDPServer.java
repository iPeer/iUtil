package com.ipeer.iutil.remoteudp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPServer implements Runnable {

	private int port;
	private DatagramSocket server;
	private boolean IS_RUNNING = false;
	private Thread thread;

	public UDPServer() throws SocketException {
		this(4445);
	}

	public UDPServer(int port) throws SocketException {
		this.port = port;
		this.server = new DatagramSocket(port);
	}

	public void start() {
		IS_RUNNING = true;
		(thread = new Thread(this, "iUtil UDP Remote Control Server")).start();
	}

	@Override
	public void run() {
		byte[] in = new byte[1024];
		byte[] out = new byte[1024];
		while (IS_RUNNING && !Thread.interrupted()) {
			try {
				DatagramPacket packet = new DatagramPacket(in, in.length);
				server.receive(packet);
				
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws SocketException {
		UDPServer a = new UDPServer();
	}

}
