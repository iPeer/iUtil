package com.ipeer.iutil.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Convert {

	public static File logout, login;
	public static FileWriter fw;

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.err.println("Must supply 2 arguments: <file to convert> <output file>");
			System.exit(0);
		}
		logout = new File(args[1]);
		login = new File(args[0]);
		if (!login.exists()) {
			System.err.println("Source fil does not exist!");
			System.exit(0);
		}
		convert(login, logout);
	}

	private static void convert(File login2, File logout2) throws IOException {
		System.out.println("Converting...");
		long now = System.currentTimeMillis();
		fw = new FileWriter(logout, true);
		BufferedReader a = new BufferedReader(new FileReader(login));
		String line = null;
		while ((line = a.readLine()) != null) {
			parseLine(line);
		}
		fw.close();
		System.out.println("Done in "+(System.currentTimeMillis() - now)+"ms");
	}

	private static void parseLine(String line) throws IOException {
		//System.err.println(line);
		if (line.contains("lost connection:")) {
			String u = line.split(" ")[3];
			writeToLog(line.split(" ")[0]+" "+line.split(" ")[1]+" "+u+" disconnected.");
		}

		else if (line.contains("logged in with entity id")) {
			String u = line.split(" ")[3].replaceAll("\\[.*\\]", "");
			writeToLog(line.split(" ")[0]+" "+line.split(" ")[1]+" "+u+" connected.");
		}
		else if (line.contains("[INFO] <") || line.matches(".*\\[37m.*\\[37m:.*\\[0m")) {
			if (line.matches(".*\\[37m.*\\[37m:.*\\[0m")) {
				//System.err.println(line);
				line = line.replaceAll("(\\[37m|\\[0m)", "");
				String[] data = line.split(" ");
				String u = data[3].replaceFirst(":", "").replaceAll("\\[31m", "");
				String message = line.split(": ")[1].replaceAll("(\\[31m|\\[36m)", "");
				if (!message.startsWith("P "))
					writeToLog(data[0]+" "+data[1]+" "+u+": "+message);
			}
			else {
				String[] data = line.split(" ");
				String u = data[3].replaceAll("(<|>|\\[31m)", "");
				String message = data[4];
				for (int x = 5; x < data.length; x++)
					message = message+" "+data[x];
				if (!message.startsWith("P ")) {
					writeToLog(data[0]+" "+data[1]+" "+u+": "+message.replaceAll("\\[0m", ""));
				}
			}
		}
		//		else
		//			writeToLog(line);
	}

	public static void writeToLog(String s) throws IOException {
		//RandomAccessFile a = new RandomAccessFile(new File(logout,"chat_"+c.toLowerCase()+".log"), "rw");
		fw.write(s+"\r\n");
	}

}
