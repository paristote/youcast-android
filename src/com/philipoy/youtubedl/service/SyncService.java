package com.philipoy.youtubedl.service;

import java.net.ConnectException;

import retrofit.RetrofitError;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.philipoy.youtubedl.App;
import com.philipoy.youtubedl.rest.Video;
import com.philipoy.youtubedl.rest.Videos;
import com.philipoy.youtubedl.rest.YouCastInterface;
import com.philipoy.youtubedl.utils.CredentialsManager;

public class SyncService extends IntentService {

	private static final String NAME = "SyncService";
	public static final int     SYNC = 1;
	
	public SyncService() {
		super(NAME);
		setIntentRedelivery(true);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		String user = CredentialsManager.getInstance(getApplicationContext()).getUsername();
		String password = CredentialsManager.getInstance(getApplicationContext()).getPassword();
		YouCastInterface srv = YouCastInterface.Factory.create(user, password);
		try {
			Videos list = srv.getNewVideosToDownload(user);
			if (list != null && list.videos != null && list.videos.length > 0)
			{
				Video[] videos = list.videos;
				Log.i(App.LOG_TAG, list.videoCount+" videos to download");
				Intent download = new Intent(getApplicationContext(), DownloadService.class);
				for (Video video : videos) {
					if (video.status == Video.Status.REGISTERED) { // continue only for videos in status registered (initial status)
						video.status = Video.Status.SCHEDULED;
						srv.updateVideo(user, video.id, video);
						Log.i(App.LOG_TAG, "Adding download of: "+video.title);
						download.putExtra(DownloadService.VIDEO_ID, video.id);
						download.putExtra(DownloadService.NEED_UPDATES, true);
						startService(download);
					}
				}
			} else {
				Log.i(App.LOG_TAG, "Nothing new to download");
			}
		} catch (RetrofitError error) {
			if (error.getCause() instanceof ConnectException) {
				Log.i(App.LOG_TAG, "Not connected to server");
			} else {
				Log.i(App.LOG_TAG, error.getMessage());
			}
		}
	}

}
