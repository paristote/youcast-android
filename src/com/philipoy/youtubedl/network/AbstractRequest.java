package com.philipoy.youtubedl.network;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

public abstract class AbstractRequest<T> extends Request<T> {

	protected Listener<T> mSuccessListener;
	protected String      TAG = "AbstractRequest";
	
	public AbstractRequest(int method, String url, Listener<T> successListener, ErrorListener errorListener) {
		super(method, url, errorListener);
		this.mSuccessListener = successListener;
	}

	public AbstractRequest(String url, Listener<T> successListener, ErrorListener errorListener) {
		super(Request.Method.GET, url, errorListener);
		this.mSuccessListener = successListener;
	}

	@Override
	protected void deliverResponse(T response) {
		if (mSuccessListener != null)
			mSuccessListener.onResponse(response);
	}
	
}
