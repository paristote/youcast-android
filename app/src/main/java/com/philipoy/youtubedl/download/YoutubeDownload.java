package com.philipoy.youtubedl.download;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.philipoy.youtubedl.App;
import com.philipoy.youtubedl.SettingsActivity;
import com.philipoy.youtubedl.extractor.YoutubeInfoExtractor.YoutubeVideoInfos;
import com.philipoy.youtubedl.network.OkHttpStack;
import com.philipoy.youtubedl.network.youtube.ConfirmAgeRequest;
import com.philipoy.youtubedl.network.youtube.DownloadVideoRequest;
import com.philipoy.youtubedl.network.youtube.DownloadVideoRequest.VideoDownloadStatusListener;
import com.philipoy.youtubedl.network.youtube.LangRequest;
import com.philipoy.youtubedl.network.youtube.OopsRequest;
import com.philipoy.youtubedl.network.youtube.VideoInfoRequest;
import com.philipoy.youtubedl.network.youtube.WebpageRequest;
import com.philipoy.youtubedl.network.youtube.YoutubeDownloadRequest;
import com.philipoy.youtubedl.network.youtube.YoutubeDownloadResponse;
import com.philipoy.youtubedl.utils.DownloadNotificationHelper;

public class YoutubeDownload implements Response.ErrorListener, Response.Listener<YoutubeDownloadResponse>{

	/*
	 * INITIALIZATION
	 */

	public static YoutubeDownload newDownload(String videoId, Context ctx) {
		YoutubeDownload dl = new YoutubeDownload(videoId, ctx);
		dl.startDownload();
		return dl;
	}

	private final String TAG = "YoutubeDownload";
	/**
	 * Util class to manage notifications about this download
	 */
	private DownloadNotificationHelper mNotifHelper;
	/**
	 * Youtube ID of the video to download
	 */
	private String mVideoId;
	/**
	 * Volley requests queue
	 */
	private RequestQueue mQueue;
	/**
	 * Context of the activity that started this download
	 */
	private Context mContext;
	/**
	 * Video Infos populated after step VIDEO_INFO
	 */
//	private YoutubeVideoInfos mInfos = null;
	/**
	 * Counter of retries of this download
	 */
	private int mCurrentTry;
	/**
	 * Maximum number of retries, set by the user in preferences
	 */
	private int mMaxRetries;
	/**
	 * Whether or not to download HD videos when available, set by the user in preferences
	 */
	private boolean mHDEnabled;
	/**
	 * Status of the current download, one of WAITING, IN_PROGRESS, COMPLETED or FAILED
	 */
	private int mStatus;
	/**
	 * Represents the different statuses of a download:
	 * <ul>
	 * <li>WAITING     (0) : Waiting to be started</li>
	 * <li>IN_PROGRESS (1) : Started</li>
	 * <li>COMPLETED   (2) : Download successful</li>
	 * <li>FAILED      (3) : Download failed</li>
	 * </ul>
	 */
	public static class Status
	{
		public static final int WAITING     = 0;
		public static final int IN_PROGRESS = 1;
		public static final int COMPLETED   = 2;
		public static final int FAILED      = 3;
	}
	/**
	 * Current step of this download
	 */
	private int mStep;
	/**
	 * Represents the different steps of a download:
	 * <ul>
	 * <li>SET_LANG (1)</li>
	 * <li>AGE_CONFIRM (2)</li>
	 * <li>WEBPAGE (3)</li>
	 * <li>VIDEO_INFO (4)</li>
	 * <li>DOWNLOAD_VIDEO (5)</li>
	 * </ul>
	 */
	public static class Step
	{
		public static final int SET_LANG       = 1;
		public static final int AGE_CONFIRM    = 2;
		public static final int WEBPAGE        = 3;
		public static final int VIDEO_INFO     = 4;
		public static final int DOWNLOAD_VIDEO = 5;
	}
	
	/*
	 * OBJECT
	 */
	
	private YoutubeDownload(String videoId, Context ctx) {
		this.mVideoId = videoId;
		this.mContext = ctx;
		this.mQueue = Volley.newRequestQueue(mContext, new OkHttpStack());
		this.mNotifHelper = new DownloadNotificationHelper(mContext);
		this.mCurrentTry = 1;
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		this.mMaxRetries = Integer.parseInt(pref.getString(SettingsActivity.PREF_MAX_RETRIES, "5"));
		this.mHDEnabled = pref.getBoolean(SettingsActivity.PREF_HD_ENABLED, false);
		this.mStatus = Status.WAITING;
	}
	
	public void setStatus(int status) {
		if (status == Status.FAILED || status == Status.COMPLETED) {
			this.mStatus = status;
		}
	}
	
	public int getStatus() {
		return this.mStatus;
	}
	
	public void startDownload() {
		Log.d(TAG, "Starting download of video "+mVideoId);
		this.mStatus = Status.IN_PROGRESS;
		langRequest("");
	}
	
	public void restart() {
		mCurrentTry++;
		startDownload();
	}
	
