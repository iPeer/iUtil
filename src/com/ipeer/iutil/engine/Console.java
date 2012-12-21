package com.ipeer.iutil.engine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import com.ipeer.iutil.youtube.YouTube;

public class Console implements Runnable {

	boolean IS_RUNNING = false;
	protected Engine engine;

	public Console(Engine engine) {
		this.engine = engine;
	}

	public void start() {
		this.IS_RUNNING = true;
		(new Thread(this, "iUtil Console Input Listener")).start();
	}

	@Override
	public void run() {
		BufferedReader a = new BufferedReader(new InputStreamReader(System.in));
		System.err.println("Console instance started.");
		while (IS_RUNNING) {
			try {

				String d = a.readLine();
				String b = d.split(" ")[0].toLowerCase();
				String c = "";

				try {
					c = d.split(" ")[1];
				}
				catch (Exception e) {	}

				if (engine.serverEnabled())
					engine.getServer().sendToAllAdminClients("[CONSOLE INPUT] "+d);
				
				if (b.matches("stop|exit|quit|[q]{1,3}")) {
					engine.quit("STOP command from console.");
					engine.requestedQuit = true;
					System.exit(0);
				}
				if (b.equals("restart")) {
					engine.quit("RESTART command from console.");
					engine.requestedQuit = true;
					System.exit(0);
				}
				if (b.equals("forceupdate"))
					Engine.YouTube.updateChannel();
				if (b.equals("msg")) {
					try {
						String e = d.split(" ")[1];
						String[] f = d.split(" ");
						String g = f[2];
						for (int x = 3; x < f.length; x++) {
							g = g+" "+f[x];
						}
						if (engine == null)
							System.err.println(e+", "+g);
						else
							engine.send("PRIVMSG "+e+" :"+g);
					}
					catch (ArrayIndexOutOfBoundsException e) { }
				}

				if (b.equals("savecache")) {
					YouTube.cache.saveToFile();
				}

				if (b.equals("var")) {
					try {
						Class x = Class.forName(d.split(" ")[1]);
						Field field = x.getDeclaredField(d.split(" ")[2]);
						field.setAccessible(true);
						System.err.println(field.getType());
						System.err.println(">>> "+(Object)field.get(x));
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (b.equals("dumpstats")) {
					long totalMemory = Runtime.getRuntime().totalMemory();
					long freeMemory = Runtime.getRuntime().freeMemory();
					long usedMemory = totalMemory - freeMemory;
					String memory = (usedMemory / 1024L / 1024L)+"MB/"+(totalMemory / 1024L / 1024L)+"MB";
					System.out.println("M: "+memory);

					ThreadGroup root = Thread.currentThread().getThreadGroup();
					ThreadGroup parent = root.getParent();
					listThreads(parent, "");
				}
				if (b.equals("generatecache")) {
					YouTube.cache.generateFile();
				}
				if (b.equals("updatechannel")) {
					try {
						String channel = d.split(" ")[1].toLowerCase();
						System.err.println(channel);
						Engine.YouTube.updateChannel(channel);
					}
					catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException e) { e.printStackTrace(); Engine.YouTube.updateChannel(); }
				}
				if (b.matches("shell:?")) {
					if (d.split(" ").length == 1) {
						System.out.println("Command format:\nshell: <command>|arg1|arg2|...");
					}
					else {
						String[] f = d.split("\\|");
						String cmd = f[0].split(": ")[1];
						String g = f[1];
						String[] out = new String[f.length];
						out[0] = cmd;
						for (int x = 1; x < f.length; x++)
							out[x] = f[x];
						//						for (String j : out)
						//							System.err.println(j);
						Engine.Shell.sendBasicCommand(out);
					}
				}
				if (b.equals("test"))
					System.out.println("TEST COMMAND!");

				Thread.sleep(10);
			} catch (Exception e) {
				e.printStackTrace();
				engine.restartConsole();
			}
		}
	}

	private void listThreads(ThreadGroup parent, String i) {
		try {
			System.out.println(i + "Group[" + parent.getName() + ":" + parent.getClass()+"]");
			int a = parent.activeCount();
			Thread[] b = new Thread[a*2 + 10];
			a = parent.enumerate(b, false);

			for (int x = 0; x < a; x++) {
				Thread t = b[x];
				System.out.println(i+" Thread["+t.getName()+" : "+t.getClass()+"]");
			}

			int c = parent.activeGroupCount();
			ThreadGroup[] g = new ThreadGroup[c*2 + 10];
			c = parent.enumerate(g, false);

			for (int x = 0; x < 5; x++) {
				listThreads(g[x], i+" ");
			}
		}
		catch (NullPointerException e) { }

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Console a = new Console(null);
		a.start();
	}

}
