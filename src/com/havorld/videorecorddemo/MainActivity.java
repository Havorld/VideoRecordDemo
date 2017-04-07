package com.havorld.videorecorddemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	public final static int VEDIO_CUSTOM = 1;
	private static final String TAG = "Havorld";
	private TextView videoPathText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		videoPathText = (TextView) findViewById(R.id.videoPath);
		findViewById(R.id.customRecord).setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.customRecord:
			intent = new Intent(this, VideoRecordActivity.class);
			startActivityForResult(intent, VEDIO_CUSTOM);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		String videoPath = null;
		if (resultCode != Activity.RESULT_OK) {
			Log.e(TAG, "RESULT_NOT_OK");
			return;
		}
		switch (requestCode) {
		case VEDIO_CUSTOM:// 录制自定义视频
			if (data != null) {

				videoPath = data.getStringExtra("path");
			}
			break;

		default:
			break;
		}
		videoPathText.setText(videoPath);
	}
}
