package com.ipeer.iutil.gui;

public class Utils {

	public static String formatTime(long time) {
		time = System.currentTimeMillis() - time;
		int hours = (int)(time / 1000L) / 3600;
		int minutes = (int)((time / 1000L) % 3600) / 60;
		int seconds = (int)((time / 1000L) % 60);
		return (hours < 10 ? "0"+hours : hours)+":"+(minutes < 10 ? "0"+minutes : minutes)+":"+(seconds < 10 ? "0"+seconds : seconds);
	}
	
}
