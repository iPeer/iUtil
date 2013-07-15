package com.ipeer.iutil.youtube;

public class Upload {

	public String title, length, VID, removedBy;
	public boolean removed = false;
	
	public Upload(String title, String length, String VID, boolean removed, String removedBy) {
		this.title = title;
		this.length = length;
		this.VID = VID;
		this.removed = removed;
		this.removedBy = removedBy;
	}
	
	public Upload(String title, String length, String VID) {
		this(title, length, VID, false, "null");
	}

	@Override
	public String toString() {
		return this.title+", "+this.length+", "+this.VID;
	}
	
	public void setRemoved(boolean removed, String removedBy) {
		this.removed = removed;
		this.removedBy = removedBy;
	}
	
}
