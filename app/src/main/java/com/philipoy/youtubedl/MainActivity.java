package com.philipoy.youtubedl;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.philipoy.youtubedl.service.SyncService;
import com.philipoy.youtubedl.utils.CredentialsManager;
import com.philipoy.youtubedl.utils.DeviceManager;
import com.philipoy.youtubedl.widget.VideoListView;
import com.philipoy.youtubedl.widget.VideoListView.VideoListStatusListener;

public class MainActivity extends Activity implements VideoListStatusListener {
	
	private VideoListView mListVideos;
	private LinearLayout mPanelEmpty;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mListVideos = (VideoListView)findViewById(R.id.main_list_videos);
		mPanelEmpty = (LinearLayout)findViewById(R.id.main_panel_empty);
		
		
			
		// register the device
		if (DeviceManager.getInstance(this).registerDevice()) {
			Toast.makeText(this, R.string.toast_registration_success, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, R.string.toast_registration_failed, Toast.LENGTH_LONG).show();
		}
		
		// schedule the app to sync with the server every 5 minutes
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean sync = pref.getBoolean(SettingsActivity.PREF_SYNC_ENABLED, true);
		if (sync) scheduleSyncEvery5Minutes();
	}
	
	@Override
	protected void onResume() {
		
		mListVideos.initList(this, this);
		
		super.onResume();
	}

//	@Override
//	protected void onRestoreInstanceState(Bundle savedInstanceState) {
//		mListVideos.restartLoader(this);
//		super.onRestoreInstanceState(savedInstanceState);
//	}
//
//	@Override
//	protected void onSaveInstanceState(Bundle outState) {
//		mListVideos.stopLoader(this);
//		super.onSaveInstanceState(outState);
//	}

	/**
	 * Schedules an alarm, via Alarm Manager, to start the service SyncService every 5 minutes.
	 */
	private void scheduleSyncEvery5Minutes() {
		Log.i(App.LOG_TAG, "Scheduled synchronisation every 5 minutes");
		Intent sync = new Intent(this, SyncService.class);
		PendingIntent alarmIntent = PendingIntent.getService(this, SyncService.SYNC, sync, 0);
		
		AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		Calendar cal = Calendar.getInstance();
		// SYNC_FREQUENCY is in seconds, need to multiply by 1000 to get milliseconds
		long syncFreq = App.SYNC_FREQUENCY * 1000;
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), syncFreq, alarmIntent);
	}
	
	private void stopSync() {
		Intent sync = new Intent(this, SyncService.class);
		PendingIntent alarmIntent = PendingIntent.getService(this, SyncService.SYNC, sync, 0);
		AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(alarmIntent);
	}
	
	@Override
	public void onVideoListLoaded(int videoCount) {
		if (videoCount == 0)
			mPanelEmpty.setVisibility(View.VISIBLE);
		else
			mPanelEmpty.setVisibility(View.GONE);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			// Open the settings screen
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
			return true;
		} else if (id == R.id.action_signout) {
			// Sign-out
			// Stop syncing the videos to download with the server
			stopSync();
			// Delete the credentials from the local storage
			CredentialsManager.getInstance(this).deleteCredentials();
			// TODO Set the device disconnected on the server
			DeviceManager.getInstance(this).disconnectDevice();
			// Opens the Launch activity in a new cleared task
			Intent launch = new Intent(this, LaunchActivity.class);
			launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(launch);
			// Finishes so users cannot navigate to this activity with the [back] button
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

}
