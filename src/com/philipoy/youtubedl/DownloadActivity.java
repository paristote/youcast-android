package com.philipoy.youtubedl;

import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.philipoy.youtubedl.network.OkHttpStack;
import com.philipoy.youtubedl.service.DownloadService;

public class DownloadActivity extends Activity {
	
	private String mVideoId;
	private boolean downloadNow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		downloadNow = pref.getBoolean(SettingsActivity.PREF_START_DOWNLOAD_AUTO, true);
		
		if (!downloadNow)
			setContentView(R.layout.activity_download);
	}
	
	@Override
	protected void onResume() {
		
		StringBuilder videoTitle = null;
		Intent receivedIntent = getIntent();
		if (receivedIntent != null) {
			String action = receivedIntent.getAction();
			String type = receivedIntent.getType();
			if (Intent.ACTION_SEND.equals(action) && type != null) {
				if ("text/plain".equals(type)) {
					String sharedText = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
				    if (sharedText != null) {
				    	// Example sharedText => GoPro: Shaun White's Backyard Mini Ramp: http://youtu.be/FVKlr9-WLfM
				    	// Video URL extraction
				    	String[] parts = sharedText.split(":");
				    	// Example URL => http://youtu.be/FVKlr9-WLfM
				    	String url = (parts.length >= 1) ? "http:"+parts[parts.length-1] : "";
				    	// Video title extraction
				    	videoTitle = new StringBuilder();
				    	int max = (parts.length >= 2) ? parts.length-2 : 0;
				    	for (int i=0; i<max; i++) {
				    		videoTitle.append(parts[i]).append(' ');
				    	}
				    	// Video ID extraction
				    	try {
							URI uri = new URI(url);
							mVideoId = uri.getPath().substring(1);
						} catch (URISyntaxException e) {
							Log.e(App.LOG_TAG, e.getMessage());
							// TODO display error message : incorrect url 
						}
				    }
		        }
			}
			if (mVideoId != null) {
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
				boolean downloadNow = pref.getBoolean(SettingsActivity.PREF_START_DOWNLOAD_AUTO, true);
				if (downloadNow) {
					// start download immediately
					startDownload();
					Toast.makeText(getApplicationContext(), R.string.toast_download_started, Toast.LENGTH_LONG).show();
					// call finish() to return to the previous app (youtube)
					finish();
				} else {
					// download the video's thumbnail, set the video title and the download button
					downloadThumbnail();
					Button button = (Button)findViewById(R.id.download_button);
					TextView videoTitleTextView = (TextView)findViewById(R.id.download_video_title);
					videoTitleTextView.setText(videoTitle.toString());
					button.setText(R.string.btn_start_download);
					button.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							startDownload();
							Toast.makeText(getApplicationContext(), R.string.toast_download_started, Toast.LENGTH_LONG).show();
							finish();
						}
					});
				}
			} else {
				// TODO display error message : no video id was found
			}
		}
		
		super.onResume();
	}

	
	private void downloadThumbnail() {
		if (mVideoId != null) {
			final ImageView thumbnailView = (ImageView)findViewById(R.id.download_thumbnail);
			thumbnailView.setVisibility(View.INVISIBLE);
			// URL has format http://img.youtube.com/vi/dUiANb6WsHA/0.jpg 480x360
			String imageUrl = "http://img.youtube.com/vi/"+mVideoId+"/0.jpg";
			ImageRequest request = new ImageRequest(imageUrl,
				    new Response.Listener<Bitmap>() {
				        public void onResponse(final Bitmap bitmap) {
				        	thumbnailView.setVisibility(View.VISIBLE);
				        	thumbnailView.setImageBitmap(bitmap);
				        }
				    }, 0, 0, null,
				    new Response.ErrorListener() {
				        public void onErrorResponse(VolleyError error) {
				        	thumbnailView.setVisibility(View.VISIBLE);
				        	thumbnailView.setImageResource(R.drawable.ic_big_arrow_down);
				        }
				    });
			Volley.newRequestQueue(this, new OkHttpStack()).add(request);
		}
		
	}

	/**
	 * Start a direct download, i.e. not initiated by the website
	 */
	private void startDownload() {
		if (mVideoId != null) {
			Intent dlService = new Intent(this, DownloadService.class);
			dlService.putExtra(DownloadService.VIDEO_ID, mVideoId);
			// this download is not initiated by the website, so we don't have to send updates back
			dlService.putExtra(DownloadService.NEED_UPDATES, false);
			startService(dlService);
		}
	}
}
