package com.philipoy.youtubedl.service;

import java.util.Map;

import android.app.IntentService;
import android.content.Intent;

import com.philipoy.youtubedl.download.YoutubeDownload;
import com.philipoy.youtubedl.rest.Video;
import com.philipoy.youtubedl.rest.YouCastInterface;
import com.philipoy.youtubedl.utils.CredentialsManager;

public class DownloadService extends IntentService {

	private static final String NAME = "YoutubeDownloadService";
	
	public static final String VIDEO_ID = "video_id";
	public static final String NEED_UPDATES = "need_updates";
	
	private YouCastInterface yc;
	private Video video;
	private boolean needUpdates;
	
	public DownloadService() {
		super(NAME);
		setIntentRedelivery(true);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String videoId = intent.getStringExtra(VIDEO_ID);
		needUpdates = intent.getBooleanExtra(NEED_UPDATES, true);
		if (videoId != null)
		{
			
			// Prepare service and video object to update the status
			prepareForUpdates(videoId);

			try {
				// Start download
				YoutubeDownload dl = YoutubeDownload.newDownload(videoId, getApplicationContext());

				// Update status to 'Started'
				updateVideoStatus(Video.Status.STARTED);

				// Wait until the download is complete
				while (dl.getStatus() == YoutubeDownload.Status.IN_PROGRESS)
				{
					// empty loop will stop when the download ends, successfully or not
					// hack to "block" the service while a download is in progress, so other calls to the service
					// get queued and started only when the previous one ends
					Thread.sleep(5000);
				}
				
				// Final status update on the web service
				if (dl.getStatus() == YoutubeDownload.Status.COMPLETED) {
					updateVideoStatus(Video.Status.DONE);
				} else if (dl.getStatus() == YoutubeDownload.Status.FAILED) {
					updateVideoStatus(Video.Status.FAILED);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Create the YouCastInterface and the Video object for updating the download status on the website.
	 * Returns immediately if updates are not needed.
	 * @param videoId the ID of the video to download
	 */
	private void prepareForUpdates(String videoId) {
		if (!needUpdates) return;
		Map<String, String> credentials = CredentialsManager.getInstance(getApplicationContext()).getCredentials();
		String user = credentials.get(CredentialsManager.USERNAME);
		yc = YouCastInterface.Factory.create(user, credentials.get(CredentialsManager.PASSWORD));
		video = new Video();
		video.id = videoId;
		video.username = user;
		video.downloadCount = 1; // TODO better increment
	}
	/**
	 * Updates the download of the video with the given status.
	 * Returns immediately if updates are not needed.
	 * @param newStatus the status to update on the website
	 */
	private void updateVideoStatus(int newStatus) {
		if (!needUpdates) return;
		video.status = newStatus;
		yc.updateVideo(video.username, video.id, video);
	}

}
