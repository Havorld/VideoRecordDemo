package com.havorld.videorecorddemo;

import java.io.File;
import java.util.List;

import com.havorld.videorecord.utils.PreviewSizeutil;
import com.havorld.videorecord.utils.ScreenUtil;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
/**
 * 
 *博客地址: http://blog.csdn.net/xiaohao0724/article/details/68488226
 */
public class VideoRecordActivity extends Activity implements OnClickListener, SurfaceHolder.Callback {
	private static final String TAG = "Havorld";
	//路径/storage/emulated/0/Movies/
	private String savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + File.separator;
	private String path;
	private MediaRecorder mediaRecorder;// 录制视频的类
	private SurfaceView surfaceview;// 显示视频的控件
	private Camera camera;// 摄像头
	private SurfaceHolder surfaceHolder;
	private FrameLayout frameLayout;
	private ImageButton imageButton, reset, ok;
	private boolean isSufaceCreated = false;
	private boolean isRecording = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 选择支持半透明模式,在有surfaceview的activity中使用。
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.activity_video_record);
		initView();
	}

	private void initView() {

		frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
		imageButton = (ImageButton) findViewById(R.id.imageButton);
		reset = (ImageButton) findViewById(R.id.reset);
		ok = (ImageButton) findViewById(R.id.ok);

		surfaceview = (SurfaceView) this.findViewById(R.id.surfaceview);
		frameLayout.setOnClickListener(this);
		imageButton.setOnClickListener(this);
		reset.setOnClickListener(this);
		ok.setOnClickListener(this);
		// 绑定SurfaceView，取得SurfaceHolder对象
		surfaceHolder = surfaceview.getHolder();
		// surfaceHolder加入回调接口
		surfaceHolder.addCallback(this);
		// 设置预览大小
		// surfaceHolder.setFixedSize(width, height);
		// 设置显示器类型 ，setType必须设置
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.frameLayout:
		case R.id.imageButton:
			isRecording = !isRecording;
			setRecord(isRecording);
			break;
		case R.id.ok:
			Intent intent = getIntent();
			if (intent != null) {
				intent.putExtra("path", path);
				setResult(-1, intent);
			}
			finish();
			break;
		case R.id.reset:
			if (path == null || path == "")
				return;
			File file = new File(path);
			 if (file.exists()) {
				 file.delete();
			}
			 file = null;
			break;
		default:
			break;
		}
	}

	private void setRecord(boolean isRecording) {
		if (isRecording) {
			initMediaRecorder();
			frameLayout.setBackgroundResource(R.drawable.start_bc);
			imageButton.setBackgroundResource(R.drawable.start);
			reset.setVisibility(View.GONE);
			ok.setVisibility(View.GONE);
		} else {
			freeMediaRecorder();
			frameLayout.setBackgroundResource(R.drawable.stop_bc);
			imageButton.setBackgroundResource(R.drawable.stop);
			reset.setVisibility(View.VISIBLE);
			ok.setVisibility(View.VISIBLE);
		}
	}

	private void initCamera() {

		// 获取照相机实例
		camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
		// 获取相机的参数
		Parameters parameters = camera.getParameters();

		// 获取支持的预览尺寸
		List<Size> sizes = parameters.getSupportedPreviewSizes();
		for (int i = 0; i < sizes.size(); i++) {
//			 Log.e(TAG,"width:"+sizes.get(i).width+"---"+"height:"+sizes.get(i).height);
		}

		Camera.Size csize = camera.getParameters().getPreviewSize();
		Log.e(TAG, csize.width + "---" + csize.height);
		Size previewSize = PreviewSizeutil.getInstance().getCameraPreviewSize(parameters.getSupportedPreviewSizes(),
				ScreenUtil.getScreenHeight(this));
		Log.e(TAG, previewSize.width + "===" + previewSize.height);
		
		parameters.setPreviewSize(previewSize.width, previewSize.height);

		// 获取对焦模式
		List<String> focusModes = parameters.getSupportedFocusModes();
		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
			// 设置自动对焦
			parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		}
		// 设置预览帧数
		parameters.setPreviewFrameRate(20);
		// 解决在电脑端视频缩略图倾斜了90的问题(注意：设置这个参数后如果PictureSize的参数是X*Y，得到的图片大小是Y*X)
		parameters.set("rotation", 90);
		camera.setParameters(parameters);
		// 设置预览方向， 摄像图旋转90度
		camera.setDisplayOrientation(90);

		try {
			camera.setPreviewDisplay(surfaceHolder);
			camera.startPreview();
		} catch (Exception e) {
			releaseResource();
		}
	}

	private void initMediaRecorder() {

		if (mediaRecorder != null) {

			freeMediaRecorder();
		}
		mediaRecorder = new MediaRecorder();// 创建mediarecorder对象
		if (camera == null) {

			initCamera();
		}
		camera.unlock();// 允许media进程得以访问camera。
		mediaRecorder.setCamera(camera);
		// 解决在电脑上播放视频旋转90度的问题
		mediaRecorder.setOrientationHint(90);

		// 设置音频源,从麦克风采集声音
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// 设置录制视频源,从摄像头采集图像
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		/*
		 * //在Android2.2 (API
		 * Level8)之前，你必须直接设置输出格式和编码格式等参数，而不是使用CamcorderProfile // 设置视频的输出格式:
		 * THREE_GPP(3gp)、MPEG_4(mp4)
		 * mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		 * //设置音频的编码格式
		 * mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		 * //设置视频的编码格式
		 * mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		 * //设置视频分辨率 mediaRecorder.setVideoSize(960,720);
		 */

		// 在Android2.2 (API Level8)之后，你可以直接使用CamcorderProfile
		CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
		// 构造CamcorderProfile,使用高质量视频
		mediaRecorder.setProfile(profile);
		// 设置视频分辨率
		mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);

		// 设置视频捕获帧速率
		mediaRecorder.setVideoFrameRate(30);
		// 设置视频编码位率(比特率)
		mediaRecorder.setVideoEncodingBitRate(5 * 512 * 512);
		// 设置最大录像时间单位为毫秒
		mediaRecorder.setMaxDuration(10000);
		// 设置使用SurfaceView来显示视频预览
		mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
		
		File file = new File(savePath);  
        if (!file.exists()) {  
            //多级文件夹的创建  
            file.mkdirs();  
        }  
        file = null;
        path = savePath+"XiuMF_" + System.currentTimeMillis() + ".mp4";
		// 设置视频输出路径
		mediaRecorder.setOutputFile(path);
		try {
			// 准备录制
			mediaRecorder.prepare();
			mediaRecorder.start();
		} catch (Exception e) {
			releaseResource();
			e.printStackTrace();
		}

	}

	private void freeMediaRecorder() {

		if (mediaRecorder != null) {
			// 停止录制
			mediaRecorder.stop();
			// 重置MediaRecorder对象，使其为空闲状态
			mediaRecorder.reset();
			// 释放MediaRecorder对象
			mediaRecorder.release();
			mediaRecorder = null;
		}
		if (camera != null) {

			camera.lock();
		}
	}

	private void freeCamera() {

		if (camera != null) {

			try {
				camera.setPreviewDisplay(null);
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				
				camera.setPreviewCallback(null);
				camera.stopPreview();
				camera.lock();
				camera.release();
				camera = null;
			}
		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
		surfaceHolder = holder;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		isSufaceCreated = true;
		// 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
		surfaceHolder = holder;
		initCamera();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		isSufaceCreated = true;
		releaseResource();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseResource();
	}

	private void releaseResource() {
		freeCamera();
		freeMediaRecorder();
		surfaceview = null;
		surfaceHolder = null;
	}
	
	

	
}
