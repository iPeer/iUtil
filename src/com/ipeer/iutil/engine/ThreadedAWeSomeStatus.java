package com.ipeer.iutil.engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ipeer.minecraft.servers.Query;

public class ThreadedAWeSomeStatus implements Runnable {

	private InetSocketAddress[] addresses;
	public boolean RUNNING = false;
	public long updateDelay = 60000;
	private Thread thread;

	public static void main(String[] args) {
		ThreadedAWeSomeStatus a = new ThreadedAWeSomeStatus(new InetSocketAddress("auron.co.uk", 35565), new InetSocketAddress("auron.co.uk", 35566), new InetSocketAddress("auron.co.uk", 35567), new InetSocketAddress("auron.co.uk", 53368));
		a.start();
	}
	
	public void start() {
		this.RUNNING = true;
		(thread = new Thread(this, "AWeSome Service Status Checker")).start();
	}
	
	public void stop() {
		this.RUNNING = false;
		thread.interrupt();
	}

	public ThreadedAWeSomeStatus(InetSocketAddress... addr) {
		this.addresses = addr;
	}

	public void run() {
		while (RUNNING && !Thread.interrupted()) {
			Set<String> servers = new HashSet<String>();
			for (InetSocketAddress add :this.addresses) {
				try {
					Query q = new Query(add);
					q.sendQuery();
					String[] players = q.getPlayers();
					Map<String, String> data = q.getData();
					String playersFinal = "";
					for (String p : players) // Generate online players list
						playersFinal = playersFinal+(playersFinal.length() > 0 ? "," : "")+p;
					String dataFinal = "";
					for (String k : data.keySet())
						dataFinal = dataFinal+(dataFinal.length() > 0 ? "\01" : "")+k+":"+data.get(k);
					servers.add(add.getAddress()+":"+add.getPort()+"\01"+dataFinal+(playersFinal.length() > 0 ? "\01players:"+playersFinal : ""));
				}
				catch (Exception e) {
					//e.printStackTrace();
					servers.add(add.getAddress()+":"+add.getPort()+"\01"+e.toString());
				}
			}
			writeToAPI(servers);
			try {
				Thread.sleep(updateDelay);
			} catch (InterruptedException e) {
				this.RUNNING = false;
			}
		}

	}

	public void writeToAPI(Set<String> data) {
		try {
			File file = new File("AweSomeAPIData.txt");
			FileWriter write = new FileWriter(file);
			for (String a : data)
				write.write(a+"\n");
			write.flush();
			write.close();

		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}


}
