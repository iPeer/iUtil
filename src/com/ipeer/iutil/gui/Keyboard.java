package com.ipeer.iutil.gui;

import java.awt.Component;
import java.awt.event.KeyEvent;

public class Keyboard extends KeyEvent {

	private static final long serialVersionUID = -2669482104921708121L;

	public Keyboard(Component arg0, int arg1, long arg2, int arg3, int arg4,
			char arg5) {
		super(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public Keyboard(Component arg0, int arg1, long arg2, int arg3, int arg4,
			char arg5, int arg6) {
		super(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}

}
