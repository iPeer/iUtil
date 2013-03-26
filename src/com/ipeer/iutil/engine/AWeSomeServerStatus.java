package com.ipeer.iutil.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.ipeer.minecraft.servers.ThreadedMCServerPoller;

public class AWeSomeServerStatus implements Runnable {

	private boolean IS_RUNNING = false;
	private Thread thread;
	private Engine engine;
	public static List<String> data;
	public long updateDelay = 60000;
	public static File logFile;
	private static RandomAccessFile logWriter;
	private Calendar calendar;

	public AWeSomeServerStatus(Engine engine) {
		this.engine = engine;
		this.calendar = Calendar.getInstance();
		File a = new File("logs/");
		if (!a.exists())
			a.mkdirs();
		logFile = new File(a, "AWeSomeStatus.log");
		try {
			logWriter = new RandomAccessFile(logFile, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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
			(new ThreadedMCServerPoller(this.engine, null, "ftb.auron.co.uk", 25568, true)).start();
			(new ThreadedMCServerPoller(this.engine, null, "e.auron.co.uk", 25567, true)).start();
			calendar.setTime(new Date(System.currentTimeMillis()));
			writeToLog("Updating AWeSome Status @ "+calendar.getTime());
			try {
				Thread.sleep(updateDelay);
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
			Iterator<String> it = data.iterator();
			while (it.hasNext()) {
				b.write(it.next()+"\n");
				it.remove();
			}
			b.close();
		} catch (IOException e) {
			try {
				Engine.engine.send("PRIVMSG #peer.dev :Unable to save servers' statuses to file! "+e.getMessage());
				//e.printStackTrace();
				writeToLog("IO Exception.");
				for (StackTraceElement a : e.getStackTrace())
					writeToLog("    "+a.toString());
			} 
			catch (IOException e1) { 
				writeToLog("IO Exception.");
				for (StackTraceElement a : e.getStackTrace())
					writeToLog("    "+a.toString());
			}
		}
		catch (ConcurrentModificationException e) { 
			writeToLog("Concurrent Modification.");
			for (StackTraceElement a : e.getStackTrace())
				writeToLog("    "+a.toString());
		}
		catch (Exception e) { 
			writeToLog("General Exception.");
			for (StackTraceElement a : e.getStackTrace())
				writeToLog("    "+a.toString());
		}
	}

	public static void writeToLog(String s) {
		try {
			logWriter.writeBytes(s+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean hasDataFor(String server) {
		for (Iterator<String> it = data.iterator(); it.hasNext();)
			if (it.next().contains(server))
				return true;
		return false;
	}

}
