package com.ipeer.iutil.engine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlTest {

	public static void main(String[] args) {
		String URL = "https://reddit.com";
		System.err.println(getURL(URL));
	}
	
	public static String getURL(String url) {
		Pattern url1 = Pattern.compile("((https?://)?.*(?=[/$])?)", Pattern.CASE_INSENSITIVE);
		Matcher m = url1.matcher(url);
		while (m.find()) {
			return m.group();
		}
		return "No match";
	}

}
