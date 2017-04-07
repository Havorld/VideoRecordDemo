package com.havorld.videorecord.utils;

import android.app.Activity;
import android.util.DisplayMetrics;

public class ScreenUtil {

	public static int getScreenWidth(Activity activity) {

		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;

		return screenWidth;
	}
	public static int getScreenHeight(Activity activity) {
		
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenHeigh = dm.heightPixels;
		
		return screenHeigh;
	}
}
