package com.havorld.videorecord.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class VideoUtil {

	private static final String TAG = "Havorld";

	/**
	 * 获取拍摄的视频路径
	 * 
	 * @param context
	 * @param uri
	 * @return
	 */
	public static String getVideoPath(Context context, Uri uri) {

		String[] projection = { MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID };
		String sortOrder = MediaStore.Video.Media._ID;
		Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder);
		int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String videoPath = cursor.getString(columnIndex);
		Log.e(TAG, "videoPath:" + videoPath);
		return videoPath;

	}

	/**
	 * 获取SDCard上的视频路径集合
	 * 
	 * @param context
	 * @return
	 */
	public static List<String> getVideoPathList(Context context) {

		List<String> list = new ArrayList<String>();

		try {
			String[] columns = { MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID };
			String orderBy = MediaStore.Video.Media._ID;

			Cursor videoCursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
					columns, null, null, orderBy);

			if (videoCursor != null && videoCursor.getCount() > 0) {
				while (videoCursor.moveToNext()) {
					int dataColumnIndex = videoCursor.getColumnIndex(MediaStore.Images.Media.DATA);
					String videoPath = videoCursor.getString(dataColumnIndex);
					list.add(videoPath);
					Log.e(TAG, "videoPath:" + videoPath);
				}
			}
		} catch (Exception e) {

			Log.e(TAG, e.toString());
		}
		return list;

	}
	

}
