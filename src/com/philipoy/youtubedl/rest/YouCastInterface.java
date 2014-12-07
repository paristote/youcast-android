package com.philipoy.youtubedl.rest;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import android.util.Base64;
import android.util.Log;

import com.philipoy.youtubedl.App;

public interface YouCastInterface {

	@GET("/users/{user}/videos?status=new")
	Videos getNewVideosToDownload(@Path("user") String username);
	
	@GET("/users/{user}/videos?status=failed")
	Videos getFailedVideosToDownload(@Path("user") String username);
	
	@PUT("/users/{user}/videos/{video}")
	Videos updateVideo(@Path("user") String username, @Path("video") String videoId, @Body Video video);
	
	@POST("/users/{user}/connect")
	String connect(@Path("user") String username);
	
	@POST("/users/{user}/devices")
	Devices registerDevice(@Path("user") String username, @Body Device device);
	
	public static class Factory
	{
		/**
		 * Creates an instance of the YouCast Rest Interface. <br/>
		 * Adds an Authorization header to each request, using the given credentials.
		 * @param username username used for authentication
		 * @param password password used for authentication
		 * @return the YouCast Rest Interface 
		 * @throws IllegalArgumentException if username or password are null or empty
		 */
		public static YouCastInterface create(String username, String password) throws IllegalArgumentException {
			if (username == null || "".equals(username)) throw new IllegalArgumentException("Username must contain a value");
			if (password == null || "".equals(password)) throw new IllegalArgumentException("Password must contain a value");
			
			String userPassword = username+':'+password;
			final String encodedHeader = Base64.encodeToString(userPassword.getBytes(), Base64.DEFAULT);
			RequestInterceptor requestInterceptor = new RequestInterceptor() {
				  @Override
				  public void intercept(RequestFacade request) {
					  String header = "Basic "+encodedHeader;
					  request.addHeader("Authorization", header);
				  }
				};
			ErrorHandler errorHandler = new ErrorHandler() {
				@Override
				public Throwable handleError(RetrofitError error) {
					Log.e(App.LOG_TAG, "ERROR "+error.getMessage());
					return error;
				}
			};
			RestAdapter a = new RestAdapter.Builder()
			   .setEndpoint(App.YOUCAST_SERVER_URL)
			   .setRequestInterceptor(requestInterceptor)
			   .setErrorHandler(errorHandler)
			   .build();
			return a.create(YouCastInterface.class);
			
		}
	}
	
}
