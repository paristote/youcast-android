package com.philipoy.youtubedl.network.youtube;

import java.util.List;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.toolbox.Header;
import com.android.volley.toolbox.HttpHeaderParser;

public class LangRequest extends YoutubeDownloadRequest {

	public static final String LANG_URL = "https://www.youtube.com/?hl=en&persist_hl=1&gl=US&persist_gl=1&opt_out_ackd=1";
	
	public static LangRequest newRequest(String url, Response.Listener<YoutubeDownloadResponse> successListener, Response.ErrorListener errorListener) {
		return new LangRequest(url, successListener, errorListener);
	}
	
	private LangRequest(String url, Response.Listener<YoutubeDownloadResponse> successListener, ErrorListener errorListener) {
		super(url, successListener, errorListener);
	}

	@Override
	protected Response<YoutubeDownloadResponse> parseNetworkResponse(NetworkResponse response) {
		List<Header> headers = response.headers;
		StringBuilder cookieString = new StringBuilder(mCookiesString);
		String location = null;
		for (Header header : headers) {
			if ("Set-Cookie".equalsIgnoreCase(header.name) && header.value != null) {
				String cookie = header.value.split(";")[0];
				String[] parts = cookie.split("=");
				if (parts.length >= 2) {
					cookieString.append(cookie).append("; ");
				}
			} else if("Location".equalsIgnoreCase(header.name)) {
				location = "Location:"+header.value;
			}
		}
		mCookiesString = cookieString.toString();
		Log.d(TAG, "Cookie :" + cookieString);

		YoutubeDownloadResponse resp = null;
		if (response.statusCode >= 200 && response.statusCode < 300) // OK
			resp = YoutubeDownloadResponse.ok();
		else if (response.statusCode >= 300 && response.statusCode < 400)
			resp = (location!=null ? YoutubeDownloadResponse.retry(location) : YoutubeDownloadResponse.error());
		else
			resp = YoutubeDownloadResponse.error();
		
		return Response.success(resp, HttpHeaderParser.parseCacheHeaders(response));
	}

}
