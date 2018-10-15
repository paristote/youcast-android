package com.philipoy.youtubedl.network.youtube;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.philipoy.youtubedl.App;
import com.philipoy.youtubedl.extractor.YoutubeInfoExtractor.YoutubeVideoInfos;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class DownloadVideoRequest {
	
	public static final int SUCCESS = 0;
	public static final int BAD_REQUEST = 1;
	public static final int ALREADY_EXISTS = 2;
	public static final int DOWNLOAD_FAILED = 3;
	public static final int NOT_ENOUGH_SPACE = 4;
	public static final int NO_STORAGE = 5;
	public static final int RETRY_NEW_LOCATION = 6;
	public static final int UNKNOWN_ERROR = 20;

	public static long newRequest(YoutubeVideoInfos infos, VideoDownloadStatusListener listener, Context ctx) {
		
		DownloadVideoRequest rq = new DownloadVideoRequest(infos, listener, ctx);
		rq.downloadOkHttp();
		return 0;
	}
	
	private Context                     mContext;
	private YoutubeVideoInfos           mInfos;
	private VideoDownloadStatusListener mStatusListener;
	
	private DownloadVideoRequest(YoutubeVideoInfos infos, VideoDownloadStatusListener listener, Context ctx) {
		mInfos = infos;
		mContext = ctx;
		mStatusListener = listener;
	}
	
	@SuppressWarnings("deprecation")
	private long availableSpace(String pathToCheck) {
		StatFs stats = new StatFs(pathToCheck);
		long bytesAvailable = stats.getAvailableBlocks() * stats.getBlockSize();
		return bytesAvailable;
	}
	
	private String filenameFromTitle(String title)
	{
		return title.replaceAll("[\\s\\W]", "_")+".mp4";
	}
	
	public void downloadOkHttp() {
		
		AsyncTask<Void, Long, Integer> dl = new AsyncTask<Void, Long, Integer>() {
			
			private OkHttpClient client;
			private SuccessResponse resp = new SuccessResponse();
			private long videoLength;
			
			@Override
			protected void onPreExecute() {
				client = new OkHttpClient();
			}

			@Override
			protected void onProgressUpdate(Long... values) {
				long progress = values[0].longValue();
				if (mStatusListener != null) mStatusListener.onProgress(progress, videoLength);
				super.onProgressUpdate(values);
			}

			@Override
			protected Integer doInBackground(Void... params) {
				
				URL url = null;
				Request rq = null;
				try {
					url = new URL(mInfos.getFullUrl());
					rq = new Request.Builder()
					 .url(url)
					 .header("Accept-Encoding", "identity")
					 .header("Accept-Language", "en-us,en;q=0.5")
					 .header("Host", url.getHost())
					 .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
					 .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20100101 Firefox/10.0 (Chrome)")
					 .header("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7")
					 .header("Connection", "close")
					 .build();
				} catch (MalformedURLException e) {
					return Integer.valueOf(BAD_REQUEST); 
				} catch (IllegalStateException e2) {
					return Integer.valueOf(BAD_REQUEST); // exit with error BAD_REQUEST
				}
				
				Response rp = null;
				InputStream is = null;
				FileOutputStream fos = null;
				try {
					rp = client.newCall(rq).execute();
					Log.d(App.LOG_TAG, rp.message()+" / "+rp.code());
					if (rp.code() >= 300 && rp.code() < 400) {
						// String newLocHeader = rp.header("Location", "");
						// TODO handle redirection to the new download location
						return Integer.valueOf(RETRY_NEW_LOCATION);
					}
					if (rp.code() >= 400) return Integer.valueOf(BAD_REQUEST); // exit with error BAD_REQUEST
					videoLength = rp.body().contentLength();
					is = rp.body().byteStream();
					File parentDir = null;
					if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
						parentDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "YouCast");
					} else {
//						parentDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "YoutubeDL");
						parentDir = new File(mContext.getFilesDir(), "Clips");
					}
//					if (parentDir == null) return Integer.valueOf(NO_STORAGE);
					parentDir.mkdir();
					// TODO check available storage
//					if (videoLength >= availableSpace(parentDir.getAbsolutePath())) return Integer.valueOf(NOT_ENOUGH_SPACE); // exit with error NOT_ENOUGH_SPACE
					File dest = new File(parentDir, filenameFromTitle(mInfos.videoTitle));
					if (dest.exists()) return Integer.valueOf(ALREADY_EXISTS); // exit with error ALREADY_EXISTS
					resp.destination = dest.getAbsolutePath();
					fos = new FileOutputStream(dest);
					byte data[] = new byte[1024];
			        int count = 0;
			        long total = 0;
			        int progress = 0, progress_temp = 0;
			        resp.videoSize = videoLength;
			        while ((count=is.read(data)) != -1)
			        {
			            total += count;
			            fos.write(data, 0, count);
			            progress = (int) (total*100/videoLength);
			            if (progress != progress_temp) {
			            	progress_temp = progress;
			            	publishProgress(total);
			            }
			        }
			        is.close();
			        fos.close();
			        
			        MediaScannerConnection.scanFile(mContext,
			                new String[] { dest.toString() }, null,
			                new MediaScannerConnection.OnScanCompletedListener() {
			            public void onScanCompleted(String path, Uri uri) {
			                Log.i("ExternalStorage", "Scanned " + path + ":");
			                Log.i("ExternalStorage", "-> uri=" + uri);
			            }
			        });
				} catch (IOException e) {
					Log.e(App.LOG_TAG, e.getMessage());
					return Integer.valueOf(DOWNLOAD_FAILED); // exit with error DOWNLOAD_FAILED
				} catch (NullPointerException e2) {
					Log.e(App.LOG_TAG, e2.getMessage());
					return Integer.valueOf(DOWNLOAD_FAILED);
				}
				
				
				
				return Integer.valueOf(SUCCESS); // exit with success
			}
			
			@Override
			protected void onPostExecute(Integer result) {
				int code = result.intValue();
				if (mStatusListener != null) {
					if (code == SUCCESS) mStatusListener.onSuccess(resp.destination, mInfos.videoTitle, mInfos.videoId, resp.videoSize);
					else {
						String reason = null;
						switch (code) {
						case ALREADY_EXISTS: reason = "Video already exists"; break;
						case DOWNLOAD_FAILED: reason = "Download failed"; break;
						case BAD_REQUEST: reason = "Bad request"; break;
						case NOT_ENOUGH_SPACE: reason = "Not enough space"; break;
						case NO_STORAGE: reason = "No available storage"; break;
						case RETRY_NEW_LOCATION: reason = "Retry with new location"; break;
						case UNKNOWN_ERROR: reason = "Unknown error"; break;
						}
						mStatusListener.onError(reason);
					}
				}
				super.onPostExecute(result);
			}
			
		};
		
		dl.execute();
		
	}
	
	private class SuccessResponse {
		public String destination;
		public long   videoSize;
	}
	
	public interface VideoDownloadStatusListener {
		public void onSuccess(String videoPath, String videoTitle, String videoId, long videoSize);
		public void onProgress(long progress, long max);
		public void onError(String reason);
	}
	
}
