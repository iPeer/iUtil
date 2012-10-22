package com.ipeer.iutil.youtube;

public class Upload {

	public String title, length, VID;
	
	public Upload(String title, String length, String VID) {
		this.title = title;
		this.length = length;
		this.VID = VID;
	}
	
	@Override
	public String toString() {
		return this.title+", "+this.length+", "+this.VID;
	}
	
}
