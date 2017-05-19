package com.havorld.videorecord.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;

public class PreviewSizeutil {

	private PreviewSizeutil() {

	}

	public static PreviewSizeutil getInstance() {

		return new PreviewSizeutil();
	}

	/**
	 * 
	 * 获取相机合适的预览尺寸
	 * 
	 * @param list
	 * @param th
	 * @return
	 */
	public Size getCameraPreviewSize(List<Camera.Size> list, int th) {
		CameraSizeComparator sizeComparator = new CameraSizeComparator();
		Collections.sort(list, sizeComparator);
		Size size = null;
		for (int i = 0; i < list.size(); i++) {
			size = list.get(i);
			if ((size.width > th) && equalRate(size, 1.3f)) {
				break;
			}
		}
		return size;
	}

	public static boolean equalRate(Size size, float rate) {
		float r = (float) (size.width) / (float) (size.height);
		if (Math.abs(r - rate) <= 0.2) {
			return true;
		} else {
			return false;
		}
	}

	public class CameraSizeComparator implements Comparator<Camera.Size> {
		// 按升序排列
		@Override
		public int compare(Size lhs, Size rhs) {
			if (lhs.width == rhs.width) {
				return 0;
			} else if (lhs.width > rhs.width) {
				return 1;
			} else {
				return -1;
			}
		}

	}

	/**
	 * 获取合适的预览尺寸(亲测可用)
	 * 
	 * @param parameters
	 * @param screenResolution
	 * @return point.x预览的宽,point.y预览的高
	 */
	public static Point getCameraResolution(Activity activity, Camera.Parameters parameters) {

		// 注意：大多会把相机屏幕翻转90度，所以设置相机宽高时对应的是高宽，此处在获取时做了颠倒
		Point screenResolution = new Point(ScreenUtil.getScreenHeight(activity) - DensityUtil.dip2px(activity, 150),
				ScreenUtil.getScreenWidth(activity));
		String previewSizeValueString = parameters.get("preview-size-values");
		// saw this on Xperia
		if (previewSizeValueString == null) {
			previewSizeValueString = parameters.get("preview-size-value");
		}
		// 1920x1080,1440x1080,3840x2160,1280x720,960x720,864x480,800x480,768x432,720x480,640x480,576x432,480x320,384x288,352x288,320x240,240x160,176x144
		Log.e(TAG, "previewSizeValueString:" + previewSizeValueString);
		Point cameraResolution = null;

		if (previewSizeValueString != null) {
			cameraResolution = findBestPreviewSizeValue(previewSizeValueString, screenResolution);
		}

		if (cameraResolution == null) {
			// Ensure that the camera resolution is a multiple of 8, as the
			// screen may not be.
			cameraResolution = new Point((screenResolution.x >> 3) << 3, (screenResolution.y >> 3) << 3);

		}

		return cameraResolution;
	}

	private static final Pattern COMMA_PATTERN = Pattern.compile(",");
	private static final String TAG = "Havorld";

	private static Point findBestPreviewSizeValue(CharSequence previewSizeValueString, Point screenResolution) {
		int bestX = 0;
		int bestY = 0;
		int diff = Integer.MAX_VALUE;
		for (String previewSize : COMMA_PATTERN.split(previewSizeValueString)) {

			previewSize = previewSize.trim();
			int dimPosition = previewSize.indexOf('x');
			if (dimPosition < 0) {
				Log.e(TAG, "Bad preview-size: " + previewSize);
				continue;
			}

			int newX;
			int newY;
			try {
				newX = Integer.parseInt(previewSize.substring(0, dimPosition));
				newY = Integer.parseInt(previewSize.substring(dimPosition + 1));
			} catch (NumberFormatException nfe) {
				Log.e(TAG, "Bad preview-size: " + previewSize);
				continue;
			}

			int newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y);
			if (newDiff == 0) {
				bestX = newX;
				bestY = newY;
				break;
			} else if (newDiff < diff) {
				bestX = newX;
				bestY = newY;
				diff = newDiff;
			}

		}

