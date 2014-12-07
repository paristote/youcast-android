package com.philipoy.youtubedl.network.youtube;

import java.util.List;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.Header;
import com.android.volley.toolbox.HttpHeaderParser;

public class ConfirmAgeRequest extends YoutubeDownloadRequest {

	public static final String AGE_URL = "https://www.youtube.com/verify_age?next_url=/&gl=US&hl=en";
	
	public static ConfirmAgeRequest newRequest(Listener<YoutubeDownloadResponse> successListener, ErrorListener errorListener) {
		return new ConfirmAgeRequest(AGE_URL, successListener, errorListener);
	}
	
	private ConfirmAgeRequest(String url, Listener<YoutubeDownloadResponse> successListener, ErrorListener errorListener) {
		super(Request.Method.POST, url, successListener, errorListener);
	}

	@Override
	public String getBodyContentType() {
		return "application/x-www-form-urlencoded";
	}

	@Override
	public byte[] getBody() throws AuthFailureError {
		return "next_url=%2F&action_confirm=Confirm".getBytes();
	}
	
	private int getSize() {
		try {
			return getBody().length;
		} catch (AuthFailureError e) {
			return 0;
		}
	}

	@Override
	public List<Header> getHeaders() throws AuthFailureError {
		List<Header> headers = super.getHeaders();
		headers.add(new Header("Content-Length", String.valueOf(getSize())));
		headers.add(new Header("Content-Type", "application/x-www-form-urlencoded"));
		return headers;
	}

	@Override
	protected Response<YoutubeDownloadResponse> parseNetworkResponse(NetworkResponse response) {
		YoutubeDownloadResponse resp = null;
		if (response.statusCode >= 200 && response.statusCode < 300 ) {
			resp = YoutubeDownloadResponse.ok();
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
