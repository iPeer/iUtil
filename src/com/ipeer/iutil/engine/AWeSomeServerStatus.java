package com.ipeer.iutil.engine;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

import com.ipeer.iutil.remote.server.CannotSavePasswordException;
import com.ipeer.minecraft.servers.ThreadedMCServerPoller;

public class AWeSomeServerStatus implements Runnable {

	private boolean IS_RUNNING = false;
	private Thread thread;
	private Engine engine;
	public static List<String> data;

	public AWeSomeServerStatus(Engine engine) {
		this.engine = engine;
	}

	public static void main(String[] args) {
		AWeSomeServerStatus a = new AWeSomeServerStatus(null);
		a.start();
	}

	public void start() {
		this.IS_RUNNING = true;
		(thread = new Thread(this, "iUtil's AWeSome Server Status Checker")).start();
	}

	public void stop() {
		this.IS_RUNNING = false;
		thread.interrupt();
	}

	@Override
	public void run() {
		data = new ArrayList<String>();
		while (IS_RUNNING && !Thread.interrupted()) {
			(new ThreadedMCServerPoller(this.engine, null, "s.auron.co.uk", 25565, true)).start();
			(new ThreadedMCServerPoller(this.engine, null, "c.auron.co.uk", 25566, true)).start();
			(new ThreadedMCServerPoller(this.engine, null, "e.auron.co.uk", 25567, true)).start();
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				this.IS_RUNNING = false;
			}
		}
	}

	public static void writeDataToFile() {
		try {
			File a = new File("AWeSomeStatus.txt");
			FileWriter b = new FileWriter(a);
			for (String d : data)
				b.write(d+"\n");
			b.close();
			data.clear();
		} catch (IOException e) {
			try {
				Engine.engine.send("PRIVMSG #peer.dev :Unable to save servers' statuses to file! "+e.getMessage());
				e.printStackTrace();
			} 
			catch (IOException e1) {	}
		}
		catch (ConcurrentModificationException e) { }
	}

}
