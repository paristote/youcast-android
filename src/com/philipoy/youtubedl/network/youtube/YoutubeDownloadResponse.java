package com.philipoy.youtubedl.network.youtube;

import com.philipoy.youtubedl.extractor.YoutubeInfoExtractor.YoutubeVideoInfos;

public class YoutubeDownloadResponse {
	
	public static YoutubeDownloadResponse ok() {
		YoutubeDownloadResponse resp = new YoutubeDownloadResponse();
		resp.status = Status.OK;
		return resp;
	}
	
	public static YoutubeDownloadResponse okWithInfos(YoutubeVideoInfos infos) {
		if (infos == null) throw new IllegalArgumentException("You must provide an object with the video information");
		YoutubeDownloadResponse resp = new YoutubeDownloadResponse();
		resp.status = Status.OK;
		resp.videoInfos = infos;
		return resp;
	}
	
	public static YoutubeDownloadResponse retry(String newLoc) {
		if (newLoc == null) throw new IllegalArgumentException("You must provide a new location for the redirection");
		
		YoutubeDownloadResponse resp = new YoutubeDownloadResponse();
		resp.status = Status.RETRY;
		resp.newLocation = newLoc;
		return resp;
	}
	
	public static YoutubeDownloadResponse error() {
		YoutubeDownloadResponse resp = new YoutubeDownloadResponse();
		resp.status = Status.FAILED;
		return resp;
	}
	
	private YoutubeDownloadResponse() {}

	public Status status;
	public enum Status {
		OK,
		RETRY,
		FAILED
	}
	public String newLocation;
	public YoutubeVideoInfos videoInfos;
}
