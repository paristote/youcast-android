package com.philipoy.youtubedl.network.youtube;

import java.util.ArrayList;
import java.util.List;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.Header;
import com.philipoy.youtubedl.network.AbstractRequest;

public abstract class YoutubeDownloadRequest extends AbstractRequest<YoutubeDownloadResponse> {
	
	protected String                    mCookiesString = "";

	public YoutubeDownloadRequest(int method, String url, Listener<YoutubeDownloadResponse> successListener, ErrorListener errorListener) {
		super(method, url, successListener, errorListener);
	}

	public YoutubeDownloadRequest(String url, Listener<YoutubeDownloadResponse> successListener, ErrorListener errorListener) {
		super(url, successListener, errorListener);
		TAG = "YoutubeDownloadRequest";
	}
	
	@Override
	public List<Header> getHeaders() throws AuthFailureError {
		List<Header> headers = new ArrayList<Header>();
		headers.add(new Header("Accept-Language", "en-us,en;q=0.5"));
		headers.add(new Header("Accept-Encoding", "gzip, deflate"));
		headers.add(new Header("Host", "www.youtube.com"));
		headers.add(new Header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
		headers.add(new Header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20100101 Firefox/10.0 (Chrome)"));
		headers.add(new Header("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7"));
		headers.add(new Header("Connection", "close"));
		if (!"".equals(mCookiesString)) {
			headers.add(new Header("Cookie", mCookiesString));
		}
		return headers;
	}

	@Override
	protected abstract Response<YoutubeDownloadResponse> parseNetworkResponse(NetworkResponse response);

}
