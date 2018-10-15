package com.philipoy.youtubedl.extractor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.philipoy.youtubedl.App;

public class YoutubeInfoExtractor {

	private String                  mBody;
	
	private String                  mTitle, mStreamMap;
	
	private List<YoutubeVideoInfos> mFormatsList;
	
	private YoutubeVideoInfos       mBestFmt;
	
	private Status                  mStatus;
	
	private enum Status {
		CONTINUE,
		RETRY_DETAIL_PAGE,
		ERROR
	};
	
	private YoutubeInfoExtractor(String response) {
		mBody = response;
		mStatus = Status.CONTINUE;
	}
	
	public static YoutubeVideoInfos extract(String body, boolean hd) {
		YoutubeInfoExtractor ie = new YoutubeInfoExtractor(body);
		ie.extractVideoInfo();
		ie.extractVideoFormats();
		ie.findBestAvailableFormat(hd);
		return ie.getExtract();
	}
	
	public void extractVideoInfo() {
		String[] elements = mBody.split("&");
		for (String elt : elements) {
			String[] param = elt.split("=");
			
			if (param.length < 2) continue; // skip this param if it does not have a value
			
			String key = param[0];
			String value = param[1];
			
			if ("url_encoded_fmt_stream_map".equals(key)) {
				mStreamMap = decodeUrl(value);
				continue;
			}
			if ("title".equals(key)) {
				mTitle = decodeUrl(value);
				continue;
			}
			if ("status".equals(key)) {
				if ("fail".equals(value))
					if (mStatus == Status.CONTINUE)
						mStatus = Status.ERROR; // Decide to cancel this download only if a redirection was not already detected
				continue;
			}
			if ("errorcode".equals(key))
				if ("150".equals(value))
					mStatus = Status.RETRY_DETAIL_PAGE;
//			errorcode = 2, reason=Invalid parameters.
//			errorcode = 150, reason=This video contains content from LFP. It is restricted from playback on certain sites.
		}
	}
	
	public void extractVideoFormats() {
		if (mStatus == Status.CONTINUE && mStreamMap != null) {
			// every format string is separated by ,
			String[] formatStrings = mStreamMap.split(",");
			mFormatsList = new ArrayList<YoutubeInfoExtractor.YoutubeVideoInfos>(5);
			for (String formatString : formatStrings) {
				YoutubeVideoInfos formatObject = new YoutubeVideoInfos();
				String decodedFormatString = decodeUrl(formatString); // decode the string to reveal the format parts
				String[] formatPartsArray = decodedFormatString.split("&"); // separate each part of the format
				for (String formatPart : formatPartsArray) {
					String[] partElts = formatPart.split("="); // each part element is in key=value syntax
					String partK = partElts[0];
					String partV = partElts[1];
					if ("type".equals(partK))
					{
						// we just need the first part of the type (type=video/3gpp; codecs="mp4v.20.3, mp4a.40.2")
						formatObject.set("type", partV.split(";")[0]);
					}
					else if ("url".equals(partK))
					{
						// syntax: url=https://r3---sn-42u-i5ol.googlevideo.com/videoplayback?key=yt5
						// partK : url ; partV = https://r3---sn-42u-i5ol.googlevideo.com/videoplayback?key ; partElts[2] = yt5
						String url[] = partV.split("\\?"); // we must separate the url from the url param name
						String urlString = url[0]; // url
						String urlParamK = url[1]; // url param name
						String urlParamV = partElts[2]; // url param value
						formatObject.set(urlParamK, urlParamV);
						formatObject.set("url", urlString);
					} 
					else 
					{
						formatObject.set(partK, partV);
					}
				}
				mFormatsList.add(formatObject);
			}
		}
		
	}
	
	/**
	 * List of formats:
	 * <ul>
	 * <li>37 : HD 1080p (disabled by default)</li>
	 * <li>22 : HD 720p (disabled by default)</li>
	 * <li>18 : 640x360</li>
	 * <li>17 : 176x144</li>
	 * </ul>
	 * @param hd true to activate the HD formats
	 */
	public void findBestAvailableFormat(boolean hd) {
		if (mStatus == Status.CONTINUE) {
			String[] bestFormats;
			if (hd) bestFormats = new String[] {"37", "22","18", "17"};
			else    bestFormats = new String[] {"18", "17"};
			for (String string : bestFormats) {
				for (YoutubeVideoInfos format : mFormatsList) {
					if (string.equals(format.get("itag"))) {
						mBestFmt = format;
						mBestFmt.videoTitle = mTitle;
						
						break;
					}
				}
				if (mBestFmt != null) break;
			}
			Log.d(App.LOG_TAG, "**** Best Format:");
			Log.d(App.LOG_TAG, mBestFmt.toString());
		}
	}
	
	/**
	 * Returns the best extracted format if one was found. <br/>
	 * If the request must be sent to the detail page, an empty object is returned with the attribute retry_detail_page set to true.<br/>
	 * Finally, if an error occurred, or no accepted format was found, returns null.
	 * @return
	 */
	public YoutubeVideoInfos getExtract() {
		YoutubeVideoInfos result = null;
		if (mStatus == Status.CONTINUE)
			result = mBestFmt;
		else if (mStatus == Status.RETRY_DETAIL_PAGE) {
			result = new YoutubeVideoInfos();
			result.retry_detail_page = true;
		}
		return result;
	}
	
	private String decodeUrl(String encodedUrl) {
		try {
			return URLDecoder.decode(encodedUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}
	
	
	
	public class YoutubeVideoInfos {
//		Commented out Nov 27th
//		private final String[] allowedKeys = 
//			{"mm", "expire", "initcwndbps", "mt", "ms", "mv", "type", "itag", "dur",
//			 "url", "ratebypass", "ip", "id", "fexp", "source", "fallback_host", "upn",
//			 "quality", "sparams", "sver", "ipbits", "key", "signature", "requiressl", "gcr" };
		private Map<String, String> content;
		
		public String videoTitle;
		public String videoId;
		public boolean retry_detail_page = false;
		public void set(String key, String value) {
//			if (Arrays.asList(allowedKeys).contains(key)) {
				if (content == null) content = new HashMap<String, String>(25);
				content.put(key, value);
//			}
		}
		public String get(String key) {
			if (content == null) return null;
			else return content.get(key);
		}
		public String getFullUrl() {
			StringBuilder sb = new StringBuilder(get("url")).append("?");
			for (String key : content.keySet()) {
				// we don't need the url params: url, type, fallback_host
				if ("url".equals(key) || "type".equals(key) || "fallback_host".endsWith(key)) continue;
				
				sb.append(key).append("=").append(content.get(key)).append("&");
			}
			return sb.toString();
		}
		public String toString() {
			StringBuilder sb = new StringBuilder("[");
			for (String key : content.keySet()) {
				sb.append("\n\t").append(key).append(":").append(content.get(key));
			}
			return sb.append("\n]").toString();
		}
	}

}
