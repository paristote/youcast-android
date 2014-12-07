package com.philipoy.youtubedl.utils;

import java.net.ConnectException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import retrofit.RetrofitError;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Log;

import com.philipoy.youtubedl.App;
import com.philipoy.youtubedl.rest.Device;
import com.philipoy.youtubedl.rest.Devices;
import com.philipoy.youtubedl.rest.YouCastInterface;

public class DeviceManager {
	private final String DEVICE_ID  = "device_id";
	private final String PREFS_NAME = "youcast_device";
	
	private static DeviceManager instance;
	private SharedPreferences mPrefs;
	private Context           mContext;
	/**
	 * Cached value of this device id, or null
	 */
	public static String deviceId;
	
	private DeviceManager(Context ctx) {
		mPrefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		mContext = ctx;
		deviceId = null;
	}
	
	public static DeviceManager getInstance(Context ctx) {
		if (instance == null) {
			instance = new DeviceManager(ctx.getApplicationContext());
		}
		return instance;
	}
	
	/**
	 * @return the stored device ID or "" if none is found
	 */
	public String getDeviceId() {
		if (deviceId == null || "".equals(deviceId)) deviceId = mPrefs.getString(DEVICE_ID, "");
		return deviceId;
	}
	
	/**
	 * Generate the deviceId, store it on the device and cache it in the corresponding static variable.
	 * Attempt to register the device on the server.
	 * @param username username used to generate a device id
	 * @return true if the device ID is stored on the device, false otherwise
	 */
	public boolean registerDevice() {
		String dId = getDeviceId();
		Map<String, String> credentials = CredentialsManager.getInstance(mContext).getCredentials();
		String username = credentials.get(CredentialsManager.USERNAME);
		String password = credentials.get(CredentialsManager.PASSWORD);
		boolean registered = !"".equals(dId);
		if (!registered) {
			dId = generateDeviceId(username);
			Editor editor = mPrefs.edit();
			editor.putString(DEVICE_ID, dId);
			deviceId = dId;
			registered = editor.commit();
		}
		new RegisterTask(username, password, dId).execute();
		return registered;
	}
	
	/**
	 * Set the status connected=false for this device
	 * @return true if the device status connected=false
	 */
	public boolean disconnectDevice() {
		
		return false;
	}
	
	// TODO
	public boolean unregisterDevice() {
		
		return false;
	}
	
	/**
	 * Generates a device ID that is the MD5 hash of
	 * <ul>
	 * <li>the username</li>
	 * <li>the current time</li>
	 * <li>the variable android.os.Build.PRODUCT</li>
	 * </ul>
	 * @param username
	 * @return
	 */
	private String generateDeviceId(String username)
	{
		StringBuilder sb = new StringBuilder(username)
			.append("-").append(System.currentTimeMillis())
			.append("-").append(android.os.Build.PRODUCT);
		String deviceId = null;
		final String MD5 = "MD5";
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
			digest.update(sb.toString().getBytes());
			byte messageDigest[] = digest.digest();
			// Create Hex String
	        StringBuilder hexString = new StringBuilder();
	        for (byte aMessageDigest : messageDigest) {
	            String h = Integer.toHexString(0xFF & aMessageDigest);
	            while (h.length() < 2)
	                h = "0" + h;
	            hexString.append(h);
	        }
	        deviceId = hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			Log.e(App.LOG_TAG, "Could not hash the device id with MD5");
		}
		
		return deviceId;
	}
	
	private static class RegisterTask extends AsyncTask<Void, Void, Device>
	{

		private String username;
		private String password;
		private String deviceId;
		
		public RegisterTask(String usr, String pwd, String id) {
			username = usr;
			password = pwd;
			deviceId = id;
		}
		
		@Override
		protected Device doInBackground(Void... params) {
			YouCastInterface yc = YouCastInterface.Factory.create(username, password);
			Device d = new Device();
			d.id = deviceId;
			d.username = username;
			d.setConnected(true);
			d.setType(Device.DeviceType.ANDROID);
			d.name = android.os.Build.MODEL;
			Device registeredDevice = null;
			try {
				Devices devices = yc.registerDevice(username, d);
				if (devices.deviceCount == 1) {
					registeredDevice = devices.devices[0];
				}
			} catch (RetrofitError error) {
				if (error.getCause() instanceof ConnectException) {
					Log.i(App.LOG_TAG, "Not connected to server.");
				} else {
					Log.i(App.LOG_TAG, error.getMessage());
				}
			}
			return registeredDevice;
		}
	}
}
