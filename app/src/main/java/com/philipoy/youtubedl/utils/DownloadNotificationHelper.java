package com.philipoy.youtubedl.utils;

import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import com.philipoy.youtubedl.App;
import com.philipoy.youtubedl.R;

public class DownloadNotificationHelper {
	
	private int mNotifId;
	private Context mContext;
	private NotificationManager mNotifManager;
	private String mVideoId;
	private String mVideoTitle;
	
	public DownloadNotificationHelper(Context ctx) {
		mNotifId = new Random().nextInt();
		this.mContext = ctx;
		this.mNotifManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	private String getString(int id) {
		return mContext.getResources().getString(id);
	}
	
	/**
	 * Create a Notification.Builder with default properties:
	 * <ul>
	 * <li>small icon : the download icon</li>
	 * <li>defaults : DEFAULT_LIGHTS</li>
	 * </ul>
	 * @return a Notification.Builder
	 */
	private Notification.Builder baseNotificationBuilder()
	{
		return new Notification.Builder(mContext)
		.setSmallIcon(R.drawable.ic_notif_download)
		.setDefaults(Notification.DEFAULT_LIGHTS);
	}
	
	/**
	 * Adds these properties to the base Notification.Builder:
	 * <ul>
	 * <li>ongoing : true</li>
	 * <li>progress : indeterminate</li>
	 * </ul>
	 * @return a Notification.Builder
	 */
	private Notification.Builder ongoingNotificationBuilder()
	{
		return baseNotificationBuilder()
				.setOngoing(true)
				.setProgress(1, 0, true);
	}
	
	/**
	 * Adds these properties to the base Notification.Builder:
	 * <ul>
	 * <li>ongoing : false</li>
	 * <li>auto cancel : true</li>
	 * </ul>
	 * @return a Notification.Builder
	 */
	private Notification.Builder cancelableNotificationBuilder()
	{
		return baseNotificationBuilder()
				.setOngoing(false)
				.setAutoCancel(true);
	}
	
	/**
	 * Sends this notification:<br/>
	 * Downloading video {videoId}<br/>
	 * Preparing download...
	 * @param videoId 
	 */
	public void notifyBegin(String videoId)
	{
		mVideoId = videoId;
		Notification.Builder b = ongoingNotificationBuilder();
		String title = getString(R.string.notif_downloading) + " " + mVideoId;
		b.setTicker(title);
		b.setContentText(getString(R.string.notif_preparing_download));
		b.setContentTitle(title);
		mNotifManager.notify(mNotifId, b.getNotification());
	}
	
	
	/**
	 * Sends this notification:<br/>
	 * Downloading {videoTitle}<br/>
	 * n %
	 * @param videoTitle
	 * @param max size of the video used to calculate the progress %age
	 * @param progress number of bytes already downloaded
	 */
	public void notifyProgress(String videoTitle, long max, long progress)
	{
		mVideoTitle = videoTitle;
		String title = getString(R.string.notif_download_started) + " " + videoTitle;
		String text = new String((progress*100)/max+"%");
		int maxKb = (int)max/1024;
		int progressKb = (int)progress/1024;
		Notification.Builder b = ongoingNotificationBuilder();
		b.setTicker(title);
		b.setContentText(text);
		b.setContentTitle(title);
		b.setProgress(maxKb, progressKb, false);
		Log.i(App.LOG_TAG, "*** Progress = "+text);
		mNotifManager.notify(mNotifId, b.getNotification());
	}
	
	/**
	 * Sends this notification:<br/>
	 * Download failed<br/>
	 * Could not download {videoTitle}.
	 */
	public void notifyFailure()
	{
		String title = getString(R.string.notif_download_failed);
		String text = getString(R.string.notif_download_failed_text) + " " + mVideoTitle;
		Notification.Builder b = cancelableNotificationBuilder();
		b.setTicker(title);
		b.setContentText(text);
		b.setContentTitle(title);
		b.setDefaults(Notification.DEFAULT_ALL);
		mNotifManager.notify(mNotifId, b.getNotification());
	}
	
	/**
	 * Sends this notification:<br/>
	 * Download successful<br/>
	 * {videoTitle}
	 */
	public void notifySuccess()
	{
		String title = getString(R.string.notif_download_success);
		Notification.Builder b = cancelableNotificationBuilder();
		b.setTicker(title);
		b.setContentText(mVideoTitle);
		b.setContentTitle(title);
		b.setDefaults(Notification.DEFAULT_ALL);
		mNotifManager.notify(mNotifId, b.getNotification());
	}
}
