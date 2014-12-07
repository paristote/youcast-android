package com.philipoy.youtubedl.utils;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

public class CredentialsManager {
	/**
	 * @see CredentialsManager.getCredentials()
	 */
	public static final String USERNAME = "USER";
	/**
	 * @see CredentialsManager.getCredentials()
	 */
	public static final String PASSWORD = "PASS";
	private final String KEY_USER = "user_email";
	private final String KEY_PASS = "user_pass";
	private final String PREFS_NAME = "youcast_credentials";
	
	private static CredentialsManager instance;
	private SharedPreferences mPrefs;
	/**
	 * Cached value of the signed-in user's username, or null if no user is signed-in
	 */
	private static String username;
	/**
	 * Cached value of the signed-in user's password, or null if no user is signed-in
	 */
	private static String password;
	
	private CredentialsManager(Context ctx) {
		mPrefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		username = null;
		password = null;
	}
	
	public static CredentialsManager getInstance(Context ctx) {
		if (instance == null) {
			instance = new CredentialsManager(ctx.getApplicationContext());
		}
		return instance;
	}
	/**
	 * @return the stored username or "" if none was found
	 */
	public String getUsername() {
		if (username == null || "".equals(username)) username = mPrefs.getString(KEY_USER, "");
		return username;
	}
	/**
	 * @return the stored password in clear or "" if none was found
	 */
	public String getPassword() {
		if (password == null || "".equals(password)) password = mPrefs.getString(KEY_PASS, "");
		return password;
	}
	/**
	 * @return a map with values: <br/> CredentialsManager.USERNAME => username <br/> CredentialsManager.PASSWORD => password (clear)
	 */
	public Map<String, String> getCredentials() {
		Map<String, String> cred = new HashMap<String, String>(2);
		cred.put(USERNAME, getUsername());
		cred.put(PASSWORD, getPassword());
		return cred;
	}
	
	/**
	 * Store the given credentials in the private preferences, 
	 * and cache them in the corresponding static variables. 
	 * @param usr the username
	 * @param pwd the password in clear
	 * @return true if the credentials were saved successfully
	 */
	public boolean storeCredentials(String usr, String pwd) {
		/* 
		 * TODO store in an encrypted file, or store the password encrypted
		 * cf http://android-developers.blogspot.com/2013/02/using-cryptography-to-store-credentials.html
		 * Or use http://developer.android.com/reference/android/accounts/AccountManager.html
		 */
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putString(KEY_USER, usr);
		username = usr;
		editor.putString(KEY_PASS, pwd);
		password = pwd;
		return editor.commit();
	}
	/**
	 * Deletes the credentials from the private preferences
	 * @return true if the credentials were deleted successfully
	 */
	public boolean deleteCredentials() {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.remove(KEY_USER);
		username = null;
		editor.remove(KEY_PASS);
		password = null;
		return editor.commit();
	}
	/**
	 * @return true if both username and password are stored in the private preferences
	 */
	public boolean credentialsExist() {
		return mPrefs.contains(KEY_USER) && mPrefs.contains(KEY_PASS);
	}
	
}
