package com.ipeer.iutil.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Shell {

	public static void main(String[] args) {

		Shell s = new Shell();
		String[] a = {"F:\\mIRC - iPeer\\a.bat"};
		s.sendBasicCommand(a);

	}

	public Shell() {

	}
	
	public void sendBasicCommand(String[] args) {
		sendBasicCommand(args, "");
	}

	public void sendBasicCommand(String[] args, String dir) {
		if (args.length == 0) {
			System.err.println("No command to send!");
			return;
		}
//		System.err.println(args.length);
//		for (String e : args) {
//			System.err.println(e);
//		}
		try {
			ProcessBuilder a = new ProcessBuilder(args);
			if (!dir.equals(""))
				a.directory(new File(dir));
			Process b = a.start();
			BufferedReader c = new BufferedReader(new InputStreamReader(b.getInputStream()));
			String line = null;
			while ((line = c.readLine()) != null) {
				System.err.println("SHELL: "+line);
			}
			b.destroy();
		} 
		catch (IOException e) {
			System.err.println("Unable to run process:");
			e.printStackTrace();
		}

	}
	
	

	public void relayIRCChat(String string) {
		String[] a = {"/home/minecraft/iPeer.sh", "sendIRCToServers", string};
		sendBasicCommand(a);
	}

}
