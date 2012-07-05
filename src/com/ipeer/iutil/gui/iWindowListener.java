package com.ipeer.iutil.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import com.ipeer.iutil.engine.Debug;

public class iWindowListener implements WindowListener {

	protected GuiEngine engine;
	
	public iWindowListener(GuiEngine engine) {
		this.engine = engine;
	}
	
	@Override
	public void windowActivated(WindowEvent arg0) {

	}

	@Override
	public void windowClosed(WindowEvent arg0) {

	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		Debug.err.println("[GUIENGINE] Window closing!");
		if (!engine.botengine.isConnected)
			System.exit(0);
		else
			engine.toggleVisible();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {

	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {

	}

	@Override
	public void windowIconified(WindowEvent arg0) {

	}

	@Override
	public void windowOpened(WindowEvent arg0) {

	}

}
