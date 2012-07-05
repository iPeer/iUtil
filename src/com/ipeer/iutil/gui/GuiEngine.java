package com.ipeer.iutil.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.io.PrintStream;

import com.ipeer.iutil.engine.Colour;
import com.ipeer.iutil.engine.Engine;


public class GuiEngine extends Canvas implements Runnable {

	private static final String GAME_TITLE = "iUtil Main Gui";
	private static final int BUFFER_LEVEL = 2;
	private static final int HEIGHT_RATIO = 9;
	private static final int WIDTH_RATIO = 16;
	private static int GAME_WIDTH = 1024;
	private static int GAME_HEIGHT = GAME_WIDTH * HEIGHT_RATIO / WIDTH_RATIO;
	private static final int IDLE_FRAME_LIMIT = 60;
	private static final double TICKS_PER_SECOND = 60.0;
	private static Frame frame;

	private int lastFrames, lastTicks;
	private KeyboardHandler input;

	public boolean IS_RUNNING = false;
	public Graphics2D g;
	public Gui gui;
	public boolean isVisible = true;

	public static final String allowedCharacters = "abcdefghijklmnopqrstuvwxyz1234567890.+-*/`¬¦!\"£$€%^&*()_{}[]@~'#<>?,/\\;:";
	public static GuiEngine engine;
	public static final PrintStream debug = System.out;
	public static final PrintStream err = System.err;

	protected Engine botengine;


	public GuiEngine(Engine bengine) {
		this.botengine = bengine;
		engine = this;
		if (!System.getProperty("os.name").equals("Linux")) {
			Canvas a = new Canvas();
			frame = new Frame(GAME_TITLE);
			engine.setPreferredSize(new Dimension(GAME_WIDTH - 10, GAME_HEIGHT - 10));
			frame.setLayout(new BorderLayout());
			frame.addWindowListener(new iWindowListener(engine));
			frame.add(a);
			err.println("[GUIENGINE] Added Window listener.");
			engine.addMouseListener(new iMouseListener(engine));
			err.println("[GUIENGINE] Added Mouse listener.");
			engine.addMouseMotionListener(new iMouseMotionListener(engine));
			err.println("[GUIENGINE] Added Mouse Motion listener.");
			engine.addComponentListener(new iComponentListener(engine));
			err.println("[GUIENGINE] Added Component listener.");
			frame.add(engine, "Center");
			frame.pack();
			frame.setResizable(false);
			frame.setVisible(true);
			frame.setLocationRelativeTo(null);
			engine.requestFocus();
		}
		engine.start();
	}

	public void start() {
		try {
			(new Thread(this, "iUtil Main GUI Thread")).start();
			IS_RUNNING = true;
		}
		catch (Exception e) {
			err.println("[GUIENGINE] Unable to start Engine Thread!");
			System.exit(0);
		}
	}

	public void setGui(Gui g) {
		this.gui = g;
	}

	public void stop() {
		IS_RUNNING = false;
		engine.stop();
	}

	public void init() {
		if (!System.getProperty("os.name").equals("Linux")) {
			input = new KeyboardHandler(botengine, this);
			setGui(new GuiMain(botengine, this));
		}
	}

	public void run() {
		try {
			int ticks = 0;
			int frames = 0;
			long lastTime = System.nanoTime();
			double processQueue = 0.0;
			double ticksPerLoop = 1000000000 / TICKS_PER_SECOND;
			long lastTick = System.currentTimeMillis();
			init();
			while (IS_RUNNING) {
				long now = System.nanoTime();
				processQueue += (double)(now - lastTime) / ticksPerLoop;
				lastTime = now;
				if (processQueue >= 1.0) { // Tick
					if (processQueue > 61.0)
						processQueue = 60.0;
					ticks++;
					if (isVisible && botengine.runGUI)
						tick();
					processQueue--;
				}
				try {
					Thread.sleep(1000 / IDLE_FRAME_LIMIT);
				} 
				catch (InterruptedException e) {
					err.println("[GUIENGINE] Unable to enforece idle frame limit");
					e.printStackTrace();
				}
				if (isVisible && botengine.runGUI) {
					render();
					frames++;
				}
				if (System.currentTimeMillis() - lastTick > 1000L) {
					//System.out.println(frames+" fps, "+ticks+" ticks");
					lastFrames = frames;
					lastTicks = ticks;
					frames = ticks = 0;
					lastTick = System.currentTimeMillis();
				}
			}
			System.exit(0);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void render() {

		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(BUFFER_LEVEL);
			requestFocus();
			return;
		}

		g = (Graphics2D)bs.getDrawGraphics();

		g.setColor(Colour.GRAY);
		g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

		if (this.gui != null)
			gui.render();

		g.dispose();
		bs.show();

	}
	public void tick() {
		if (this.gui != null) {
			gui.tick();
		}
	}

	public void toggleVisible() {
		System.err.println("[GUIENGINE] Visiblity: "+!isVisible);
		this.isVisible = !isVisible;
		frame.setVisible(isVisible);
	}


}
