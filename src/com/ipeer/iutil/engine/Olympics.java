package com.ipeer.iutil.engine;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

public class Olympics implements Runnable {

	public boolean isRunning = true;
	protected Engine engine;
	public String r = ".*<span class=\"countryName\">.*+</span></a></div></div></td><td class=\"gold c\">[0-9]+</td><td class=\"silver c\">[0-9]+</td><td class=\"bronze c\">[0-9]+</td>";
	public long updateAt = 0;
	public long updateDelay = 10800000;
	public boolean manualUpdate = false;

	public Olympics(Engine e) {
		this.engine = e;
	}

	public static void main(String[] args) { 
		Olympics a = new Olympics(null);
		a.start();
	}

	public void stop() {
		isRunning = false;
	}

	public void start() {
		isRunning = true;
		(new Thread(this, "Olympic Medal Table Announcer")).start();
	}

	@Override
	public void run() {
		//System.err.println("Running: "+isRunning);
		while (isRunning) {
			try {
				System.err.println("Updating Olympic Medal Table...");
				URL url = new URL("http://www.london2012.com/paralympics/medals/medal-count/?fr=no");
				DataInputStream a = new DataInputStream(url.openStream());
				Scanner s = new Scanner(a, "UTF-8");
				int top = 5;
				int tot = top;
				String out = c1("[")+c2("LONDON 2012 PARALYMPICS")+c1("]");
				while (s.hasNextLine() && top > 0) {
					String l = s.nextLine();
					if (/*l.matches(r)*/l.contains("<span class=\"countryName\">")) {
						top--;
						String country = l.split("<span class=\"countryName\">")[1].split("</span></a></div></div></td><td class=\"gold c\">")[0];
						String code = l.split("src=\"/imgml/flags/s/")[1].split(".png")[0];
						int g = Integer.parseInt(l.split("<td class=\"gold c\">")[1].split("</td><td class=\"silver c\">")[0]);
						int s1 = Integer.parseInt(l.split("<td class=\"silver c\">")[1].split("</td><td class=\"bronze c\">")[0]);
						int b = Integer.parseInt(l.split("<td class=\"bronze c\">")[1].split("</td><td class=\"all_medals\">")[0]);
						//System.err.println(country+", "+code+", "+g+", "+s1+", "+b);
						out = out+(top <= (tot - 2) ? c1(" | ") : " ")+c2((country.length() > 15 ? code : country))+c1(" (")+c2(g+s1+b)+c1(") G: ")+c2(g)+c1(" S: ")+c2(s1)+c1(" B: ")+c2(b);
					}

				}

				//System.err.println(out);

				if (engine != null) 
					engine.send("PRIVMSG #QuestHelp :"+out);
				else
					System.err.println(out);

				//System.err.println("Done reading.");
				this.updateAt = System.currentTimeMillis();
				System.err.println("Finished Updating Olympic Medal Table.");
				if (checkDate())
					Thread.sleep(updateDelay);
				else
					isRunning = false;

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public String c1(String s) {
		return Engine.colour+"14"+s;
	}
	public String c2(String s) {
		return Engine.colour+"13"+s;
	}
	public String c2(int i) {
		return c2(Integer.toString(i));
	}

	private boolean checkDate() {
		Calendar calendar = Calendar.getInstance();
		if (calendar.getTime() != new Date())
			calendar.setTime(new Date());
		int a = calendar.get(5);
		int b = calendar.get(2) + 1;
		int d = Integer.parseInt(Integer.toString(a)+Integer.toString(b));
		int e = calendar.get(1);
		return (d < 99 && e == 2012);
	}

}
