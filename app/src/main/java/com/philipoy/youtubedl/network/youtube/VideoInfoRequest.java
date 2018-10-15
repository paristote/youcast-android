package com.philipoy.youtubedl.network.youtube;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.Header;
import com.android.volley.toolbox.HttpHeaderParser;
import com.philipoy.youtubedl.extractor.YoutubeInfoExtractor;
import com.philipoy.youtubedl.extractor.YoutubeInfoExtractor.YoutubeVideoInfos;

public class VideoInfoRequest extends YoutubeDownloadRequest {
	
	public static final String INFO_URL        = "https://www.youtube.com/get_video_info?&video_id=____&el=embedded&ps=default&eurl=&gl=US&hl=en";
	public static final String INFO_URL_DETAIL = "https://www.youtube.com/get_video_info?&video_id=____&el=detailpage&ps=default&eurl=&gl=US&hl=en";
	
	private boolean mHDEnabled;
	
	public static VideoInfoRequest newRequest(String url, boolean hd, Listener<YoutubeDownloadResponse> successListener, ErrorListener errorListener) {
		return new VideoInfoRequest(url, hd, successListener, errorListener);
	}

	private VideoInfoRequest(String url, boolean hd, Listener<YoutubeDownloadResponse> successListener, ErrorListener errorListener) {
		super(Request.Method.GET, url, successListener, errorListener);
		this.mHDEnabled = hd;
	}

	@Override
	protected Response<YoutubeDownloadResponse> parseNetworkResponse(NetworkResponse response) {
		YoutubeDownloadResponse resp = null;
		if (response.statusCode >= 200 && response.statusCode < 300 ) {
			YoutubeVideoInfos infos = YoutubeInfoExtractor.extract(new String(response.data), this.mHDEnabled);
			if (infos != null) {
				if (infos.retry_detail_page) {     // Force redirect to the detail page
					Log.d(TAG, "Retrying with detail page");
					resp = YoutubeDownloadResponse.retry(INFO_URL_DETAIL);
				} else {                            // Normal successful response
					resp = YoutubeDownloadResponse.okWithInfos(infos);
				}
			} else {                                // Force failure because no accepted format was found
				resp = YoutubeDownloadResponse.error();
			}
		} else if (response.statusCode >= 300 && response.statusCode < 400) {
			for (Header header : response.headers) {
				if ("Location".equalsIgnoreCase(header.name)) {
					resp = YoutubeDownloadResponse.retry(header.value);
					break;
				}
			}
		} else {
			resp = YoutubeDownloadResponse.error();
		}
		return Response.success(resp, HttpHeaderParser.parseCacheHeaders(response));
	}
}
