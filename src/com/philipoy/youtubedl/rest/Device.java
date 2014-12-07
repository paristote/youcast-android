package com.philipoy.youtubedl.rest;

public class Device {
	public String id;
	public String name;
	public String username;
	public String connected;
	public String type;
	
	public boolean isConnected() {
		return "true".equalsIgnoreCase(connected);
	}
	
	public void setConnected(boolean conn) {
		if (conn) connected = "true";
		else      connected = "false";
	}
	
	public void setType(DeviceType t) {
		switch (t) {
		case ANDROID:
			type = "android";
			break;
		case IOS:
			type = "ios";
			break;
		}
	}
	
	public static enum DeviceType {
		ANDROID, IOS
	}
}
