package cordova.plugins.microvideo;

import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;

public class MicroVideoActivity extends Activity implements SurfaceHolder.Callback {

	private int m_count = 0;
	private Camera m_camera;
	private MediaRecorder m_mediaRecorder;
	private SurfaceView m_surfaceView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout);

		final Button button = new Button(this);
		button.setText("Record");
		button.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getActionMasked()) {
					case MotionEvent.ACTION_DOWN:
						m_camera.unlock();

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

						m_mediaRecorder.setOutputFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), String.format("MediaRecorder_%d.mp4", ++m_count)).getAbsolutePath());

						try {
							m_mediaRecorder.prepare();
							m_mediaRecorder.start();
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
		layout.addView(button);

		m_surfaceView = new SurfaceView(this);
		m_surfaceView.getHolder().addCallback(this);
		layout.addView(m_surfaceView);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		m_camera = Camera.open();

		if (null == m_camera) {
			finish();
		}

		m_mediaRecorder = new MediaRecorder();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		try {
			m_camera.setDisplayOrientation(90);
			m_camera.setPreviewDisplay(holder);
			m_camera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
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