	private boolean stopAfterTooManyTries() {
		boolean stop = (mCurrentTry > mMaxRetries);
		if (stop) {
			mNotifHelper.notifyFailure();
			this.mStatus = Status.FAILED;
		}
		return stop;
	}
	
	/*
	 * REQUESTS
	 */
	
	private void langRequest(String url) {
		if (stopAfterTooManyTries()) return;
		Log.d(TAG, "Setting language");
		mNotifHelper.notifyBegin(mVideoId);
		this.mStep = Step.SET_LANG;
		
		if ("".equals(url)) url = LangRequest.LANG_URL;
		
		YoutubeDownloadRequest rq = LangRequest.newRequest(url, this, this);
		
		mQueue.add(rq);
	}
	
	private void confirmAgeRequest() {
		if (stopAfterTooManyTries()) return;
		Log.d(TAG, "Confirming age");
		this.mStep = Step.AGE_CONFIRM;
		
		YoutubeDownloadRequest rq = ConfirmAgeRequest.newRequest(this, this);
		
		mQueue.add(rq);
	}
	
	private void newLocationRequest(String url) {
		if (stopAfterTooManyTries()) return;
		Log.d(TAG, "GET "+url);
		// TODO this is the same as the confirm age request but to a different loc
		
		YoutubeDownloadRequest rq = OopsRequest.newRequest(url, this, this);
		
		mQueue.add(rq);
	}
	
	private void webpageRequest() {
		if (stopAfterTooManyTries()) return;
		Log.d(TAG, "Getting video webpage");
		this.mStep = Step.WEBPAGE;
		
		YoutubeDownloadRequest rq = WebpageRequest.newRequest(mVideoId, this, this);
		
		mQueue.add(rq);
	}
	
	private void videoInfoRequest(String url) {
		if (stopAfterTooManyTries()) return;
		Log.d(TAG, "Getting video information");
		this.mStep = Step.VIDEO_INFO;
		
		if ("".equals(url)) {
			url = VideoInfoRequest.INFO_URL.replace("____", mVideoId);
		}
		
		YoutubeDownloadRequest rq = VideoInfoRequest.newRequest(url, this.mHDEnabled, this, this);
		
		mQueue.add(rq);
	}
	
	private void downloadVideoRequest(YoutubeVideoInfos infos) {
		if (stopAfterTooManyTries()) return;
		if (infos == null) restart();
		Log.d(TAG, "Downloading video...");
		this.mStep = Step.DOWNLOAD_VIDEO;
		
		final String title = infos.videoTitle;
//		mNotifHelper.notifyProgress(title, 1, 0);
		infos.videoId = mVideoId;
		DownloadVideoRequest.newRequest(infos, new VideoDownloadStatusListener() {
			@Override
			public void onSuccess(String videoPath, String videoTitle, String videoId, long videoSize) {
				Log.d(TAG, "Video downloaded");
				mNotifHelper.notifySuccess();
				setStatus(Status.COMPLETED);
			}
			@Override
			public void onError(String reason) {
				Log.e(TAG, reason);
				mCurrentTry++;
				startDownload();
			}
			@Override
			public void onProgress(long progress, long max) {
//				String title = "Downloading "+mInfos.videoTitle;
				mNotifHelper.notifyProgress(title, max, progress);
			}
		}, mContext);
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		Log.e(App.LOG_TAG, error.getMessage());
		// Error thrown by Volley because of an IOException, although the response code == 200 => just retry
		if (this.mStep == Step.SET_LANG && error != null && error.networkResponse != null && error.networkResponse.statusCode == 200) {
			mCurrentTry++;
			startDownload();
		} else {
//			setStatus(Status.FAILED);
			mCurrentTry++;
			startDownload();
		}
	}

	@Override
	public void onResponse(YoutubeDownloadResponse response) {
		switch (this.mStep) {
		case Step.SET_LANG:
			if (response.status == YoutubeDownloadResponse.Status.OK) {
				confirmAgeRequest();
			} else if (response.status == YoutubeDownloadResponse.Status.RETRY) {
				langRequest(response.newLocation);
			} else {
				restart();
			}
			break;
		case Step.AGE_CONFIRM:
			if (response.status == YoutubeDownloadResponse.Status.OK) {
				webpageRequest();
			} else if (response.status == YoutubeDownloadResponse.Status.RETRY) {
				newLocationRequest(response.newLocation);
			} else {
				restart();
			}
			break;
		case Step.WEBPAGE:
			if (response.status == YoutubeDownloadResponse.Status.OK) {
				videoInfoRequest("");
			} else if (response.status == YoutubeDownloadResponse.Status.FAILED) {
				restart();
			}
			break;
		case Step.VIDEO_INFO:
			if (response.status == YoutubeDownloadResponse.Status.OK) {
//				this.mInfos = response.videoInfos;
				downloadVideoRequest(response.videoInfos);
			} else if (response.status == YoutubeDownloadResponse.Status.RETRY) {
				String newLocation = response.newLocation.replace("____", mVideoId);
				videoInfoRequest(newLocation);
			} else {
				restart();
			}
			break;
		case Step.DOWNLOAD_VIDEO:
			break;
		}
	}
}
