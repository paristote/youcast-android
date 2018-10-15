package com.philipoy.youtubedl.rest;

public class Video {

	public String username;
	public String id;
	public int downloadCount;
	public String title;
	public int status;
	
	public static class Status {
		
		public static final int REGISTERED = 0;
		public static final int SCHEDULED = 1;
		public static final int STARTED = 2;
		public static final int DONE = 3;
		public static final int FAILED = 10;
		
	}
	
}
