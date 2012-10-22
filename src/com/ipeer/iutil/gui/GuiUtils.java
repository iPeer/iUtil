package com.ipeer.iutil.gui;

public class GuiUtils {

	public static String formatTime(long time) {
		time = System.currentTimeMillis() - time;
		int hours = (int)(time / 1000L) / 3600;
		int minutes = (int)((time / 1000L) % 3600) / 60;
		int seconds = (int)((time / 1000L) % 60);
		int days = (int)Math.floor(hours / 24);
		hours -= days*24;
		return days+" days, "+(hours < 10 ? "0"+hours : hours)+":"+(minutes < 10 ? "0"+minutes : minutes)+":"+(seconds < 10 ? "0"+seconds : seconds);
	}
	
}
