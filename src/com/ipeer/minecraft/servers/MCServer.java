package com.ipeer.minecraft.servers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.ipeer.iutil.engine.Engine;

public class MCServer {

	public static void main(String[] args) {
		try {
			pollServer(null, 0, "127.0.0.1", 7778, null);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void pollServer(String channel, int mode, String addr, int port, Engine engine) throws IOException {
		long ping1 = System.nanoTime();
		System.out.println("Polling "+addr+":"+port);
		Socket s = null;
		DataInputStream in = null;
		DataOutputStream out = null;
		try {
			s = new Socket();
			s.setSoTimeout(3000);
			s.setTcpNoDelay(true);
			s.setTrafficClass(18);
			s.connect(new InetSocketAddress(addr, port));
			in = new DataInputStream(s.getInputStream());
			out = new DataOutputStream(s.getOutputStream());
			out.write(254);
			
			if (in.read() != 255) { // Bad response
				if (channel != null)
					engine.send((mode == 1 ? "PRIVMSG " : "NOTICE ")+channel+" :ERROR: Bad response");
				System.out.println("Bad response!");
				return;
			}
			
			String data = Packet.readLine(in, 256);
			char[] chars = data.toCharArray();
			data = new String(chars);
			String[] data1 = data.split("\247");
			String motd = data1[0];
			int playerCount = Integer.parseInt(data1[1]);
			int maxPlayers = Integer.parseInt(data1[2]);
			char c = Engine.colour;
			long ping2 = System.nanoTime();
			long ping = (ping2 - ping1) / 0xf4240L;
			String out1 = c+"14["+c+"13"+addr+c+"14 ("+c+"13"+port+c+"14)] MOTD: "+c+"13"+motd+c+"14 Players: "+(playerCount < 0 || maxPlayers < 0 ? c+"13???" : c+"13"+playerCount+c+"14/"+c+"13"+maxPlayers)+c+"14 Ping: "+c+"13"+ping+"ms";
			if (channel != null)
				engine.send((mode == 1 ? "PRIVMSG " : "NOTICE ")+channel+" :"+out1);
			
		}
		catch (Exception e) {
			String out1 = "Unable to connect to server "+addr+":"+port+": "+e.getMessage();
			if (e instanceof UnknownHostException)
				out1 = "Unable to connect to server "+addr+":"+port+": Unknown host \""+addr+"\"";
			if (channel != null)
				engine.send((mode == 1 ? "PRIVMSG " : "NOTICE ")+channel+" :"+out1);
			System.out.println(out1);
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
	
}
