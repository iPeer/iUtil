package com.ipeer.iutil.gui;

import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ipeer.iutil.engine.Colour;
import com.ipeer.iutil.engine.Engine;

public class GuiMain extends Gui {

	private String text = "";
	private int ticks;
	private int caretPos;
	private List<String> inputHistory = new ArrayList<String>();
	private int historyPoint;
	public List<String> textHistory = new ArrayList<String>();
	private Console console;
	private String channel = "";

	public GuiMain(Engine engine, GuiEngine guiengine) {
		super(engine, guiengine);
		console = System.console();
	}

	public void tick() {
		ticks++;
		super.tick();
	}

	public void render() { 
		Graphics2D g = guiengine.g;

		g.setColor(Colour.BLACK);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
		g.fillRect(10, 190, w - 20, 350);
		g.fillRect(10, 545, w - 20, 20);
		g.fillRoundRect(10, 10, 280, 170, 10, 10);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		g.setColor(Colour.WHITE);
		Font b = g.getFont();
		g.drawString("Server: "+engine.server+" ("+(engine.ACTUAL_SERVER.equals("") ?  "..." : engine.ACTUAL_SERVER)+")", 15, 30);
		g.drawString("Port: "+engine.port, 15, 45);
		g.drawString("SSL: "+engine.SSL, 15, 60);
		g.drawString("Nick: "+engine.MY_NICK, 15, 75);
		g.drawString("Channels: "+Engine.channels.size(), 15, 90);
		String uptime = "", uptime2 = "";
		try {
			uptime = (!engine.isConnected ? "---" : Utils.formatTime(engine.botStart));
			uptime2 = (!engine.isConnected ? "---" : Utils.formatTime(engine.connectionStart));
		}
		catch (NullPointerException e) { 
			uptime = uptime2 = "-"; 
		}
		g.drawString("Bot: "+uptime, 15, 120);
		g.drawString("Connection: "+uptime2, 15, 135);
		long totalMemory = Runtime.getRuntime().totalMemory();
		long freeMemory = Runtime.getRuntime().freeMemory();
		long usedMemory = totalMemory - freeMemory;
		String memory = (usedMemory / 1024L / 1024L)+"MB/"+(totalMemory / 1024L / 1024L)+"MB";
		g.drawString("Memory: "+memory, 15, 165);

		int l = textHistory.size() - 1;
		int pos = 537;
		for (int x = 0; x < 27 && x < l-1; x++) {
			String a = textHistory.get(l-x);

			if (a.startsWith("<-")) {
				g.setFont(new Font(b.getFontName(), Font.ITALIC, b.getSize()));
				g.drawString(a, 12, pos);
				pos -= 13;
				g.setFont(b);
			}
			else {
				if (a.contains("Closing Link") || a.startsWith("!")) {
					if (a.startsWith("!"))
						a = a.substring(2);
					g.setColor(Colour.RED);
				}
				else if (a.startsWith("?")) {
					a = a.substring(2);
					g.setColor(Colour.GREEN);
				}
				else {
					g.setColor(Colour.WHITE);
				}
				g.drawString(a, 12, pos);
				pos -= 13;
			}
		}
		boolean f = (ticks / 30) % 2 == 0;
		int y = 558;
		if (caretPos == text.length())
			g.drawString(text+(f ? "|" : ""), 12, y);
		else if (caretPos > 0 && caretPos < text.length() + 1) {
			String a = "";
			String b1 = a;
			a = text.substring(0, caretPos);
			b1 = text.substring(caretPos, text.length());
			g.drawString(a+(f ? "|" : "")+b1, 12, y);
		}
		else {
			g.drawString((f ? "|" : "")+text, 12, y);
		}

		super.render();
	}

	public void keyTyped(char keyChar, int keyCode) {
		if (keyCode == Keyboard.VK_BACK_SPACE && text.length() > 0) {
			caretPos--;
			setText(text.substring(0, text.length() - 1));
		}

		else if(keyCode == Keyboard.VK_DELETE && caretPos > 0 && text.length() > 0) {
			String a = "";
			String b = a;
			a = text.substring(0, caretPos);
			b = text.substring(caretPos + 1, text.length());
			setText(a+b);
		}

		else if (keyCode == Keyboard.VK_SPACE || GuiEngine.allowedCharacters.contains(Character.toString(keyChar).toLowerCase()) || Arrays.asList(0x03, 0x02, 0x01F, 0x1D, 0x16, 0xF0).contains(keyChar)) {
			if (caretPos < text.length()) {
				String a = "";
				String b = a;
				a = text.substring(0, caretPos);
				b = text.substring(caretPos, text.length());
				setText(a+keyChar+b);
			}
			else
				setText(text+keyChar);
			caretPos++;
		}

		else if (keyCode == Keyboard.VK_LEFT && caretPos > 0) {
			caretPos--;
		}
		else if (keyCode == Keyboard.VK_RIGHT && caretPos < (text.length() + 1)) {
			caretPos++;
		}

		else if (keyCode == Keyboard.VK_UP && inputHistory.size() > 0 && historyPoint > 0) {
			historyPoint--;
			String t = inputHistory.get(historyPoint);
			setText(t);
			caretPos = t.length();
		}

		else if (keyCode == Keyboard.VK_DOWN) {
			historyPoint++;
			String t = "";
			if (historyPoint < inputHistory.size())
				t = inputHistory.get(historyPoint);
			else {
				historyPoint = inputHistory.size();
				t = "";
			}
			setText(t);
			caretPos = t.length();
		}

		else if (keyCode == Keyboard.VK_ENTER) {
			try {
				inputHistory.add(text);
				historyPoint = inputHistory.size();
				if (text.startsWith("/"))
					parseCommand(text);
				else
					parseText(text);
				setText("");
				caretPos = 0;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void parseCommand(String t) throws IOException {
		String command = t.substring(1).split(" ")[0];
		if (engine.connection != null) {
			if (command.equals("quit")) {
				engine.send("QUIT :Requested disconnect from GUI");
			}

			else if (command.equals("msg")) {
				String channel1 = t.split(" ")[1];
				String[] messageData = t.split(" ");
				String message = messageData[2];
				for (int x = 3; x < messageData.length; x++) {
					message = message+" "+messageData[x];
				}
				engine.send("PRIVMSG "+(!channel.equals("") ? channel : channel1)+" :"+message);
			}
			else if (command.equals("setchannel")) {
				this.channel = t.split(" ")[1];
				addTextHistory("? Channel set to "+channel);
			}
			else {
				addTextHistory("! Unknown command '"+command+"'");
			}

		}
	}
	public void parseText(String t) throws IOException {
		if (!channel.equals(""))
			engine.send("PRIVMSG "+channel+" :"+t);
	}

	public void setText(String t) {
		this.text = t;
	}

	public static void main(String[] args) {
		GuiEngine e = new GuiEngine(Engine.engine);
	}

	public void addTextHistory(String t) { 
		t = t.replaceAll("("+Engine.colour+"([0-9]{1,2}(\\,[0-9]{1,2})?)?|"+Engine.bold+"|"+Engine.underline+"|"+Engine.reverse+"|"+Engine.underline+"|"+Engine.italics+")", "");
		if (t.length() > 175) {
			String a = t.substring(0, 175);
			textHistory.add(a+"...");
		}	
		else
			textHistory.add(t);
	}
}
