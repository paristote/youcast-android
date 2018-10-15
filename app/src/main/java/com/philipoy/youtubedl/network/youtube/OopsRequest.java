package com.philipoy.youtubedl.network.youtube;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

public class OopsRequest extends YoutubeDownloadRequest {
	
	public static OopsRequest newRequest(String url, Listener<YoutubeDownloadResponse> successListener, ErrorListener errorListener) {
		return new OopsRequest(url, successListener, errorListener);
	}
	
	private OopsRequest(String url, Listener<YoutubeDownloadResponse> successListener, ErrorListener errorListener) {
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
