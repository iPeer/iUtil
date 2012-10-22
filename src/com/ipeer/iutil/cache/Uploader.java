package com.ipeer.iutil.cache;

import java.util.HashMap;
import java.util.Map;

public class Uploader {
	
	public Map<String, String> URLs;
	public String username = "";

	public Uploader (String user) { 
		this.username = user;
		this.URLs = new HashMap<String, String>();
	}
	
	public void addVideo(String title, String url) {
		this.URLs.put(title, url);
	}
	
	public void clear() {
		this.URLs.clear();
	}
	
	public boolean isCached(String title) {
		return URLs.containsKey(title);
	}
	
}
