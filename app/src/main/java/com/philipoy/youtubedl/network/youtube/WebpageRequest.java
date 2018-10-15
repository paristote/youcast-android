package com.philipoy.youtubedl.network.youtube;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

public class WebpageRequest extends YoutubeDownloadRequest {
	
	private static final String WEBPAGE_URL = "https://www.youtube.com/watch?v=____&gl=US&hl=en&has_verified=1";
	
	public static WebpageRequest newRequest(String videoId, Listener<YoutubeDownloadResponse> successListener, ErrorListener errorListener) {
		String fullUrl = WEBPAGE_URL.replace("____", videoId); 
		return new WebpageRequest(fullUrl, successListener, errorListener);
	}

	private WebpageRequest(String url, Listener<YoutubeDownloadResponse> successListener, ErrorListener errorListener) {
		super(Request.Method.GET, url, successListener, errorListener);
	}
	
	@Override
	protected Response<YoutubeDownloadResponse> parseNetworkResponse(NetworkResponse response) {
		if (response.statusCode >= 200 && response.statusCode < 400 )
			return Response.success(YoutubeDownloadResponse.ok(), HttpHeaderParser.parseCacheHeaders(response));
		else
			return Response.success(YoutubeDownloadResponse.error(), HttpHeaderParser.parseCacheHeaders(response));
	}

}
