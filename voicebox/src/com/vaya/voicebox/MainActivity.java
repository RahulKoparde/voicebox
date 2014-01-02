package com.vaya.voicebox;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.vaya.voicebox.AudioRecorder.MessageProto;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private Messenger msgService = null;
	boolean mBound = false;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	UpdateDuration upd = null;

	public static final String LOG_TAG = "VoiceBox"; //TAG TO USE FOR ALL DEBUG	
	
	/*
	 * ******************
	 * UI Stuff
	 * ******************
	 */
	
	public void TouchStartRecord(View view) { 
		Log.d(MainActivity.LOG_TAG, "Start Record button hit");
		sendMsgServ(AudioRecorder.MSG_START_RECORD);
	}

	public void TouchStopRecord(View view) { 
		Log.d(MainActivity.LOG_TAG, "Stop Record button hit");
		sendMsgServ(AudioRecorder.MSG_STOP_RECORD);
	}

	private void toggleUiRecord(boolean recording) {
		TextView t =(TextView)findViewById(R.id.textView1); 
		Button btn_srt = (Button)findViewById(R.id.button_start);
		Button btn_stp = (Button)findViewById(R.id.button_stop);

		btn_srt.setEnabled(!recording);
		btn_stp.setEnabled(recording);
		if (recording) {
			t.setText("Recording");
		} else {
			t.setText("Stopped Recording");
		}
	}
	
	private void updateDuration(long t) {
		Log.d(MainActivity.LOG_TAG, "Update duration"); 
		TextView txt =(TextView)findViewById(R.id.text_duration);
		long t_now = System.currentTimeMillis();
		long elapse = (t_now - t) / 1000;
		txt.setText("Duration : " + Long.toString(elapse) + "sec");
	}
	
	
	private class UpdateDuration extends AsyncTask<Long, Long, Long> {
		
		@Override
		protected Long doInBackground(Long... arg0) {
			Log.d(MainActivity.LOG_TAG, "Start async task"); 
			 while (true) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					publishProgress(arg0);
					if (isCancelled()) break;
	         }
	         return arg0[0];
		}
	     protected void onProgressUpdate(Long... progress) {
	    	 Log.d(MainActivity.LOG_TAG, "onProgressUpdate task"); 
	    	 updateDuration(progress[0]);
	     }

	     protected void onPostExecute(Long result) {
	    	 Log.d(MainActivity.LOG_TAG, "Stop async task"); 
	     }
	 }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_settings:
			OpenSettings();
			return true;
		case R.id.action_filelist:
			OpenFileList();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void OpenFileList() {
		startActivity(new Intent(MainActivity.this, FileListActivity.class));
	}

	private void OpenSettings() {
		startActivity(new Intent(MainActivity.this, SettingsActivity.class));
	}

	
	
	/*
	 * ******************
	 * Messaging service <-> Activity
	 * ******************
	 */
	
	
	//Create connection between activity and the recording service
	private ServiceConnection mConnection  = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBound = true;
			Log.d(MainActivity.LOG_TAG, "onServiceConnected() called");  	
			msgService = new Messenger(service);
			try {
				Message msg = Message.obtain(null,
						AudioRecorder.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				msgService.send(msg);
				msg = Message.obtain(null,
						AudioRecorder.MSG_SET_VALUE, this.hashCode(), 0);
				sendMsgServ(AudioRecorder.MSG_GET_STATUS);
			} catch (RemoteException e) {
				Log.e(MainActivity.LOG_TAG, "onServiceConnected() crash : " + e.toString());  	
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
			msgService = null;
			Log.d(MainActivity.LOG_TAG, "onServiceDisconnected() called");
		}
	};

	//Wrapper to send message to serv
	private void sendMsgServ(int msg) {
		try {
			msgService.send(Message.obtain(null,
					msg, msg, 0));
		} catch (RemoteException e) {
		}
	}

	//Handle incoming message from server
	class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			Log.d(MainActivity.LOG_TAG, "handleMessage Acti : " + msg.toString());

			switch (msg.what) {
			case AudioRecorder.MSG_SAY_HELLO:
				Log.d(MainActivity.LOG_TAG, "Service say hello");
				break;
			case AudioRecorder.MSG_START_RECORD:
				Log.d(MainActivity.LOG_TAG, "Service say it started recording");
				sendMsgServ(AudioRecorder.MSG_TIME_START);
				toggleUiRecord(true);
				break;
			case AudioRecorder.MSG_STOP_RECORD:
				Log.d(MainActivity.LOG_TAG, "Service say it stopped recording"); 
				toggleUiRecord(false);
				if (upd != null) upd.cancel(true);
				break;
			case AudioRecorder.MSG_TIME_START:
				MessageProto val = (MessageProto) msg.obj;
				Log.d(MainActivity.LOG_TAG, "Service sending time start : "+ Long.toString(val.value));
				 upd = new UpdateDuration();
				 upd.execute(val.value, val.value, val.value);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	
	/*
	 * ******************
	 * Activity
	 * ******************
	 */
	
	public MainActivity() {
		Log.d(MainActivity.LOG_TAG, "Program Started");
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(MainActivity.LOG_TAG, "Pause MainActivity"); 
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(MainActivity.LOG_TAG, "Start MainActivity"); 
		Intent intent = new Intent(this, AudioRecorder.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(MainActivity.LOG_TAG, "Stop MainActivity"); 
	}

	@Override
	protected void onResume() {
		Intent intent = new Intent(this, AudioRecorder.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		Log.d(MainActivity.LOG_TAG, "Resume MainActivity");   
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

}
