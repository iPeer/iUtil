package com.ipeer.iutil.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;

import com.ipeer.iutil.engine.Debug;
import com.ipeer.iutil.engine.Engine;

public class KeyboardHandler implements KeyListener {
	
	public class Key {
		
		public boolean down, pressed, pressed1;
		public int presses, multi;
		
		public Key() {
			keys.add(this);
		}

		public void toggle(boolean flag) {
			if (down != flag)
				down = flag;
			if (flag)
				presses++;
		}

		public void tick() {
			if (multi < presses) {
				multi++;
				pressed1 = true;
			} else {
				pressed1 = false;
			}
		}
	}
	
	public ArrayList<Key> keys;
	public Key left;
	public Key right;
	public Key up;
	public Key down;
	public Key jump;
	public Key debug;
	public Key rendering;
	public Key quit;
	public Key pause;
	public Key newLevel;
	public Key screenshot;
	public Key console;
	public Key centerOnPlayer;
	public Key centerOnMarker;
	
	protected Engine engine;
	protected GuiEngine guiengine;

	public KeyboardHandler(Engine engine, GuiEngine guiengine) {
		this.engine = engine;
		this.guiengine = guiengine;
		
		keys = new ArrayList<Key>();
		
		left = new Key();
		right = new Key();
		up = new Key();
		down = new Key();
		
		pause = new Key();
		
		centerOnPlayer = new Key();
		centerOnMarker = new Key();
		
		newLevel = new Key();
		
		debug = new Key();
		quit = new Key();
		
		screenshot = new Key();
		
		console = new Key();
		
		guiengine.addKeyListener(this);
		Debug.err.println("[GUIENGINE] Added Key listener.");
	}

	
	public void tick() {
		for (int x = 0; x < keys.size(); x++) {
			keys.get(x).tick();
		}
	}
	
	public void releaseAll() {
		for (int x = 0; x < keys.size(); x++) {
			keys.get(x).down = false;
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		try {
			guiengine.gui.keyTyped(arg0.getKeyChar(), arg0.getKeyCode());
		}
		catch (NullPointerException n) {
			toggle(arg0, true);
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		toggle(arg0, false);
	}

	@Override
	public void keyTyped(KeyEvent arg0) {

	}
	
	private void toggle(KeyEvent e, boolean flag) {
		int k = e.getKeyCode();
		if (Arrays.asList(Keyboard.VK_Q, Keyboard.VK_ESCAPE).contains(k))
			quit.toggle(flag);
		if (k == Keyboard.VK_F3)
			debug.toggle(flag);
		
		if (k == Keyboard.VK_ESCAPE)
			pause.toggle(flag);
		
		if (k == Keyboard.VK_P)
			centerOnPlayer.toggle(flag);
		
		if (k == Keyboard.VK_M)
			centerOnMarker.toggle(flag);
		
		if (k == Keyboard.VK_R)
			newLevel.toggle(flag);
		
		if (k == Keyboard.VK_F2)
			screenshot.toggle(flag);
		
		if (Arrays.asList(Keyboard.VK_BACK_QUOTE, Keyboard.VK_DEAD_TILDE).contains(k))
			console.toggle(flag);
		
		if (Arrays.asList(Keyboard.VK_UP, Keyboard.VK_W, Keyboard.VK_NUMPAD8).contains(k))
			up.toggle(flag);
		if (Arrays.asList(Keyboard.VK_RIGHT, Keyboard.VK_D, Keyboard.VK_NUMPAD6).contains(k))
			right.toggle(flag);
		if (Arrays.asList(Keyboard.VK_LEFT, Keyboard.VK_A, Keyboard.VK_NUMPAD4).contains(k))
			left.toggle(flag);
		if (Arrays.asList(Keyboard.VK_DOWN, Keyboard.VK_S, Keyboard.VK_NUMPAD2).contains(k))
			down.toggle(flag);
	}

}
