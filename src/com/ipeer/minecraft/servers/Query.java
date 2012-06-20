package com.ipeer.minecraft.servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Random;

public class Query implements Runnable {

	private String address = "";
	private int port = 25565;
	public boolean isRunning = false;
	private int sessionToken;
	private int sessionID;

	private static final byte HANDSHAKE = 0x09;
	private static final byte STATISTIC = 0x00;

	public static void main(String[] args) {
		Query q = new Query("127.0.0.1", 47337);
		q.start();
	}

	public void start() {
		isRunning = true;
		(new Thread(this)).start();
	}


	public void stop() {
		isRunning = false;
	}

	public Query (String address, int port) {
		this.address = address;
		this.port = port;
	}

	@Override
	public void run() {
		System.err.println(address+", "+port);
		while (isRunning) {
			try {
				DatagramSocket s = new DatagramSocket();
				s.setSoTimeout(30000);
				s.connect(new InetSocketAddress(address, port));
				// Get session token
				writeData(s, HANDSHAKE, "");
				s.close();

				Thread.sleep(300000);
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void writeData(DatagramSocket s, byte type, String payload) throws IOException {
		StringBuilder c = new StringBuilder();
		c.append(" ( ");
		int sessionID = new Random().nextInt(899)+100;
		byte[] session = Integer.toString((sessionID)).getBytes();
		for (byte b1 : session) {
			c.append(b1+" ");
		}
		c.append(")");
		System.err.println("SID: "+sessionID+c.toString());
		c = new StringBuilder();
		byte[] data0 = {(byte)254, (byte)253, type, 0, 0, 0, 1};
		byte[] data = data0;
		if (type == STATISTIC) {
			byte[] payload1 = { (byte)(this.sessionToken >> 24), (byte)(this.sessionToken >> 16 & 0xff), (byte)(this.sessionToken >> 8 & 0xff), (byte)(this.sessionToken & 0xff) };
			data = new byte[data0.length + payload1.length];
			for (int i = 0; i < data.length; i++) {
				data[i] = i < data0.length ? data0[i] : payload1[i - data0.length];
			}
		} 
		for (byte a1 : data) {
			c.append(a1+" ");
		}
		System.err.println("DATA: "+c.toString());
		c = new StringBuilder();
		DatagramPacket p = new DatagramPacket(data, data.length);
		s.send(p);
		s.receive(p);
		byte[] data1 = p.getData();
		for (byte b1 : data1) {
			c.append(b1+" ");
		}
		System.err.println("RESPONSE: "+c.toString());
		c = new StringBuilder();

		this.sessionToken = byteArrayToInt(data, 0);
		System.err.println("SESSION TOKEN: "+this.sessionToken);
		if (type != STATISTIC)
			writeData(s, STATISTIC, Integer.toString(this.sessionToken));
	}

	public static int byteArrayToInt(byte[] b, int offset) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (b[i + offset] & 0xFF) << shift;
		}
		return value;
	}

}
