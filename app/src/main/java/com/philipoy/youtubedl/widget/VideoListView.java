package com.philipoy.youtubedl.widget;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import com.philipoy.youtubedl.R;

public class VideoListView extends ListView implements LoaderCallbacks<Cursor> {
	
	private VideoListStatusListener mStatusListener;
	
	public VideoListView(Context context) {
		super(context);
	}

	public VideoListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public VideoListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void initList(Activity activity, VideoListStatusListener statusListener) {
		
		mStatusListener = statusListener;
		
		SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getContext(), 
				R.layout.item_list_video, 
				null, 
				new String[] {
					MediaStore.Video.Media._ID,
					MediaStore.Video.Media.TITLE,
					MediaStore.Video.Media.DATE_ADDED
				}, 
				new int[] {
					// will be filled with the video's thumbnail thanks to the custom ViewBinder
					R.id.item_video_thumbnail,
					R.id.item_video_title,
					R.id.item_video_date
				}, 
				0);
		
		cursorAdapter.setViewBinder(new VideoItemViewBinder());
		setAdapter(cursorAdapter);
		setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				// id is the value of the row MediaStore.Video.Media._ID so we can use it directly in the content resolver
				Uri videoUri = ContentUris.withAppendedId(
				        android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
				Intent playVideo = new Intent(Intent.ACTION_VIEW, videoUri);
				getContext().startActivity(playVideo);
			}
		});
		
		initLoader(activity);
		
	}
	
	public void initLoader(Activity activity) {
		activity.getLoaderManager().initLoader(0, null, this);
	}
	
	public void restartLoader(Activity activity) {
		activity.getLoaderManager().restartLoader(0, null, this);
	}
	
	public void stopLoader(Activity activity) {
		activity.getLoaderManager().destroyLoader(0);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		String[] projection = {
				MediaStore.Video.Media._ID,
				MediaStore.Video.Media.TITLE,
				MediaStore.Video.Media.DATE_ADDED
		};
		String selection = MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " = ?";
		String[] selectionArgs = {
				"YouCast"
		};
		String orderBy = MediaStore.Video.Media.DATE_ADDED + " DESC";
		
		return new CursorLoader(getContext(), uri, projection, selection, selectionArgs, orderBy);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		((SimpleCursorAdapter)getAdapter()).swapCursor(cursor);
		if (mStatusListener != null) mStatusListener.onVideoListLoaded(cursor.getCount());
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		((SimpleCursorAdapter)getAdapter()).swapCursor(null);
	}

	private class VideoItemViewBinder implements ViewBinder {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			boolean bound = false;
			if (view instanceof ImageView) {
				int videoId = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
				ContentResolver res = getContext().getContentResolver();
				Bitmap thumbnail = MediaStore.Video.Thumbnails.getThumbnail(res, videoId, MediaStore.Video.Thumbnails.MICRO_KIND, null);
				((ImageView)view).setImageBitmap(thumbnail);
				bound = true;
			} else if (view instanceof TextView) {
				switch (view.getId()) {
				case R.id.item_video_title:
					String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
					title = title.replaceAll("(_+)", " ");
					((TextView)view).setText(title);
					bound = true;
					break;
				case R.id.item_video_date:
					long time = 1000*cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
					String addedOn = getContext().getString(R.string.text_video_added_date);
					((TextView)view).setText(addedOn + " " + DateFormat.format("MMM d, y", time));
					bound = true;
					break;
				}
			}
			return bound;
		}
	}
	
	public static interface VideoListStatusListener {
		public void onVideoListLoaded(int videoCount);
	}
}
