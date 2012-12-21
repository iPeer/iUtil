package com.ipeer.minecraft.servers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.ipeer.iutil.engine.AWeSomeServerStatus;
import com.ipeer.iutil.engine.Engine;

public class ThreadedMCServerPoller extends Thread {

	private Engine engine;
	private String prefix;
	private MinecraftServer mcserver;
	private boolean isStatus = false;

	/* Stuff used for the service status on mc.auron.co.uk */
	private String server, status, version;
	private int port, severity;
	private long ping;

	public ThreadedMCServerPoller(Engine engine, String prefix, String address, int port) {
		this(engine, prefix, address, port, false);
	}

	public ThreadedMCServerPoller(Engine engine, String prefix, String address, int port, boolean status) {
		this.engine = engine;
		this.prefix = prefix;
		this.mcserver = new MinecraftServer(address, port);
		this.isStatus = status;
	}

	public void run() { 
		try {
			long ping1 = System.nanoTime();
			//System.out.println("Polling "+addr);
			Socket s = null;
			DataInputStream in = null;
			DataOutputStream out = null;
			int port;
			String addr = mcserver.getAddress();
			if (mcserver.getPort() == 0)
				port = MCServerUtils.getPort(mcserver.getAddress());
			else
				port = mcserver.getPort();
			this.server = mcserver.getAddress();
			this.port = port;
			try {
				s = new Socket();
				s.setSoTimeout(3000);
				s.setTcpNoDelay(true);
				s.setTrafficClass(18);
				//System.err.println(addr+"'s port is "+port);
				s.connect(new InetSocketAddress(addr, port), 3000);
				in = new DataInputStream(s.getInputStream());
				out = new DataOutputStream(s.getOutputStream());
				out.write(254);
				out.write(1);

				if (in.read() != 255) { // Bad response
					if (!isStatus) {
						if (this.prefix != null)
							engine.send(this.prefix+" :ERROR: Bad response");
						System.out.println("Bad response!");
					}
					else {
						this.status = "The server responded with unexpected data.";
						this.version = "???";
						this.severity = 2;
					}
					return;
				}

				String data = Packet.readLine(in, 256);
				char[] chars = data.toCharArray();
				data = new String(chars);
				String[] data1;
				String motd = "";
				int playerCount = -1;
				int maxPlayers = -1;
				String version = "";
				if (data.startsWith("\247") && data.length() > 1) {
					data1 = data.split("\0");
					version = data1[2];
					this.version = version;
					motd = data1[3];
					playerCount = Integer.valueOf(data1[4]);
					maxPlayers = Integer.valueOf(data1[5]);
				}
				else {
					this.version = "unknown";
					data1 = data.split("\247");
					motd = data1[0];
					playerCount = Integer.valueOf(data1[1]);
					maxPlayers = Integer.valueOf(data1[2]);
				}
				char c = Engine.colour;
				long ping2 = System.nanoTime();
				long ping = (ping2 - ping1) / 0xf4240L;
				// ("+c+"13"+port+c+"14)
				if (isStatus) {
					this.status = "Online - "+ping+"ms";
					this.severity = (ping > 500 ? 1 : 0);
					this.ping = ping;
				}
				else {
					String out1 = c+"14["+c+"13"+addr+c+"14] "+(!version.equals("") ? c+"14("+c+"13"+version+c+"14) " : "")+c+"14MOTD: "+c+"13"+motd+c+"14 Players: "+(playerCount < 0 || maxPlayers < 0 ? c+"13???" : c+"13"+playerCount+c+"14/"+c+"13"+maxPlayers)+c+"14 Ping: "+c+"13"+ping+"ms";
					if (this.prefix != null)
						engine.send(this.prefix+out1);
					else
						System.err.println(out1);
				}
			}
			catch (Exception e) {
				if (!isStatus)
					e.printStackTrace();
				String out1 = "Unable to connect to server "+addr+":"+port+": "+e.getMessage();
				if (e instanceof UnknownHostException) {
					out1 = "Unable to connect to server "+addr+":"+port+": Unknown host \""+addr+"\"";
					this.status = "Offline - Unknown Host";
					this.severity = 2;
				}
				else if (e instanceof ConnectException) {
					this.status = "Offline - "+e.getMessage();
					this.severity = 2;
				}
				else if (e instanceof SocketTimeoutException) {
					out1 = "Timed out when connecting to server "+addr+":"+port;
					this.status = "Offline - Timed out";
					this.severity = 2;
				}
				else if (e instanceof IOException) {
					out1 = "A communication error occurred when trying to connect to "+addr+":"+port;
					this.status = "Offline - Communication error";
					this.severity = 2;
				}
				if (!isStatus && this.prefix != null)
					engine.send(this.prefix+out1);
				//System.out.println(out1);
			}
			finally {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
				if (s != null) 
					s.close();
			}
		}
		catch (Exception e) {
			String s = "An error occurred when trying to poll the minecraft server "+mcserver.getAddress()+"/"+mcserver.getPort()+": "+e.getMessage();
			if (isStatus) {
				this.status = "Offline - Unknown error: "+e.getMessage();
				this.severity = 3;
			}
			else {
				if (engine != null)
					engine.amsg(s);
				else
					System.err.println(s);
			}
			e.printStackTrace();
		}

		if (isStatus) {
			AWeSomeServerStatus.data.add(getStatusString());
			if (AWeSomeServerStatus.data.size() == 3)
				AWeSomeServerStatus.writeDataToFile();
		}

	}

	public String getStatusString() {
		return this.server+"\01"+this.port+"\01"+this.version+"\01"+this.ping+"\01"+this.severity+"\01"+this.status;
	}
	
	@Override
	public String toString() {
		return getStatusString();
	}

}
