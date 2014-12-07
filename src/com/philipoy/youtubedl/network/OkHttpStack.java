package com.philipoy.youtubedl.network;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.android.volley.toolbox.HurlStack;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

public class OkHttpStack extends HurlStack {

	private final OkUrlFactory mFactory;

    public OkHttpStack() {
        this(new OkHttpClient());
    }

    public OkHttpStack(OkHttpClient client) {
        if (client == null) {
            throw new NullPointerException("OkHttpClient must not be null.");
        }
        mFactory = new OkUrlFactory(client);
    }

    @Override protected HttpURLConnection createConnection(URL url) throws IOException {
        return mFactory.open(url);
    }

}
