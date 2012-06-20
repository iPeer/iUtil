package com.ipeer.iutil.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class IdentdServer implements Runnable {

	protected Engine engine;

	public IdentdServer(Engine engine){
		this.engine = engine;
	}

	@Override
	public void run()  {
		try {
			engine.writeToLog("-> Starting Identd server...");
			ServerSocket a = new ServerSocket(113);
			Socket b = a.accept();

			BufferedReader in = new BufferedReader(new InputStreamReader(b.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(b.getOutputStream()));

			String line = in.readLine();

			if (line != null) {
				System.err.println("IDENTD Request: "+line);

				line = line+" : USERID : UNIX : "+engine.MY_NICK;
				engine.writeToLog("-> IDENTD: "+line);
				out.write(line+"\r\n");
				out.flush();
			}

			in.close();
			out.close();
			a.close();
			b.close();
		}
		catch (Exception e) { e.printStackTrace(); }

	}

	public void start() {
		(new Thread(this)).start();
	}

}