		if (bestX > 0 && bestY > 0) {
			return new Point(bestX, bestY);
		}
		return null;
	}

	/**
	 * 修改相机的预览尺寸，调用此方法就行
	 * 
	 * @param camera
	 *            相机实例
	 * @param viewWidth
	 *            预览的surfaceView的宽
	 * @param viewHeight
	 *            预览的surfaceView的高
	 */
	public static void changePreviewSize(Camera camera, int viewWidth, int viewHeight) {
		if (camera == null) {
			return;
		}

		Camera.Parameters parameters = camera.getParameters();
		List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
		Camera.Size closelySize = null;// 储存最合适的尺寸
		for (Camera.Size size : sizeList) { // 先查找preview中是否存在与surfaceview相同宽高的尺寸
			if ((size.width == viewWidth) && (size.height == viewHeight)) {
				closelySize = size;
			}
		}
		if (closelySize == null) {
			// 得到与传入的宽高比最接近的size
			float reqRatio = ((float) viewWidth) / viewHeight;
			float curRatio, deltaRatio;
			float deltaRatioMin = Float.MAX_VALUE;
			for (Camera.Size size : sizeList) {
				if (size.width < 1024)
					continue;// 1024表示可接受的最小尺寸，否则图像会很模糊，可以随意修改
				curRatio = ((float) size.width) / size.height;
				deltaRatio = Math.abs(reqRatio - curRatio);
				if (deltaRatio < deltaRatioMin) {
					deltaRatioMin = deltaRatio;
					closelySize = size;
				}
			}
		}
		if (closelySize != null) {
			Log.e(TAG, "预览尺寸修改为：" + closelySize.width + "*" + closelySize.height);
			parameters.setPreviewSize(closelySize.width, closelySize.height);
			camera.setParameters(parameters);
		}
	}

	/**
	 * 小有问题 通过对比得到与宽高比最接近的预览尺寸（如果有相同尺寸，优先选择）
	 * 
	 * @param isPortrait
	 *            是否竖屏
	 * @param surfaceWidth
	 *            需要被进行对比的原宽
	 * @param surfaceHeight
	 *            需要被进行对比的原高
	 * @param preSizeList
	 *            需要对比的预览尺寸列表
	 * @return 得到与原宽高比例最接近的尺寸
	 */
	public static Camera.Size getCloselyPreSize(boolean isPortrait, int surfaceWidth, int surfaceHeight,
			List<Camera.Size> preSizeList) {
		Log.e(TAG, "surfaceWidth:" + surfaceWidth + "----" + "surfaceHeight:" + surfaceHeight);
		int reqTmpWidth;
		int reqTmpHeight;
		// 当屏幕为垂直的时候需要把宽高值进行调换，保证宽大于高
		if (isPortrait) {
			reqTmpWidth = surfaceHeight;
			reqTmpHeight = surfaceWidth;
		} else {
			reqTmpWidth = surfaceWidth;
			reqTmpHeight = surfaceHeight;
		}
		// 先查找preview中是否存在与surfaceview相同宽高的尺寸
		for (Camera.Size size : preSizeList) {
			if ((size.width == reqTmpWidth) && (size.height == reqTmpHeight)) {
				return size;
			}
		}

		// 得到与传入的宽高比最接近的size
		float reqRatio = ((float) reqTmpWidth) / reqTmpHeight;
		float curRatio, deltaRatio;
		float deltaRatioMin = Float.MAX_VALUE;
		Camera.Size retSize = null;
		for (Camera.Size size : preSizeList) {
			curRatio = ((float) size.width) / size.height;
			deltaRatio = Math.abs(reqRatio - curRatio);
			if (deltaRatio < deltaRatioMin) {
				deltaRatioMin = deltaRatio;
				retSize = size;
			}
		}

		return retSize;
	}

}
