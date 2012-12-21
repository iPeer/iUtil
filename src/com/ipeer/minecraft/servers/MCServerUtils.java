package com.ipeer.minecraft.servers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;

import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import com.ipeer.iutil.engine.Engine;

public class MCServerUtils extends Thread {

	public static void main(String[] args) {
		try {
			//pollServer(null, 0, "mc.neizt.co.uk", 0, null);
			//pollServer(null, null, "mc.neizt.co.uk", 0);
			pollServer(null, null, "s.auron.co.uk", 0);
/*			pollServer(null, 0, "c.auron.co.uk", 0, null);
			pollServer(null, 0, "e.auron.co.uk", 0, null);*/
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
	}
	
	public static void pollServer(Engine engine, String prefix, String addr, int port) {
		(new ThreadedMCServerPoller(engine, prefix, addr, port)).start();
	}

	@Deprecated
	public static void pollServer(String channel, int mode, String addr, int p, Engine engine) throws IOException {
		long ping1 = System.nanoTime();
		//System.out.println("Polling "+addr);
		Socket s = null;
		DataInputStream in = null;
		DataOutputStream out = null;
		int port = p;
		try {
			s = new Socket();
			s.setSoTimeout(3000);
			s.setTcpNoDelay(true);
			s.setTrafficClass(18);
			if (p == 0)
				port = getPort(addr);
			//System.err.println(addr+"'s port is "+port);
			s.connect(new InetSocketAddress(addr, port));
			in = new DataInputStream(s.getInputStream());
			out = new DataOutputStream(s.getOutputStream());
			out.write(254);
			out.write(1);

			if (in.read() != 255) { // Bad response
				if (channel != null)
					engine.send((mode == 1 ? "PRIVMSG " : "NOTICE ")+channel+" :ERROR: Bad response");
				System.out.println("Bad response!");
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
				motd = data1[3];
				playerCount = Integer.valueOf(data1[4]);
				maxPlayers = Integer.valueOf(data1[5]);
			}
			else {
				data1 = data.split("\247");
				motd = data1[0];
				playerCount = Integer.valueOf(data1[1]);
				maxPlayers = Integer.valueOf(data1[2]);
			}
			char c = Engine.colour;
			long ping2 = System.nanoTime();
			long ping = (ping2 - ping1) / 0xf4240L;
			// ("+c+"13"+port+c+"14)
			String out1 = c+"14["+c+"13"+addr+c+"14] "+(!version.equals("") ? c+"14("+c+"13"+version+c+"14) " : "")+c+"14MOTD: "+c+"13"+motd+c+"14 Players: "+(playerCount < 0 || maxPlayers < 0 ? c+"13???" : c+"13"+playerCount+c+"14/"+c+"13"+maxPlayers)+c+"14 Ping: "+c+"13"+ping+"ms";
			if (channel != null)
				engine.send((mode == 1 ? "PRIVMSG " : "NOTICE ")+channel+" :"+out1);
			else
				System.err.println(out1);

		}
		catch (Exception e) {
			e.printStackTrace();
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static int getPort(String addr) {
		try {
			Hashtable a = new Hashtable();
			a.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
			a.put("java.naming.provider.url", "dns:");
			InitialDirContext b = new InitialDirContext(a);
			Attributes c = b.getAttributes("_minecraft._tcp."+addr, new String[] {"SRV"});
			return Integer.parseInt(c.get("srv").get().toString().split(" ", 4)[2]);
		}
		catch (Throwable e) {
			return 25565;
		}
	}

}
