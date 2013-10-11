package org.ucb.collect.android.tracker;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.ucb.collect.android.R;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class TrackerMainService extends Service {
	private boolean DEBUG = false;
	private static final String TAG = "MAINSERVICE";
	private Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	private final int randomHour = (int) (Math.random()*3);
	private final int random45Min = (int) (Math.random()*46);
	private final int random60Min = (int) (Math.random()*60);
	private final int randomSecond = (int) (Math.random()*60);

	private AlarmManager am;
	private PendingIntent uploadSender;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Start main service");
		Utils.log(new Date(), TAG, "Start main service");
		checkWifi();
		Intent updateIntent = new Intent(this, UpdateReceiver.class);
		updateIntent.setAction("START_UPDATE");
		this.sendBroadcast(updateIntent);
		return Service.START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		File travelStudyFolder = new File(Environment.getExternalStorageDirectory(), "Travel_Study");
		if (!travelStudyFolder.exists()) {
			travelStudyFolder.mkdir();
		}
		
		Log.d(TAG, "Create main service");	
		Utils.log(new Date(), TAG, "Create main service");
		
		if (DEBUG) {
			Calendar onUploadTime = Calendar.getInstance();
			Log.d(TAG, "Upload Time: " + onUploadTime.getTime());
			am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
			Intent uploadIntent = new Intent(this, UploadReceiver.class);
			uploadIntent.putExtra("RESP", "UPLOAD");
			uploadSender = PendingIntent.getBroadcast(this,(int) System.currentTimeMillis(),uploadIntent,PendingIntent.FLAG_UPDATE_CURRENT);
			am.setRepeating(AlarmManager.RTC_WAKEUP, onUploadTime.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, uploadSender);
		}
		else {
			Calendar onUploadTime = Calendar.getInstance();
			onUploadTime.set(Calendar.HOUR_OF_DAY, randomHour);
			if (randomHour == 2) {
				onUploadTime.set(Calendar.MINUTE, random45Min);
			}
			else {
				onUploadTime.set(Calendar.MINUTE, random60Min);
			}
			onUploadTime.set(Calendar.SECOND, randomSecond);
			Log.d(TAG, "Upload Time: " + onUploadTime.getTime());
			Utils.log(new Date(), TAG, "Upload Time: " + onUploadTime.getTime());

			am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
			Intent uploadIntent = new Intent(this, UploadReceiver.class);
			uploadIntent.putExtra("RESP", "UPLOAD");
			uploadSender = PendingIntent.getBroadcast(this,(int) System.currentTimeMillis(),uploadIntent,PendingIntent.FLAG_UPDATE_CURRENT);
			am.setRepeating(AlarmManager.RTC_WAKEUP, onUploadTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, uploadSender);
		}
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Destroy main service");
		Utils.log(new Date(), TAG, "Destroy main service");
		super.onDestroy();
		am.cancel(uploadSender);
		uploadSender.cancel();
		Intent stopUpdate = new Intent(this, UpdateReceiver.class);
		stopUpdate.setAction("STOP_UPDATE");
		this.sendBroadcast(stopUpdate);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private void checkWifi() {
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wm != null && wm.isWifiEnabled()) {
			Log.d(TAG, "WiFi is enabled");
			Utils.log(new Date(), TAG, "WiFi is enabled");
		}
		else {
			sendWiFiNotification();
		}
	}

	private void sendWiFiNotification() {
		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(getApplicationContext());

		builder.setContentTitle("Travel Quality Study")
		.setContentText("Please enable Wifi.")
		.setSmallIcon(R.drawable.exclamation)
		.setContentIntent(getContentIntent("WIFI"))
		.setSound(sound)
		.setAutoCancel(true);

		NotificationManager notifyManager = (NotificationManager)
				getSystemService(Context.NOTIFICATION_SERVICE);
		notifyManager.notify(1, builder.build());
	}

	private PendingIntent getContentIntent(String networkType) {
		Intent	intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
		return PendingIntent.getActivity(getApplicationContext(), 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
