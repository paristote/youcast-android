package com.philipoy.youtubedl;

import android.app.Application;

public class App extends Application {

	public static String LOG_TAG = "YouCast";
	
	private static String PROD_SERVER_URL = "https://youcast.cleverapps.io";
	private static String LOCAL_SERVER_URL = "http://192.168.1.75";
	public static  String YOUCAST_SERVER_URL = PROD_SERVER_URL;
	
	
	public static int SYNC_FREQUENCY = 60; // 60 seconds
//	public static int SYNC_FREQUENCY = 300; // 300 sec = 5 min
	
	public App() {
	}

}
