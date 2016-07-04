package com.jk.soundjk;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class SoundService extends Service {

	public double threshold;
	public double hold = 0;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private class RecorderTask extends TimerTask {
		private MediaRecorder recorder;

		public RecorderTask(MediaRecorder recorder) {
			this.recorder = recorder;
		}

		public void run() {
			int amplitude = recorder.getMaxAmplitude();
			double amp = 20 * Math.log10((double) Math.abs(amplitude));
			String msg = Double.toString(amp);
			Log.i("DEBUG", msg);
			
			if (hold > threshold && amp > threshold) {
				ring();
				stopSelf();
			}
			hold = amp;
		}
	}

	Timer timer;
	static MediaRecorder recorder;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle extras = intent.getExtras();
		this.threshold = extras.getDouble("threshold");
		Toast.makeText(this, "Sound Detection Initialised", Toast.LENGTH_SHORT).show();
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		timer = new Timer();
		timer.scheduleAtFixedRate(new RecorderTask(recorder), 0, 500);
		recorder.setOutputFile("/dev/null");

		try {
			recorder.prepare();
			recorder.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		timer.cancel();
		timer.purge();
		recorder.stop();
		recorder.release();
		Toast.makeText(this, "Sound Detection Terminated", Toast.LENGTH_SHORT).show();
	}

	public void ring() {
		Intent i = new Intent();
		i.setClass(this, ActiveActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}
}
