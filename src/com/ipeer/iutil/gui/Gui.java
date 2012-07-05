package com.ipeer.iutil.gui;

import java.util.ArrayList;

import com.ipeer.iutil.engine.Engine;

public class Gui {

	protected Engine engine;
	protected GuiEngine guiengine;
	public ArrayList controls;
	public String title;
	public int w, h;
	public boolean isVisible;

	@SuppressWarnings("rawtypes")
	public Gui(Engine engine, GuiEngine guiengine) {
		controls = new ArrayList();
		title = "Some GUI";
		this.engine = engine;
		this.guiengine = guiengine;
		this.w = guiengine.getWidth();
		this.h = guiengine.getHeight();
	}

	public void render() {
		for (int c = 0; c < controls.size(); c++) {
			((Gui)controls.get(c)).render();
		}
	}

	public void tick() { 
		for (int c = 0; c < controls.size(); c++) {
			((Gui)controls.get(c)).tick();
		}
	}

	public void keyTyped(char keyChar, int keyCode) {
		
	}
	
	public void addTextHistory(String t) { }
	
	public void toggleVisible() {
		this.isVisible = !isVisible;
	}
	
}
