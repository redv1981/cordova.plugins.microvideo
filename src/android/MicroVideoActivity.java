package cordova.plugins.microvideo;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MicroVideoActivity extends Activity implements SurfaceHolder.Callback {

	private Camera m_camera;
	private Camera.CameraInfo m_cameraInfo;
	private MediaRecorder m_mediaRecorder;
	private ArrayList<String> m_pathArray;
	private SurfaceView m_surfaceView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		m_pathArray = new ArrayList<String>();

		final LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout);

		final Button okBtn = new Button(this);
		okBtn.setText("OK");
		okBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				Intent intent = new Intent().putStringArrayListExtra("result", m_pathArray);
				setResult(Activity.RESULT_OK, intent);
				finish();
			}

		});
		layout.addView(okBtn);

		final Button cancelBtn = new Button(this);
		cancelBtn.setText("Cancel");
		cancelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}

		});
		layout.addView(cancelBtn);

		final Button recordBtn = new Button(this);
		recordBtn.setText("Record");
		recordBtn.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getActionMasked()) {
					case MotionEvent.ACTION_DOWN:
						m_camera.unlock();

						if (null == m_mediaRecorder) {
							m_mediaRecorder = new MediaRecorder();
						}

						m_mediaRecorder.reset();

						m_mediaRecorder.setCamera(m_camera);

						m_mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
						m_mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

						m_mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

						m_mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
						m_mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

						m_mediaRecorder.setAudioChannels(2);
						m_mediaRecorder.setVideoFrameRate(15);
						m_mediaRecorder.setVideoSize(480, 320);

						final String path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), new SimpleDateFormat("'MicroVideo_'yyyyMMdd_HHmmss'.mp4'").format(new Date())).getAbsolutePath();

						m_mediaRecorder.setOutputFile(path);

						try {
							m_mediaRecorder.prepare();
							m_mediaRecorder.start();

							m_pathArray.add(path);
						} catch (IOException e) {
							e.printStackTrace();
						}

						return true;

					case MotionEvent.ACTION_UP:
						if (null != m_mediaRecorder) {
							m_mediaRecorder.stop();
						}

						return true;

					default:
						return false;
				}
			}

		});
		layout.addView(recordBtn);

		m_surfaceView = new SurfaceView(this);
		m_surfaceView.getHolder().addCallback(this);
		layout.addView(m_surfaceView);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		final int cameraCount = Camera.getNumberOfCameras();

		if (0 < cameraCount) {
			final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

			for (int i = 0; cameraCount > i; i++) {
				Camera.getCameraInfo(i, cameraInfo);

				if (Camera.CameraInfo.CAMERA_FACING_BACK == cameraInfo.facing) {
					m_camera = Camera.open(i);
					m_cameraInfo = cameraInfo;

					break;
				}
			}

			if (null == m_camera) {
				Camera.getCameraInfo(0, cameraInfo);

				m_camera = Camera.open(0);
				m_cameraInfo = cameraInfo;
			}
		}

		if (null == m_camera) {
			finish();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (null != m_camera) {
			int degrees = 0;

			switch (getWindowManager().getDefaultDisplay().getRotation()) {
				case Surface.ROTATION_0: degrees = 0; break;
				case Surface.ROTATION_90: degrees = 90; break;
				case Surface.ROTATION_180: degrees = 180; break;
				case Surface.ROTATION_270: degrees = 270; break;
			}

			if (Camera.CameraInfo.CAMERA_FACING_BACK == m_cameraInfo.facing) {
				degrees = (m_cameraInfo.orientation - degrees + 360) % 360;
			} else {
				degrees = (360 - (degrees + m_cameraInfo.orientation) % 360) % 360;
			}

			try {
				m_camera.setDisplayOrientation(degrees);
				m_camera.setPreviewDisplay(holder);
				m_camera.startPreview();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (null != m_mediaRecorder) {
			m_mediaRecorder.release();
		}

		if (null != m_camera) {
			m_camera.stopPreview();
			m_camera.release();
		}
	}

}
