package com.philipoy.youtubedl;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsActivity extends Activity {
	
	public static final String PREF_SYNC_ENABLED        = "pref_sync_enabled";
	public static final String PREF_HD_ENABLED          = "pref_hd_enabled";
	public static final String PREF_MAX_RETRIES         = "pref_max_retries";
	public static final String PREF_START_DOWNLOAD_AUTO = "pref_start_download_auto";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new SettingsFragment()).commit();
		}
	}

	/**
	 * The preference fragment
	 */
	public static class SettingsFragment extends PreferenceFragment {

		public SettingsFragment() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			addPreferencesFromResource(R.xml.preferences);
		}

		
	}
}
