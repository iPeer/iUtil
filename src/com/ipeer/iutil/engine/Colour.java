package com.ipeer.iutil.engine;

import java.awt.Color;
import java.awt.color.ColorSpace;

public class Colour extends Color {

	private static final long serialVersionUID = 4776646632985288080L;

	public Colour(int arg0) {
		super(arg0);
	}

	public Colour(int arg0, boolean arg1) {
		super(arg0, arg1);
	}

	public Colour(int arg0, int arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

	public Colour(float arg0, float arg1, float arg2) {
		super(arg0, arg1, arg2);
	}

	public Colour(ColorSpace arg0, float[] arg1, float arg2) {
		super(arg0, arg1, arg2);
	}

	public Colour(int arg0, int arg1, int arg2, int arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public Colour(float arg0, float arg1, float arg2, float arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
